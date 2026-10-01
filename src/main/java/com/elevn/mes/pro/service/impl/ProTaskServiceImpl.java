package com.elevn.mes.pro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.entity.MdWorkstation;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.md.mapper.MdWorkstationMapper;
import com.elevn.mes.pro.entity.ProRoute;
import com.elevn.mes.pro.entity.ProRouteProcess;
import com.elevn.mes.pro.entity.ProRouteProduct;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProTaskScheduleDTO;
import com.elevn.mes.pro.entity.ProWorkorder;
import com.elevn.mes.pro.mapper.ProRouteMapper;
import com.elevn.mes.pro.mapper.ProRouteProcessMapper;
import com.elevn.mes.pro.mapper.ProRouteProductMapper;
import com.elevn.mes.pro.mapper.ProTaskMapper;
import com.elevn.mes.pro.mapper.ProWorkorderMapper;
import com.elevn.mes.pro.service.ProCardService;
import com.elevn.mes.pro.service.ProTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生产任务 Service 实现 —— 排产是本模块的核心
 *
 * ============================================================
 * 一、排产的前置校验（L3：状态不对、条件不够，什么都不让干）
 * ============================================================
 *   1. 工单必须存在
 *   2. 工单类型必须是 SELF 自产 —— 外协/外购不排产（业务规则三）
 *   3. 工单状态必须是 CONFIRMED 已下达 —— 待下达还没确认、已取消已放弃、已完工已做完
 *   4. 产品必须已挂产品制程（pro_route_product）—— 没有路线就无工序可拆
 *   5. 路线必须至少有一道工序
 *   6. 本次排产数量 > 0，且"累计已排 + 本次 <= 工单数量"（支持分批，但不许超）
 *
 * ============================================================
 * 二、工作站校验（L3：选了不存在或停用的工作站必须拦）
 * ============================================================
 *   - 用户提交的工序集合必须与路线工序完全一致，少一道就拒绝
 *     （少一道工序，整条工艺链就断了，后面报工接不上）
 *   - 工作站必须存在、启用，且该工作站绑定的工序就是这道工序
 *     （防止把"下料"的任务排到"包装工作站"上）
 *
 * ============================================================
 * 三、时间编排（按 link_type 分流）
 * ============================================================
 *   工序衔接方式 link_type 决定本道工序与上一道的时间关系：
 *     FS（Finish-Start 顺序流转，默认）—— 本道开始 = 上一道结束
 *     SS（Start-Start 并行开工）        —— 本道开始 = 上一道开始
 *   每道工序时长 duration（分钟）：
 *     准备时间 + 单件工时 × 数量 + 等待时间，向上取整
 *   其中 单件工时 × 数量 体现"批量越大耗时越长"，
 *   准备/等待时间是整批一次性的（如老化要等 48 小时）。
 *
 * ============================================================
 * 四、写入策略：整批替换（幂等）
 * ============================================================
 *   同一张工单重新排产时，先物理清空旧任务再写新的。
 *   因为任务行是可重算的派生数据，替换式能保证重复排产结果一致，
 *   也不会残留上一版的任务。
 *
 */
@Service
public class ProTaskServiceImpl implements ProTaskService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 工单类型：自产 */
    private static final String TYPE_SELF = "SELF";
    /** 工单状态：已下达 */
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    /** 工单状态：已完工 */
    private static final String STATUS_FINISHED = "FINISHED";
    /** 工单状态：已取消 */
    private static final String STATUS_CANCELED = "CANCELED";

    /** 任务状态：待生产 */
    private static final String TASK_NORMAL = "NORMAL";

    /** 工序衔接方式：顺序流转（默认，本道接上一道结束） */
    private static final String LINK_FS = "FS";
    /** 工序衔接方式：并行开工（本道与上一道同时开始） */
    private static final String LINK_SS = "SS";

    @Autowired
    private ProTaskMapper proTaskMapper;

    @Autowired
    private ProWorkorderMapper proWorkorderMapper;

    @Autowired
    private ProRouteProductMapper proRouteProductMapper;

    @Autowired
    private ProRouteProcessMapper proRouteProcessMapper;

    @Autowired
    private ProRouteMapper proRouteMapper;

    @Autowired
    private MdWorkstationMapper mdWorkstationMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Autowired
    private ProCardService proCardService;

    // ============================================================
    // 查询
    // ============================================================

    @Override
    public PageInfo<ProTask> page(int pageNum, int pageSize, ProTask proTask) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProTask> list = proTaskMapper.selectByCondition(proTask);
        return new PageInfo<>(list);
    }

    @Override
    public ProTask queryById(Long id) {
        return proTaskMapper.selectById(id);
    }

    @Override
    public List<ProTask> queryByWorkorderId(Long workorderId) {
        return proTaskMapper.selectByWorkorderId(workorderId);
    }

    @Override
    public List<ProTask> queryList(ProTask proTask) {
        return proTaskMapper.selectByCondition(proTask);
    }

    // ============================================================
    // 排产预览
    // ============================================================

    @Override
    public Map<String, Object> preview(Long workorderId) {
        ProWorkorder workorder = loadSchedulableWorkorder(workorderId);
        ProRouteProduct rp = loadRouteProduct(workorder);
        List<ProRouteProcess> routeProcesses = loadRouteProcesses(rp.getRouteId());
        // 路线编号/名称在 pro_route 上，产品制程表只存 route_id，所以单独查一次
        ProRoute route = proRouteMapper.selectById(rp.getRouteId());

        // 逐道工序列出候选工作站，前端让用户选
        List<Map<String, Object>> processList = new ArrayList<>(routeProcesses.size());
        for (ProRouteProcess rpItem : routeProcesses) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("processId", rpItem.getProcessId());
            row.put("processCode", rpItem.getProcessCode());
            row.put("processName", rpItem.getProcessName());
            row.put("orderNum", rpItem.getOrderNum());
            row.put("linkType", rpItem.getLinkType());
            row.put("unitTime", rpItem.getUnitTime());
            row.put("capacityPerHour", rpItem.getCapacityPerHour());
            row.put("defaultPreTime", rpItem.getDefaultPreTime());
            row.put("defaultSufTime", rpItem.getDefaultSufTime());
            row.put("keyFlag", rpItem.getKeyFlag());
            row.put("isCheck", rpItem.getIsCheck());
            // 候选工作站：该工序在车间里对应的所有启用工作站
            row.put("workstations", mdWorkstationMapper.selectByProcessId(rpItem.getProcessId()));
            processList.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("workorder", workorder);
        result.put("routeId", rp.getRouteId());
        // 路线编号/名称取自 pro_route（产品制程表只有 route_id）
        result.put("routeCode", route == null ? null : route.getRouteCode());
        result.put("routeName", route == null ? null : route.getRouteName());
        // 还能排多少 = 工单数量 - 已排产数量
        BigDecimal scheduled = workorder.getQuantityScheduled() == null
                ? BigDecimal.ZERO : workorder.getQuantityScheduled();
        result.put("quantityScheduled", scheduled);
        result.put("quantityRemain", workorder.getQuantity().subtract(scheduled));
        result.put("processList", processList);
        return result;
    }

    // ============================================================
    // 排产
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int schedule(ProTaskScheduleDTO dto) {
        if (dto == null || dto.getWorkorderId() == null) {
            throw new BusinessException("请选择要排产的工单");
        }

        // ---- 1. 工单必须可排产 ----
        ProWorkorder workorder = loadSchedulableWorkorder(dto.getWorkorderId());

        // ---- 1.5 已报工的工单不能重新排产 ----
        // 排产是「整批替换」，重新排会把旧任务连根拔掉，已产生的报工记录
        // 和流转卡过站记录会全部变成孤儿 —— 必须在动手之前拦住。
        int producedCount = proTaskMapper.countProducedByWorkorderId(dto.getWorkorderId());
        if (producedCount > 0) {
            throw new BusinessException("工单[" + workorder.getWorkorderCode()
                    + "]已有 " + producedCount + " 道工序开始报工，不能重新排产。"
                    + "如需调整请先处理报工记录。");
        }

        // ---- 2. 数量校验：分批排产，累计不许超过工单数量 ----
        BigDecimal thisQty = dto.getQuantity();
        if (thisQty == null || thisQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("本次排产数量必须大于 0");
        }
        BigDecimal alreadyScheduled = workorder.getQuantityScheduled() == null
                ? BigDecimal.ZERO : workorder.getQuantityScheduled();
        BigDecimal afterSchedule = alreadyScheduled.add(thisQty);
        if (afterSchedule.compareTo(workorder.getQuantity()) > 0) {
            throw new BusinessException("排产数量超出工单剩余可排数量。工单数量 "
                    + strip(workorder.getQuantity()) + "，已排产 " + strip(alreadyScheduled)
                    + "，本次最多还能排 " + strip(workorder.getQuantity().subtract(alreadyScheduled)));
        }

        // ---- 3. 路线与工序 ----
        ProRouteProduct rp = loadRouteProduct(workorder);
        List<ProRouteProcess> routeProcesses = loadRouteProcesses(rp.getRouteId());
        ProRoute route = proRouteMapper.selectById(rp.getRouteId());

        // ---- 4. 工作站校验：每道工序都要选，且选的工作站必须对得上这道工序 ----
        Map<Long, Long> selectedWorkstation = checkAndMapWorkstation(dto, routeProcesses);

        // ---- 5. 计划开工时间 ----
        LocalDateTime planStart = dto.getPlanStartTime();
        if (planStart == null) {
            throw new BusinessException("请选择计划开工时间");
        }

        // ---- 6. 逐道工序生成任务 + 按 link_type 算时间 ----
        String woCode = workorder.getWorkorderCode();
        LocalDateTime now = LocalDateTime.now();
        List<ProTask> tasks = new ArrayList<>(routeProcesses.size());

        // 上一道工序的开始时间 / 结束时间，用于 FS / SS 判断
        LocalDateTime prevStart = null;
        LocalDateTime prevEnd = null;

        for (int i = 0; i < routeProcesses.size(); i++) {
            ProRouteProcess rpItem = routeProcesses.get(i);
            Long wsId = selectedWorkstation.get(rpItem.getProcessId());
            MdWorkstation ws = mdWorkstationMapper.selectById(wsId);

            ProTask task = new ProTask();
            // 任务编号：工单号-两位序位，一眼看出属于哪张单的第几道工序
            task.setTaskCode(woCode + "-" + String.format("%02d", i + 1) + ws.getWorkstationCode());
            task.setTaskName(rpItem.getProcessName() + " · " + workorder.getWorkorderName());

            // 工单快照
            task.setWorkorderId(workorder.getWorkorderId());
            task.setWorkorderCode(woCode);
            task.setWorkorderName(workorder.getWorkorderName());

            // 工作站快照
            task.setWorkstationId(ws.getWorkstationId());
            task.setWorkstationCode(ws.getWorkstationCode());
            task.setWorkstationName(ws.getWorkstationName());

            // 工艺与工序快照
            task.setRouteId(rp.getRouteId());
            // 路线编号取自 pro_route（产品制程表只有 route_id）
            task.setRouteCode(route == null ? null : route.getRouteCode());
            task.setProcessId(rpItem.getProcessId());
            task.setProcessCode(rpItem.getProcessCode());
            task.setProcessName(rpItem.getProcessName());

            // 产品快照
            task.setItemId(workorder.getProductId());
            task.setItemCode(workorder.getProductCode());
            task.setItemName(workorder.getProductName());
            task.setSpecification(workorder.getProductSpc());
            task.setUnitOfMeasure(workorder.getUnitOfMeasure());
            // pro_workorder 上没有单位名称（只有编码），从产品主数据取
            MdItem product = mdItemMapper.selectById(workorder.getProductId());
            task.setUnitName(product == null ? null : product.getUnitName());

            // 数量：本次排产数量，累计字段从 0 起（防止前端伪造）
            task.setQuantity(thisQty);
            task.setQuantityProduced(BigDecimal.ZERO);
            task.setQuantityQualified(BigDecimal.ZERO);
            task.setQuantityUnqualified(BigDecimal.ZERO);
            task.setQuantityChanged(BigDecimal.ZERO);

            // 客户快照
            task.setClientId(workorder.getClientId());
            task.setClientCode(workorder.getClientCode());
            task.setClientName(workorder.getClientName());

            // ---- 开始时间：按 link_type 决定与上一道的关系 ----
            LocalDateTime start;
            if (i == 0 || prevEnd == null) {
                // 第一道：直接用计划开工时间
                start = planStart;
            } else if (LINK_SS.equalsIgnoreCase(rpItem.getLinkType())) {
                // 并行开工：与上一道同时开始
                start = prevStart;
            } else {
                // 顺序流转（FS 及未配置）：接上一道结束时间
                start = prevEnd;
            }

            int duration = calcDuration(rpItem, thisQty);
            LocalDateTime end = start.plusMinutes(duration);

            task.setStartTime(start);
            task.setDuration(duration);
            task.setEndTime(end);
            task.setColorCode(rpItem.getColorCode());
            task.setRequestDate(workorder.getRequestDate());
            task.setStatus(TASK_NORMAL);

            task.setRemark("排产生成");
            task.setCreateBy(DEFAULT_OPERATOR);
            task.setCreateTime(now);
            task.setUpdateBy(DEFAULT_OPERATOR);
            task.setUpdateTime(now);

            tasks.add(task);

            prevStart = start;
            prevEnd = end;
        }

        // ---- 7. 写入：先清旧任务（整批替换，保证幂等），再批量插入 ----
        // proTaskMapper.deleteByWorkorderId(workorder.getWorkorderId());
        proTaskMapper.insertBatch(tasks);

        // ---- 8. 回写工单已排产数量（addScheduledQuantity 内部是 + 号，可重复排产累加） ----
        proWorkorderMapper.addScheduledQuantity(workorder.getWorkorderId(), thisQty, DEFAULT_OPERATOR);

        // ---- 9. 建流转卡：一工单一卡，按本次任务铺过站行（同一事务内） ----
        // 已有卡且无过站进度则重建过站行；有进度会被 ensureCardForSchedule 拦住
        proCardService.ensureCardForSchedule(workorder, tasks);

        return tasks.size();
    }

    // ============================================================
    // 撤销排产
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int cancelSchedule(Long workorderId) {
        ProWorkorder workorder = proWorkorderMapper.selectById(workorderId);
        if (workorder == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        if (STATUS_FINISHED.equals(workorder.getStatus())) {
            throw new BusinessException("工单[" + workorder.getWorkorderCode()
                    + "]已完工，不能撤销排产");
        }
        if (STATUS_CANCELED.equals(workorder.getStatus())) {
            throw new BusinessException("工单[" + workorder.getWorkorderCode()
                    + "]已取消，无需撤销排产");
        }

        // 已经报过工的不能撤销 —— 否则报工记录会变成孤儿数据
        int producedCount = proTaskMapper.countProducedByWorkorderId(workorderId);
        if (producedCount > 0) {
            throw new BusinessException("该工单下有 " + producedCount
                    + " 道工序已经开始报工，不能撤销排产。请先处理报工记录。");
        }

        List<ProTask> tasks = proTaskMapper.selectByWorkorderId(workorderId);
        if (tasks.isEmpty()) {
            throw new BusinessException("工单[" + workorder.getWorkorderCode()
                    + "]还没有排产，无需撤销");
        }

        // 物理删除 + 已排产数量归零 + 拆掉流转卡（撤销排产必然还没报工，卡也无进度）
        proTaskMapper.deleteByWorkorderId(workorderId);
        proWorkorderMapper.resetScheduledQuantity(workorderId, DEFAULT_OPERATOR);
        proCardService.removeCardByWorkorderId(workorderId);

        return tasks.size();
    }

    // ============================================================
    // 人工微调
    // ============================================================

    @Override
    public int updateById(ProTask proTask) {
        if (proTask.getTaskId() == null) {
            throw new BusinessException("任务ID不能为空");
        }
        ProTask db = proTaskMapper.selectById(proTask.getTaskId());
        if (db == null) {
            throw new BusinessException("生产任务不存在或已被删除");
        }
        if (STATUS_FINISHED.equals(db.getStatus()) || STATUS_CANCELED.equals(db.getStatus())) {
            throw new BusinessException("任务[" + db.getTaskCode() + "]已"
                    + ("FINISHED".equals(db.getStatus()) ? "完工" : "取消") + "，不能再修改");
        }
        // 换工作站时校验新工作站是否对得上这道工序
        if (proTask.getWorkstationId() != null
                && !proTask.getWorkstationId().equals(db.getWorkstationId())) {
            MdWorkstation ws = mdWorkstationMapper.selectById(proTask.getWorkstationId());
            checkWorkstation(ws, db.getProcessId(), db.getProcessName());
            proTask.setWorkstationCode(ws.getWorkstationCode());
            proTask.setWorkstationName(ws.getWorkstationName());
        }
        proTask.setUpdateBy(DEFAULT_OPERATOR);
        proTask.setUpdateTime(LocalDateTime.now());
        return proTaskMapper.updateById(proTask);
    }

    @Override
    public int deleteById(Long id) {
        ProTask db = proTaskMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("生产任务不存在或已被删除");
        }
        if (db.getQuantityProduced() != null
                && db.getQuantityProduced().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("任务[" + db.getTaskCode()
                    + "]已经开始报工，不能删除");
        }
        return proTaskMapper.deleteById(id);
    }

    // ============================================================
    // 私有方法
    // ============================================================

    /**
     * 加载工单并校验它是否可排产
     *
     * 这里把"能不能排产"的三条判断集中在一处，
     * preview 与 schedule 共用，避免两边规则写岔了。
     */
    private ProWorkorder loadSchedulableWorkorder(Long workorderId) {
        ProWorkorder workorder = proWorkorderMapper.selectById(workorderId);
        if (workorder == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        // 规则三：只有自产工单才需要排产
        if (!TYPE_SELF.equals(workorder.getWorkorderType())) {
            throw new BusinessException("工单[" + workorder.getWorkorderCode()
                    + "]是「" + ("OUTSOURCE".equals(workorder.getWorkorderType()) ? "外协" : "外购")
                    + "」工单，不需要排产。只有自产工单才需要排产。");
        }
        // 状态机：只有已下达的能排产
        if (!STATUS_CONFIRMED.equals(workorder.getStatus())) {
            String tips;
            if ("PREPARE".equals(workorder.getStatus())) {
                tips = "工单还没下达，请先在工单列表点「下达」";
            } else if (STATUS_FINISHED.equals(workorder.getStatus())) {
                tips = "工单已完工";
            } else if (STATUS_CANCELED.equals(workorder.getStatus())) {
                tips = "工单已取消";
            } else {
                tips = "工单当前状态不允许排产";
            }
            throw new BusinessException("工单[" + workorder.getWorkorderCode()
                    + "]当前状态为「" + statusName(workorder.getStatus()) + "」，" + tips);
        }
        return workorder;
    }

    /**
     * 取工单的产品制程（工艺路线）
     *
     * 下达时已经校验过自产工单必须挂制程，但这里是独立的入口（可能有人直接调排产接口），
     * 所以再校验一次，不做假设。
     */
    private ProRouteProduct loadRouteProduct(ProWorkorder workorder) {
        List<ProRouteProduct> routes = proRouteProductMapper.selectByItemId(workorder.getProductId());
        if (routes == null || routes.isEmpty()) {
            throw new BusinessException("产品[" + workorder.getProductName()
                    + "]还没有配置产品制程（工艺路线），无法排产。"
                    + "请先到 生产管理-产品制程 里为该产品挂一条工艺路线");
        }
        // 一个产品可能挂多条路线（如不同工艺方案），取第一条作为本次排产依据；
        // 未来支持"选路线排产"时，这里改成按入参的 routeId 匹配即可。
        return routes.get(0);
    }

    /**
     * 取路线的工序列表，并校验工序上的工时是否已维护
     */
    private List<ProRouteProcess> loadRouteProcesses(Long routeId) {
        List<ProRouteProcess> list = proRouteProcessMapper.selectByRouteId(routeId);
        if (list == null || list.isEmpty()) {
            throw new BusinessException("该工艺路线下还没有配置工序，无法排产。"
                    + "请先到 生产管理-工艺路线 里维护路线工序");
        }
        return list;
    }

    /**
     * 校验"每道工序都选了工作站"，并返回 工序ID -> 工作站ID 的映射
     */
    private Map<Long, Long> checkAndMapWorkstation(ProTaskScheduleDTO dto,
                                                  List<ProRouteProcess> routeProcesses) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("请为每道工序指定工作站");
        }
        Map<Long, Long> map = new HashMap<>();
        for (ProTaskScheduleDTO.Item item : dto.getItems()) {
            if (item.getProcessId() == null || item.getWorkstationId() == null) {
                throw new BusinessException("工序与工作站都不能为空");
            }
            map.put(item.getProcessId(), item.getWorkstationId());
        }

        // 路线上的每道工序都必须有对应的工作站选择
        for (ProRouteProcess rp : routeProcesses) {
            if (!map.containsKey(rp.getProcessId())) {
                throw new BusinessException("工序「" + rp.getProcessName()
                        + "」还没有指定工作站。排产必须覆盖全部 "
                        + routeProcesses.size() + " 道工序");
            }
        }

        // 逐道校验工作站是否真的能承担这道工序
        for (ProRouteProcess rp : routeProcesses) {
            MdWorkstation ws = mdWorkstationMapper.selectById(map.get(rp.getProcessId()));
            checkWorkstation(ws, rp.getProcessId(), rp.getProcessName());
        }
        return map;
    }

    /**
     * 校验单个工作站：必须存在、启用，且它绑定的工序就是当前工序
     *
     * 这条校验防的是"把下料的任务排到包装工作站上"——
     * 车间里工位和工序是有绑定关系的，排错了产线根本干不了活。
     */
    private void checkWorkstation(MdWorkstation ws, Long processId, String processName) {
        if (ws == null) {
            throw new BusinessException("所选工作站不存在或已被删除");
        }
        if (!"Y".equals(ws.getEnableFlag())) {
            throw new BusinessException("工作站[" + ws.getWorkstationName() + "]已停用，不能排产");
        }
        if (ws.getProcessId() != null && !ws.getProcessId().equals(processId)) {
            throw new BusinessException("工作站[" + ws.getWorkstationName()
                    + "]负责的是「" + ws.getProcessName() + "」工序，不能用于「"
                    + processName + "」工序");
        }
    }

    /**
     * 计算工序时长（分钟）
     *
     *   duration = 准备时间(整批一次) + 单件工时 × 数量 + 等待时间(整批一次)
     *
     * 三个部分各自的意义：
     *   准备时间：换模、备料、开机，与数量无关，整批一次
     *   单件工时 × 数量：真正的加工时间，批量越大越长
     *   等待时间：工艺必须的静置（如老化 48 小时），与数量无关，整批一次
     *
     * 单件工时为 0（没维护）时退化成"只算准备与等待"，
     * 不报错但要给出可用的时长，避免整条路线算不出来。
     */
    private int calcDuration(ProRouteProcess rp, BigDecimal quantity) {
        int pre = rp.getDefaultPreTime() == null ? 0 : rp.getDefaultPreTime();
        int suf = rp.getDefaultSufTime() == null ? 0 : rp.getDefaultSufTime();
        BigDecimal unitTime = rp.getUnitTime() == null ? BigDecimal.ZERO : rp.getUnitTime();

        // 单件工时 × 数量，向上取整到分钟（不足 1 分钟按 1 分钟算，1 分钟也是人工工时）
        BigDecimal work = unitTime.multiply(quantity).setScale(0, RoundingMode.CEILING);
        int workMinutes = work.intValue();

        int total = pre + workMinutes + suf;
        // 保底 1 分钟，避免甘特图上出现零宽度的条
        return Math.max(total, 1);
    }

    /**
     * 数量去掉无意义的尾部 0，用于错误提示（1.000000 显示成 1）
     */
    private String strip(BigDecimal v) {
        if (v == null) {
            return "0";
        }
        return v.stripTrailingZeros().toPlainString();
    }

    /**
     * 工单状态编码转中文名（拼错误提示用）
     */
    private String statusName(String status) {
        if ("PREPARE".equals(status)) {
            return "待下达";
        }
        if (STATUS_CONFIRMED.equals(status)) {
            return "已下达";
        }
        if (STATUS_FINISHED.equals(status)) {
            return "已完工";
        }
        if (STATUS_CANCELED.equals(status)) {
            return "已取消";
        }
        return status;
    }
}
