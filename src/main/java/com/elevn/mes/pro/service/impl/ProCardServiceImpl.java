package com.elevn.mes.pro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.pro.entity.ProCard;
import com.elevn.mes.pro.entity.ProCardProcess;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProWorkorder;
import com.elevn.mes.pro.mapper.ProCardMapper;
import com.elevn.mes.pro.mapper.ProCardProcessMapper;
import com.elevn.mes.pro.service.ProCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 工序流转卡 Service 实现
 *
 * 本类自身不做业务校验的主场 —— 建卡/推进/拆卡都是被排产、报工、撤销排产
 * 的事务"顺带"调用的，所以这里只做数据组织与状态推进，
 * 事务边界由调用方（@Transactional REQUIRED 传播）统一保证。
 *
 * 状态机（字典 pro_card_status）：
 *   PENDING --首次过站--> RUNNING --末道产出达到流转数量--> FINISHED
 *
 */
@Service
public class ProCardServiceImpl implements ProCardService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 流转卡状态 */
    private static final String CARD_PENDING = "PENDING";
    private static final String CARD_RUNNING = "RUNNING";
    private static final String CARD_FINISHED = "FINISHED";

    private static final DateTimeFormatter CODE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private ProCardMapper proCardMapper;

    @Autowired
    private ProCardProcessMapper proCardProcessMapper;

    // ============================================================
    // 查询
    // ============================================================

    @Override
    public PageInfo<ProCard> page(int pageNum, int pageSize,
                                  String cardCode, String workorderCode,
                                  String itemName, String status) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProCard> list = proCardMapper.selectByCondition(cardCode, workorderCode, itemName, status);
        return new PageInfo<>(list);
    }

    @Override
    public ProCard queryById(Long cardId) {
        return proCardMapper.selectById(cardId);
    }

    @Override
    public List<ProCardProcess> queryProcessesByCardId(Long cardId) {
        return proCardProcessMapper.selectByCardId(cardId);
    }

    @Override
    public ProCard queryByWorkorderId(Long workorderId) {
        return proCardMapper.selectByWorkorderId(workorderId);
    }

    // ============================================================
    // 排产建卡（挂在排产事务里）
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void ensureCardForSchedule(ProWorkorder workorder, List<ProTask> tasks) {
        ProCard card = proCardMapper.selectByWorkorderId(workorder.getWorkorderId());

        if (card != null) {
            // 已有卡：先看有没有过站进度。有进度说明已经报过工，重新排产会把
            // 过站记录连根拔掉 —— 这类操作必须拦住。
            List<ProCardProcess> rows = proCardProcessMapper.selectByCardId(card.getCardId());
            boolean hasProgress = rows.stream().anyMatch(r ->
                    r.getQuantityOutput() != null && r.getQuantityOutput().compareTo(BigDecimal.ZERO) > 0);
            if (hasProgress) {
                throw new BusinessException("工单[" + workorder.getWorkorderCode()
                        + "]的流转卡已有过站记录（该工单已开始报工），不能重新排产。"
                        + "如需调整请先处理报工记录。");
            }
            // 无进度：整卡重建过站行（工作站可能变了），卡本体保留
            proCardProcessMapper.deleteByCardId(card.getCardId());
        } else {
            // 首次排产：建卡，编号 insert 后按主键回填（与报工单 FB 编号同一套路）
            LocalDateTime now = LocalDateTime.now();
            card = new ProCard();
            card.setWorkorderId(workorder.getWorkorderId());
            card.setWorkorderCode(workorder.getWorkorderCode());
            card.setWorkorderName(workorder.getWorkorderName());
            // 批次号默认取工单编号；B 线批次管理接入后可换成真实批次
            card.setBatchCode(workorder.getWorkorderCode());
            card.setItemId(workorder.getProductId());
            card.setItemCode(workorder.getProductCode());
            card.setItemName(workorder.getProductName());
            card.setSpecification(workorder.getProductSpc());
            card.setUnitOfMeasure(workorder.getUnitOfMeasure());
            card.setQuantityTransferred(workorder.getQuantity());
            card.setStatus(CARD_PENDING);
            card.setRemark("排产生成");
            card.setCreateBy(DEFAULT_OPERATOR);
            card.setCreateTime(now);
            card.setUpdateBy(DEFAULT_OPERATOR);
            card.setUpdateTime(now);
            proCardMapper.insert(card);
            proCardMapper.updateCardCode(card.getCardId(),
                    "LC" + now.format(CODE_FORMAT) + String.format("%06d", card.getCardId()),
                    DEFAULT_OPERATOR, now);
        }

        // 铺工序行：与任务一一对应（tasks 已按路线顺序生成）
        LocalDateTime now = LocalDateTime.now();
        List<ProCardProcess> rows = new ArrayList<>(tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            ProTask task = tasks.get(i);
            ProCardProcess row = new ProCardProcess();
            row.setCardId(card.getCardId());
            row.setCardCode(card.getCardCode());
            row.setSeqNum(i + 1);
            row.setProcessId(task.getProcessId());
            row.setProcessCode(task.getProcessCode());
            row.setProcessName(task.getProcessName());
            // 工作站从排产任务带来；操作工要等报工才知道，user_id 先存 0
            row.setWorkstationId(task.getWorkstationId());
            row.setWorkstationCode(task.getWorkstationCode());
            row.setWorkstationName(task.getWorkstationName());
            row.setUserId(0L);
            row.setRemark("排产铺行");
            row.setCreateBy(DEFAULT_OPERATOR);
            row.setCreateTime(now);
            row.setUpdateBy(DEFAULT_OPERATOR);
            row.setUpdateTime(now);
            rows.add(row);
        }
        proCardProcessMapper.insertBatch(rows);
    }

    // ============================================================
    // 报工推进（挂在报工事务里）
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void advanceOnFeedback(Long workorderId, Long processId,
                                  BigDecimal output, BigDecimal unqualified,
                                  LocalDateTime feedbackTime,
                                  String userName, String nickName) {
        // 历史工单可能没有卡（功能上线前排产的），静默跳过，不影响报工主流程
        ProCard card = proCardMapper.selectByWorkorderId(workorderId);
        if (card == null) {
            return;
        }

        ProCardProcess row = proCardProcessMapper.selectByCardAndProcess(card.getCardId(), processId);
        if (row == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // ---- 1. 本道出站：累计产出 + 不良 + 出站时间 + 操作工 ----
        // user_id 本系统暂无登录上下文，先存 0；账号与昵称照实回填
        proCardProcessMapper.addOutputQuantity(row.getRecordId(), output, unqualified,
                feedbackTime, 0L, userName, nickName, DEFAULT_OPERATOR, now);

        // ---- 2. 下一道进站：投入 = 本道本次产出 ----
        List<ProCardProcess> allRows = proCardProcessMapper.selectByCardId(card.getCardId());
        int idx = indexOfRecord(allRows, row.getRecordId());
        if (idx >= 0 && idx < allRows.size() - 1) {
            ProCardProcess next = allRows.get(idx + 1);
            proCardProcessMapper.addInputQuantity(next.getRecordId(), feedbackTime,
                    output, DEFAULT_OPERATOR, now);
        }

        // ---- 3. 卡状态推进 ----
        // 首次过站：PENDING -> RUNNING
        if (CARD_PENDING.equals(card.getStatus())) {
            proCardMapper.updateStatus(card.getCardId(), CARD_RUNNING, DEFAULT_OPERATOR, now);
        }
        // 末道累计产出达到流转数量：卡完工
        boolean isLast = idx >= 0 && idx == allRows.size() - 1;
        if (isLast && !CARD_FINISHED.equals(card.getStatus())) {
            BigDecimal totalOutput = nvl(row.getQuantityOutput()).add(output);
            if (card.getQuantityTransferred() != null
                    && totalOutput.compareTo(card.getQuantityTransferred()) >= 0) {
                proCardMapper.updateStatus(card.getCardId(), CARD_FINISHED, DEFAULT_OPERATOR, now);
            }
        }
    }

    // ============================================================
    // 撤销排产拆卡（挂在撤销事务里）
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeCardByWorkorderId(Long workorderId) {
        ProCard card = proCardMapper.selectByWorkorderId(workorderId);
        if (card == null) {
            return;
        }
        proCardProcessMapper.deleteByCardId(card.getCardId());
        proCardMapper.deleteByWorkorderId(workorderId);
    }

    // ============================================================
    // 报工冲销回退（挂在冲销事务里，与 advanceOnFeedback 严格对称）
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void revertOnFeedback(Long workorderId, Long processId,
                                 BigDecimal output, BigDecimal unqualified, String userName) {
        // 历史工单可能没有卡，静默跳过，不能让冲销主流程因为没卡而失败
        ProCard card = proCardMapper.selectByWorkorderId(workorderId);
        if (card == null) {
            return;
        }
        ProCardProcess row = proCardProcessMapper.selectByCardAndProcess(card.getCardId(), processId);
        if (row == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        String operator = userName == null || userName.isEmpty() ? DEFAULT_OPERATOR : userName;

        // ---- 1. 本道出站回退：产出/不良减回（减到 0 由 SQL 负责清空出站时间） ----
        proCardProcessMapper.subtractOutputQuantity(row.getRecordId(), output, unqualified,
                operator, now);

        // ---- 2. 下一道进站回退：投入减回 ----
        // 能走到这一步说明调用方已确认下游未开工，所以这里减掉的就是本道当初带过去的量
        List<ProCardProcess> allRows = proCardProcessMapper.selectByCardId(card.getCardId());
        int idx = indexOfRecord(allRows, row.getRecordId());
        if (idx >= 0 && idx < allRows.size() - 1) {
            ProCardProcess next = allRows.get(idx + 1);
            proCardProcessMapper.subtractInputQuantity(next.getRecordId(), output, operator, now);
        }

        // ---- 3. 卡状态回退 ----
        // 按「整卡是否还有产出」判定而不是只看本道：本道可能还有别的批次没冲销。
        // 全卡产出都归零 -> 回到 PENDING（等于还没开工）；否则 -> RUNNING。
        List<ProCardProcess> latestRows = proCardProcessMapper.selectByCardId(card.getCardId());
        boolean anyProgress = latestRows.stream()
                .anyMatch(r -> nvl(r.getQuantityOutput()).compareTo(BigDecimal.ZERO) > 0);
        String targetStatus = anyProgress ? CARD_RUNNING : CARD_PENDING;
        if (!targetStatus.equals(card.getStatus())) {
            proCardMapper.updateStatus(card.getCardId(), targetStatus, operator, now);
        }
    }

    // ============================================================
    // 只读：取某工单某工序的过站行（报工前校验投入量用）
    // ============================================================

    @Override
    public ProCardProcess queryCardProcess(Long workorderId, Long processId) {
        if (workorderId == null || processId == null) {
            return null;
        }
        ProCard card = proCardMapper.selectByWorkorderId(workorderId);
        if (card == null) {
            return null;
        }
        return proCardProcessMapper.selectByCardAndProcess(card.getCardId(), processId);
    }

    @Override
    public ProCardProcess queryNextCardProcess(Long workorderId, Long processId) {
        if (workorderId == null || processId == null) {
            return null;
        }
        ProCard card = proCardMapper.selectByWorkorderId(workorderId);
        if (card == null) {
            return null;
        }
        List<ProCardProcess> allRows = proCardProcessMapper.selectByCardId(card.getCardId());
        for (int i = 0; i < allRows.size(); i++) {
            if (processId.equals(allRows.get(i).getProcessId())) {
                // 末道没有"下一道"，返回 null 表示无下游
                return i < allRows.size() - 1 ? allRows.get(i + 1) : null;
            }
        }
        return null;
    }

    // ============================================================
    // 小工具
    // ============================================================

    /** 在过站行列表里找某条记录的序号，找不到返回 -1 */
    private int indexOfRecord(List<ProCardProcess> rows, Long recordId) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getRecordId().equals(recordId)) {
                return i;
            }
        }
        return -1;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
