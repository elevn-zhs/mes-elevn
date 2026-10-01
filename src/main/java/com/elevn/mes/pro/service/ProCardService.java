package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProCard;
import com.elevn.mes.pro.entity.ProCardProcess;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProWorkorder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工序流转卡 Service
 *
 * 卡的维护只有三个入口，全部与既有事务绑在一起：
 *   1. ensureCardForSchedule —— 排产确认时建卡 + 铺工序行（挂在排产事务里）
 *   2. advanceOnFeedback     —— 报工时推进过站（挂在报工事务里）
 *   3. removeCardByWorkorderId —— 撤销排产时拆卡（挂在撤销事务里）
 *
 * 查询入口独立：page / queryById（详情带过站行）。
 *
 */
public interface ProCardService {

    /** 分页条件查询 */
    PageInfo<ProCard> page(int pageNum, int pageSize,
                           String cardCode, String workorderCode,
                           String itemName, String status);

    /** 详情：卡 + 全部过站行（按序号排序） */
    ProCard queryById(Long cardId);

    /** 按工单查卡（一工单一卡，没有则返回 null） */
    ProCard queryByWorkorderId(Long workorderId);

    /** 详情里的过站行 */
    List<ProCardProcess> queryProcessesByCardId(Long cardId);

    /**
     * 排产建卡：工单没有卡就建一张并按任务铺工序行；已有卡且无过站进度则整卡重建。
     * 由 ProTaskServiceImpl.schedule 在同一事务里调用。
     *
     * @throws BusinessException 已有卡且存在过站进度（说明已报工，不允许重新排产）
     */
    void ensureCardForSchedule(ProWorkorder workorder, List<ProTask> tasks);

    /**
     * 报工推进：本道累计产出/不良 + 出站时间 + 操作工；下一道进站 + 投入。
     * 由 ProFeedbackServiceImpl.feedback 在同一事务里调用。
     * 工单没有卡（历史数据）时静默跳过，保证报工主流程不受影响。
     *
     * @param workorderId     工单ID
     * @param processId       本次报工的工序ID
     * @param output          本次产出（合格 + 不良）
     * @param unqualified     本次不良
     * @param feedbackTime    报工时间（作为过站时间）
     * @param userName        操作工账号
     * @param nickName        操作工昵称
     */
    void advanceOnFeedback(Long workorderId, Long processId,
                           BigDecimal output, BigDecimal unqualified,
                           LocalDateTime feedbackTime,
                           String userName, String nickName);

    /** 撤销排产时拆卡（物理删除卡 + 过站行），由撤销排产事务调用 */
    void removeCardByWorkorderId(Long workorderId);

    /**
     * 报工冲销：反向回退过站进度，由 ProFeedbackServiceImpl.reverse 在同一事务里调用。
     *
     * 与 advanceOnFeedback 严格对称：
     *   本道产出/不良减回（减到 0 清出站时间）
     *   下一道投入减回（减到 0 清进站时间）
     *   卡状态从 FINISHED 退回 RUNNING
     *
     * 调用前必须已确认「下游未开工」——下游已经产出成品还回退本道，
     * 会让下游产出变成没有来源的孤儿数据。校验放在 ProFeedbackServiceImpl。
     *
     * @param workorderId 工单ID
     * @param processId   被冲销报工所在的工序ID
     * @param output      要减回的产出（合格 + 不良）
     * @param unqualified 要减回的不良
     * @param userName    操作工账号
     */
    void revertOnFeedback(Long workorderId, Long processId,
                          BigDecimal output, BigDecimal unqualified, String userName);

    /**
     * 取某工单某工序的「下一道」过站行（只读，冲销前校验下游是否已开工）
     *
     * @param workorderId 工单ID
     * @param processId   工序ID
     * @return 下一道过站行；本道是末道、工单没有卡、或卡上没有这道工序时返回 null
     */
    ProCardProcess queryNextCardProcess(Long workorderId, Long processId);

    /**
     * 取某工单某工序的过站行（只读，报工前做"投入够不够"的校验用）
     *
     * 过站行上的 quantity_input 就是这道工序实际投进来的料对应的活：
     *   首道 = 工单数量（整批投料）
     *   其余道 = 上一道累计产出
     * 所以「本道累计产出 + 本次报工」不能超过「本道投入」——
     * 上一道只出了 4 件，本道却要报 9 件，那是凭空多出来的产量。
     *
     * @param workorderId 工单ID
     * @param processId   工序ID
     * @return 过站行；工单没有卡或卡上没有这道工序时返回 null（历史数据，跳过校验）
     */
    ProCardProcess queryCardProcess(Long workorderId, Long processId);
}
