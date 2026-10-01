package com.elevn.mes.pro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdWorkstation;
import com.elevn.mes.md.mapper.MdWorkstationMapper;
import com.elevn.mes.pro.entity.ProCardProcess;
import com.elevn.mes.pro.entity.ProFeedback;
import com.elevn.mes.pro.entity.ProFeedbackDTO;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProWorkorder;
import com.elevn.mes.pro.mapper.ProFeedbackMapper;
import com.elevn.mes.pro.mapper.ProTaskMapper;
import com.elevn.mes.pro.mapper.ProWorkorderMapper;
import com.elevn.mes.pro.service.ProCardService;
import com.elevn.mes.pro.service.ProFeedbackService;
import com.elevn.mes.pro.service.ProTransConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生产报工 Service 实现 —— 报工是本模块的核心
 *
 * ============================================================
 * 一、报工在链路里的位置
 * ============================================================
 *   工单（计划层）→ 任务（执行层）→ 报工（反馈层，本类）
 *   报工是唯一产生真实产量的动作，也是三层数量台账的源头：
 *     pro_feedback 落一行明细
 *     pro_task.quantity_produced 累加
 *     全部任务完工后 pro_workorder.quantity_produced 累加
 *
 * ============================================================
 * 二、报工总线的三件事（分工方案 S7 原话：回写 + 生成消耗 + 推进流转）
 * ============================================================
 *   写报工、回写任务数量、倒冲物料消耗、推进流转卡是一个原子业务动作，
 *   所以必须在同一个 @Transactional 里完成。中途失败若部分提交，会出现
 *   "报了工但任务数量没加"、"料扣了但产量没涨"这类永远说不清的数据。
 *
 *   三件事各自的落点：
 *     回写数量台账 -> applyProducedQuantity（本类私有方法，唯一出口）
 *     生成物料消耗 -> ProTransConsumeService.generateOnFeedback（倒冲）
 *     推进流转卡   -> ProCardService.advanceOnFeedback
 *
 * ============================================================
 * 三、数量台账的唯一出口（对齐分工方案里"所有数量变更必须走同一个方法"）
 * ============================================================
 *   任务的三个数量字段（已生产/合格/不良）与状态，只允许通过
 *   applyProducedQuantity() 这一个私有方法修改，禁止在别处直接 update。
 *   这样"数量与状态怎么变"只有一个地方要维护，不会因为多入口改漏。
 *
 * ============================================================
 * 四、报错了怎么撤 —— 冲销（reverse），不是删除
 * ============================================================
 *   一次报工会连带改任务数量、工单产量、流转卡过站、物料消耗四样东西。
 *   把报工行删掉，这四样就都成了"没有来源"的孤儿数据，永远说不清。
 *   所以冲销的做法是：本行状态置 REVERSED 并留痕（谁/何时/为什么），
 *   再把上面四样**在同一个事务里一起回退**。
 *
 *   回退不是简单地"反向记一笔"，而是把当初推进的东西原样收回来：
 *   数量减回、消耗按 feedback_id 精准删掉、过站进度退回、卡与工单状态回退。
 *   这样冲销后的账面，跟这条报工从没发生过是一致的。
 *
 * ============================================================
 * 五、校验规则（L3：数量对不上必须拒绝）
 * ============================================================
 *   报工：
 *   1. 任务必须存在，且状态不能是 CANCELED
 *   2. 累计已报 + 本次报工 <= 任务排产数量
 *   3. 累计已产出 + 本次报工 <= 本道工序的投入量（取自流转卡过站行）
 *      —— 上一道只出了 4 件，本道就报不出 9 件，防"凭空多报产量"
 *   4. 本次报工数量 = 合格 + 不良 + 待检，三者必须自洽
 *      （待检数量不填时由后端自动算，不信任前端凑数）
 *
 *   冲销：
 *   5. 已冲销的不能重复冲销
 *   6. 下游工序已产出成品则拒绝 —— 要先冲下游，否则下游产出失去来源
 *   7. 任务/工单的累计产量小于要冲回的量则拒绝 —— 账已经乱了，先查清楚再动
 *
 */
@Service
public class ProFeedbackServiceImpl implements ProFeedbackService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 任务状态：待生产 */
    private static final String TASK_NORMAL = "NORMAL";
    /** 任务状态：生产中 */
    private static final String TASK_WORKING = "WORKING";
    /** 任务状态：已完工 */
    private static final String TASK_FINISHED = "FINISHED";
    /** 任务状态：已取消 */
    private static final String TASK_CANCELED = "CANCELED";

    /** 报工状态：已报工（默认，数量已计入进度） */
    private static final String FEEDBACK_SUBMITTED = "SUBMITTED";
    /** 报工状态：已冲销（数量已从任务与工单扣回，仅留痕迹） */
    private static final String FEEDBACK_REVERSED = "REVERSED";

    /** 工单状态：已下达 */
    private static final String WORKORDER_CONFIRMED = "CONFIRMED";
    /** 工单状态：已完工 */
    private static final String WORKORDER_FINISHED = "FINISHED";

    /** 报工类型：过程报工（默认） */
    private static final String FEEDBACK_TYPE_PROCESS = "PROCESS";
    /** 报工途径：PC 端（默认，页面录入） */
    private static final String CHANNEL_PC = "PC";

    @Autowired
    private ProFeedbackMapper proFeedbackMapper;

    @Autowired
    private ProTaskMapper proTaskMapper;

    @Autowired
    private ProWorkorderMapper proWorkorderMapper;

    @Autowired
    private MdWorkstationMapper mdWorkstationMapper;

    @Autowired
    private ProCardService proCardService;

    @Autowired
    private ProTransConsumeService proTransConsumeService;

    // ============================================================
    // 查询
    // ============================================================

    @Override
    public PageInfo<ProFeedback> page(int pageNum, int pageSize, ProFeedback proFeedback) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProFeedback> list = proFeedbackMapper.selectByCondition(proFeedback);
        return new PageInfo<>(list);
    }

    @Override
    public ProFeedback queryById(Long id) {
        return proFeedbackMapper.selectById(id);
    }

    @Override
    public List<ProFeedback> queryByTaskId(Long taskId) {
        return proFeedbackMapper.selectByTaskId(taskId);
    }

    @Override
    public List<ProFeedback> queryList(ProFeedback proFeedback) {
        return proFeedbackMapper.selectByCondition(proFeedback);
    }

    // ============================================================
    // 报工预览：告诉前端"这道工序还剩多少能报"
    // ============================================================

    @Override
    public Map<String, Object> preview(Long taskId) {
        if (taskId == null) {
            throw new BusinessException("请选择要报工的生产任务");
        }
        ProTask task = proTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("生产任务不存在或已被删除");
        }

        BigDecimal scheduled = nvl(task.getQuantity());
        BigDecimal produced = nvl(task.getQuantityProduced());
        BigDecimal remain = scheduled.subtract(produced);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("task", task);
        result.put("quantityScheduled", scheduled);
        // 已报工数量直接取任务上的累计值（它就是由报工累加出来的）
        result.put("quantityProduced", produced);
        result.put("quantityRemain", remain.compareTo(BigDecimal.ZERO) > 0 ? remain : BigDecimal.ZERO);
        // 该任务历史报工明细，弹窗里给用户看"之前报过什么"
        result.put("feedbackList", proFeedbackMapper.selectByTaskId(taskId));
        return result;
    }

    // ============================================================
    // 报工总线
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> feedback(ProFeedbackDTO dto) {
        if (dto == null || dto.getTaskId() == null) {
            throw new BusinessException("请选择要报工的生产任务");
        }

        // ---- 1. 任务必须可报工 ----
        ProTask task = proTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("生产任务不存在或已被删除");
        }
        if (TASK_CANCELED.equals(task.getStatus())) {
            throw new BusinessException("该任务已取消，不能再报工");
        }

        // ---- 2. 数量校验：本次报工 > 0，且累计不许超过排产数量 ----
        BigDecimal thisQty = dto.getQuantityFeedback();
        if (thisQty == null || thisQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("本次报工数量必须大于 0");
        }
        BigDecimal scheduled = nvl(task.getQuantity());
        BigDecimal already = nvl(proFeedbackMapper.sumFeedbackByTaskId(task.getTaskId()));
        BigDecimal afterFeedback = already.add(thisQty);
        if (afterFeedback.compareTo(scheduled) > 0) {
            throw new BusinessException("报工数量超出该工序的排产数量。排产数量 "
                    + strip(scheduled) + "，已报工 " + strip(already)
                    + "，本次最多还能报 " + strip(scheduled.subtract(already)));
        }

        // ---- 2.5 流转卡校验：不能报出比投进来的更多的产量 ----
        // 过站行上的 quantity_input 就是本道实际投进来的活：
        //   首道 = 工单数量（整批投料），其余道 = 上一道累计产出。
        // 防的是"凭空多报产量"：上一道只出了 4 件，本道却要报 9 件，
        // 那 5 件库里没有、账上却有了。
        // 工单没有卡（功能上线前排产的历史数据）时 cardRow 为 null，跳过校验。
        ProCardProcess cardRow = proCardService.queryCardProcess(task.getWorkorderId(), task.getProcessId());
        if (cardRow != null && cardRow.getQuantityInput() != null) {
            BigDecimal cardInput = nvl(cardRow.getQuantityInput());
            BigDecimal cardOutput = nvl(cardRow.getQuantityOutput());
            BigDecimal afterOutput = cardOutput.add(thisQty);
            if (afterOutput.compareTo(cardInput) > 0) {
                throw new BusinessException("本道工序投进来的量不够，报不出这么多产量。"
                        + "流转卡上本道投入 " + strip(cardInput)
                        + "，已产出 " + strip(cardOutput)
                        + "，本次最多还能报 " + strip(cardInput.subtract(cardOutput))
                        + "。如果确实做了这么多，请先检查上一道工序是不是漏报了");
            }
        }

        // ---- 3. 三方数量自洽：本次报工 = 合格 + 不良 + 待检 ----
        BigDecimal qualified = nvl(dto.getQuantityQualified());
        BigDecimal unqualified = nvl(dto.getQuantityUnqualified());
        BigDecimal uncheck = dto.getQuantityUncheck();
        if (uncheck == null) {
            // 前端没填待检数量就自动算，避免要用户自己凑加法
            uncheck = thisQty.subtract(qualified).subtract(unqualified);
        }
        if (qualified.compareTo(BigDecimal.ZERO) < 0 || unqualified.compareTo(BigDecimal.ZERO) < 0
                || uncheck.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("合格品、不良品、待检测数量都不能为负数");
        }
        // 允许"待检"兜住差额（车间先报数、质检后判合格与否），
        // 但不允许合格+不良超出本次报工总数
        BigDecimal sumThree = qualified.add(unqualified).add(uncheck);
        if (sumThree.compareTo(thisQty) != 0) {
            throw new BusinessException("数量对不上：本次报工 " + strip(thisQty)
                    + "，而合格 " + strip(qualified) + " + 不良 " + strip(unqualified)
                    + " + 待检 " + strip(uncheck) + " = " + strip(sumThree));
        }

        // ---- 4. 组装报工记录：冗余字段一律从任务派生，不信前端 ----
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime feedbackTime = dto.getFeedbackTime() == null ? now : dto.getFeedbackTime();

        ProFeedback entity = new ProFeedback();
        entity.setTaskId(task.getTaskId());
        entity.setTaskCode(task.getTaskCode());
        entity.setWorkorderId(task.getWorkorderId());
        entity.setWorkorderCode(task.getWorkorderCode());
        entity.setWorkorderName(task.getWorkorderName());
        entity.setRouteId(task.getRouteId());
        entity.setRouteCode(task.getRouteCode());
        entity.setProcessId(task.getProcessId());
        entity.setProcessCode(task.getProcessCode());
        entity.setProcessName(task.getProcessName());
        entity.setItemId(task.getItemId());
        entity.setItemCode(task.getItemCode());
        entity.setItemName(task.getItemName());
        entity.setSpecification(task.getSpecification());
        entity.setUnitOfMeasure(task.getUnitOfMeasure());
        entity.setUnitName(task.getUnitName());
        // 工作站取任务上排产时指定的那一个，不由报工人另选
        // （排产时定的工位就是实际干活的工位，报工再改会导致追溯错位）
        entity.setWorkstationId(task.getWorkstationId());
        entity.setWorkstationCode(task.getWorkstationCode());
        entity.setWorkstationName(task.getWorkstationName());

        entity.setFeedbackType(dto.getFeedbackType() == null || dto.getFeedbackType().isEmpty()
                ? FEEDBACK_TYPE_PROCESS : dto.getFeedbackType());
        entity.setFeedbackChannel(dto.getFeedbackChannel() == null || dto.getFeedbackChannel().isEmpty()
                ? CHANNEL_PC : dto.getFeedbackChannel());
        // 报工编号在这里不生成流水号，交给调用方或后续编码规则；先置 null 由数据库存空
        entity.setQuantity(scheduled);
        entity.setQuantityFeedback(thisQty);
        entity.setQuantityQualified(qualified);
        entity.setQuantityUnqualified(unqualified);
        entity.setQuantityUncheck(uncheck);
        entity.setLotNumber(dto.getLotNumber());
        entity.setFeedbackTime(feedbackTime);
        entity.setStatus(FEEDBACK_SUBMITTED);
        entity.setRemark(dto.getRemark());
        entity.setUserName(DEFAULT_OPERATOR);
        entity.setNickName(DEFAULT_OPERATOR);
        entity.setRecordUser(DEFAULT_OPERATOR);
        entity.setRecordNick(DEFAULT_OPERATOR);
        entity.setCreateBy(DEFAULT_OPERATOR);
        entity.setCreateTime(now);
        entity.setUpdateBy(DEFAULT_OPERATOR);
        entity.setUpdateTime(now);

        proFeedbackMapper.insert(entity);

        // 报工编号：用自增主键拼，保证唯一又不用额外查编码规则表
        entity.setFeedbackCode(buildFeedbackCode(entity.getRecordId(), feedbackTime));
        proFeedbackMapper.updateFeedbackCode(entity.getRecordId(), entity.getFeedbackCode());

        // ---- 5. 回写任务数量与状态（唯一出口）----
        BigDecimal taskAfter = already.add(thisQty);
        // 数量报满即完工；否则首次报工转"生产中"
        String targetStatus;
        LocalDateTime finishDate = null;
        if (taskAfter.compareTo(scheduled) >= 0) {
            targetStatus = TASK_FINISHED;
            finishDate = feedbackTime;
        } else if (TASK_NORMAL.equals(task.getStatus())) {
            targetStatus = TASK_WORKING;
        } else {
            targetStatus = task.getStatus();
        }
        applyProducedQuantity(task.getTaskId(), thisQty, qualified, unqualified,
                targetStatus, finishDate, now);

        // ---- 6. 该工单全部任务完工 → 累加工单已生产数量 ----
        boolean workorderFinished = false;
        if (TASK_FINISHED.equals(targetStatus)) {
            int unfinished = proTaskMapper.countUnfinishedByWorkorderId(task.getWorkorderId());
            if (unfinished == 0) {
                // 全部工序都干完了，工单的已生产数量加上本次完工批次的产量
                proWorkorderMapper.addProducedQuantity(task.getWorkorderId(), scheduled, DEFAULT_OPERATOR);
                workorderFinished = true;
            }
        }

        // ---- 7. 推进流转卡（同一事务内）----
        // 过站产出 = 合格 + 不良；待检品还没判定，不能流入下一道，
        // 物理上它就停在本道工序等质检 —— 这正是"待检数推给质量模块"的过站表现。
        proCardService.advanceOnFeedback(task.getWorkorderId(), task.getProcessId(),
                qualified.add(unqualified), unqualified, feedbackTime,
                entity.getUserName(), entity.getNickName());

        // ---- 8. 生成物料消耗（同一事务内）----
        // 倒冲基数刻意取"本次报工总量"（合格 + 不良 + 待检），与流转卡口径不同：
        //   流转卡管"活能不能往下走"—— 待检品没判定，只能停在本道；
        //   消耗管"料有没有被吃掉"—— 待检品同样已经加工完，料也已经用掉了。
        // 两个口径分开是有意的，混成一个反而两边都说不清。
        int consumeRows = proTransConsumeService.generateOnFeedback(
                task, thisQty, feedbackTime, entity.getUserName(), entity.getRecordId());

        // ---- 9. 返回摘要，前端据此刷新 ----
        ProTask latest = proTaskMapper.selectById(task.getTaskId());
        ProWorkorder workorder = proWorkorderMapper.selectById(task.getWorkorderId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordId", entity.getRecordId());
        result.put("feedbackCode", entity.getFeedbackCode());
        result.put("taskStatus", latest == null ? null : latest.getStatus());
        result.put("taskProduced", latest == null ? null : latest.getQuantityProduced());
        result.put("taskRemain", latest == null ? BigDecimal.ZERO
                : nvl(latest.getQuantity()).subtract(nvl(latest.getQuantityProduced())));
        result.put("workorderProduced", workorder == null ? null : workorder.getQuantityProduced());
        result.put("workorderFinished", workorderFinished);
        // 本次倒冲生成的物料消耗条数，前端提示"同时扣了 N 条用料"
        result.put("consumeRows", consumeRows);
        return result;
    }

    // ============================================================
    // 私有方法
    // ============================================================

    /**
     * 数量台账的唯一出口：累计任务的已生产/合格/不良数量并推进状态。
     *
     * 所有会影响 pro_task 数量与状态的报工逻辑都必须走这里，
     * 不许在别处直接调 mapper 改这几个字段 —— 单出口才好维护一致性。
     */
    private void applyProducedQuantity(Long taskId, BigDecimal produced, BigDecimal qualified,
                                       BigDecimal unqualified, String status,
                                       LocalDateTime finishDate, LocalDateTime now) {
        proTaskMapper.addProducedQuantity(taskId, produced, qualified, unqualified,
                status, finishDate, DEFAULT_OPERATOR, now);
    }

    /** 报工编号：FB + yyyyMMdd + 6 位主键，可读且唯一 */
    private String buildFeedbackCode(Long recordId, LocalDateTime time) {
        String date = String.format("%04d%02d%02d", time.getYear(), time.getMonthValue(), time.getDayOfMonth());
        return "FB" + date + String.format("%06d", recordId == null ? 0L : recordId);
    }

    /** null 视作 0，避免到处写判空 */
    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** 去掉 BigDecimal 无意义的尾零，让报错信息里的数字好看点 */
    private String strip(BigDecimal v) {
        if (v == null) {
            return "0";
        }
        return v.stripTrailingZeros().toPlainString();
    }

    // ============================================================
    // 报工冲销（红冲）—— 报错了怎么撤
    // ============================================================
    //
    // 为什么不直接删掉那条报工：
    //   报工一旦提交，就连带改了四样东西 —— 任务数量、工单数量、物料消耗、流转卡过站。
    //   把报工行删了，这四样就变成"没有来源"的孤儿：产量挂在账上却查不到是哪次报的、
    //   料扣了却不知道为谁扣的。所以冲销的做法是把这些影响**一起回退**，
    //   本行状态置为 REVERSED 并留下"谁在什么时候为什么冲的"，痕迹永远在。
    //
    // 回退顺序刻意跟报工时的推进顺序**反过来**：
    //   报工：写报工 -> 任务数量 -> 工单产量 -> 流转卡 -> 物料消耗
    //   冲销：报工留痕 -> 任务数量 -> 工单产量 -> 流转卡 -> 物料消耗
    //   顺序本身不影响事务原子性（要么全成要么全滚），但对排查问题更直观。
    //
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> reverse(Long id, String reason) {
        // ---- 1. 报工必须存在，且没被冲销过 ----
        if (id == null) {
            throw new BusinessException("请选择要冲销的报工记录");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("请填写冲销原因（冲销必须留痕，事后要能回答\"为什么撤了\"）");
        }
        ProFeedback feedback = proFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("报工记录不存在或已被删除");
        }
        if (FEEDBACK_REVERSED.equals(feedback.getStatus())) {
            throw new BusinessException("这条报工已经冲销过了，不能重复冲销");
        }

        // ---- 2. 任务必须还在 ----
        ProTask task = proTaskMapper.selectById(feedback.getTaskId());
        if (task == null) {
            throw new BusinessException("报工对应的生产任务不存在或已被删除，无法冲销");
        }

        BigDecimal thisQty = nvl(feedback.getQuantityFeedback());
        BigDecimal produced = nvl(task.getQuantityProduced());

        // ---- 3. 账要对得上：任务累计产量不能小于本次要冲回的量 ----
        // 对不上说明有人绕过报工改过数量，或者这条报工的账已经被别的操作动过。
        // 这种情况直接拒绝，让人先查清楚，而不是硬扣成一个负数。
        if (produced.compareTo(thisQty) < 0) {
            throw new BusinessException("账对不上：任务累计已生产 " + strip(produced)
                    + "，小于本次要冲销的 " + strip(thisQty) + "。请先核对这个任务的报工记录");
        }

        // ---- 4. 下游已经开工就不许冲销（L3：不能让下游产出变成没有来源的数据） ----
        // 下一道已经产出成品，说明这批料确实流下去了。此时回退本道，下一道的产出
        // 就成了"凭空出现"的账。正确做法是从最后一道往前一道一道冲。
        ProCardProcess nextRow = proCardService.queryNextCardProcess(
                feedback.getWorkorderId(), feedback.getProcessId());
        if (nextRow != null && nvl(nextRow.getQuantityOutput()).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("下一道工序（" + nextRow.getProcessName() + "）已经产出 "
                    + strip(nextRow.getQuantityOutput()) + " 件，不能冲销本道报工。"
                    + "请先冲销下游工序的报工，再回来冲这一道");
        }

        LocalDateTime now = LocalDateTime.now();

        // ---- 5. 报工状态置 REVERSED 并留痕（不删行） ----
        // 更新条件里带 status != 'REVERSED'，所以返回 0 就说明并发下已被别人冲销
        int affected = proFeedbackMapper.reverseById(id, reason.trim(), DEFAULT_OPERATOR, now);
        if (affected == 0) {
            throw new BusinessException("冲销失败：这条报工可能刚被其他人冲销，请刷新后重试");
        }

        // ---- 6. 任务数量减回（走唯一出口，传负数即可，不必另写一套减法） ----
        // 状态退回规则：减完还有量就是"生产中"，减到 0 就退回"待生产"。
        // 完工时间一律清空（finishDate 传 null，SQL 里是无条件赋值）——
        // 一条不再完工的任务不该留着完工时间。
        BigDecimal afterProduced = produced.subtract(thisQty);
        String targetStatus = afterProduced.compareTo(BigDecimal.ZERO) <= 0
                ? TASK_NORMAL : TASK_WORKING;
        applyProducedQuantity(task.getTaskId(), thisQty.negate(),
                nvl(feedback.getQuantityQualified()).negate(),
                nvl(feedback.getQuantityUnqualified()).negate(),
                targetStatus, null, now);

        // ---- 7. 工单产量扣回与状态退回 ----
        // 只有"这个任务原本已完工 + 工单当前确实是已完工"才需要处理。
        // 用工单状态当开关是有讲究的：工单的已生产数量只在完工那一刻加过一次，
        // 也只有退回那一刻该扣一次。不这么判，连冲两条报工就会把工单产量扣两次。
        boolean workorderReverted = false;
        if (TASK_FINISHED.equals(task.getStatus())) {
            ProWorkorder workorder = proWorkorderMapper.selectById(task.getWorkorderId());
            if (workorder != null && WORKORDER_FINISHED.equals(workorder.getStatus())) {
                // 加减用的是同一个口径：这次报工当初让工单完工时加的就是这条任务的排产数量
                BigDecimal woQty = nvl(task.getQuantity());
                if (nvl(workorder.getQuantityProduced()).compareTo(woQty) < 0) {
                    throw new BusinessException("账对不上：工单已生产 "
                            + strip(workorder.getQuantityProduced()) + "，小于要扣回的 "
                            + strip(woQty) + "。请先核对工单下的报工记录");
                }
                proWorkorderMapper.addProducedQuantity(task.getWorkorderId(),
                        woQty.negate(), DEFAULT_OPERATOR);
                // 状态退回已下达，完工时间一并清掉（updateStatus 里 finish_date 是无条件赋值）
                proWorkorderMapper.updateStatus(task.getWorkorderId(), WORKORDER_CONFIRMED,
                        null, null, DEFAULT_OPERATOR);
                workorderReverted = true;
            }
        }

        // ---- 8. 回退流转卡过站（本道产出减回 + 下一道投入减回） ----
        // 产出口径与报工时严格一致：合格 + 不良（待检不算产出，它没流过站）
        proCardService.revertOnFeedback(feedback.getWorkorderId(), feedback.getProcessId(),
                nvl(feedback.getQuantityQualified()).add(nvl(feedback.getQuantityUnqualified())),
                nvl(feedback.getQuantityUnqualified()), DEFAULT_OPERATOR);

        // ---- 9. 回退物料消耗（按 feedback_id 精确删掉这次倒冲出来的用料） ----
        int consumeRows = proTransConsumeService.removeByFeedbackId(id);

        // ---- 10. 返回摘要，前端据此刷新 ----
        ProTask latestTask = proTaskMapper.selectById(task.getTaskId());
        ProWorkorder latestWo = proWorkorderMapper.selectById(task.getWorkorderId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordId", id);
        result.put("feedbackCode", feedback.getFeedbackCode());
        result.put("reversedQuantity", thisQty);
        result.put("revertConsumeRows", consumeRows);
        result.put("taskStatus", latestTask == null ? null : latestTask.getStatus());
        result.put("taskProduced", latestTask == null ? null : latestTask.getQuantityProduced());
        result.put("taskRemain", latestTask == null ? null
                : nvl(latestTask.getQuantity()).subtract(nvl(latestTask.getQuantityProduced())));
        result.put("workorderStatus", latestWo == null ? null : latestWo.getStatus());
        result.put("workorderProduced", latestWo == null ? null : latestWo.getQuantityProduced());
        result.put("workorderReverted", workorderReverted);
        return result;
    }

    /**
     * 逻辑删除报工 —— 刻意封掉，统一走冲销
     *
     * 报工只要提交成功，就一定回写过任务数量、生成过物料消耗（第 5、8 步是无条件执行的），
     * 所以"从没产生过影响的报工"根本不存在。直接删行只会留下一堆对不上账的孤儿数据，
     * 所以这里不做删除，而是引导调用方走冲销。
     */
    @Override
    public int deleteById(Long id) {
        ProFeedback feedback = proFeedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("报工记录不存在或已被删除");
        }
        if (FEEDBACK_REVERSED.equals(feedback.getStatus())) {
            throw new BusinessException("这条报工已经冲销过了，不需要再删除");
        }
        throw new BusinessException("报工提交后已回写任务与工单数量、并生成物料消耗，"
                + "不能直接删除。如需撤销请使用「冲销」功能");
    }
}
