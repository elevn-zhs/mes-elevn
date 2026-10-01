package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProFeedback;
import com.elevn.mes.pro.entity.ProFeedbackDTO;

import java.util.List;
import java.util.Map;

/**
 * 生产报工 Service
 *
 */
public interface ProFeedbackService {

    /** 分页 + 多条件查询报工列表 */
    PageInfo<ProFeedback> page(int pageNum, int pageSize, ProFeedback proFeedback);

    /** 按主键查询报工详情 */
    ProFeedback queryById(Long id);

    /** 按任务ID查询该任务的全部报工 */
    List<ProFeedback> queryByTaskId(Long taskId);

    /** 条件查询报工列表（不分页） */
    List<ProFeedback> queryList(ProFeedback proFeedback);

    /**
     * 报工（核心方法，报工总线）
     *
     * 一次事务内完成：校验可报数量 → 写报工记录 → 回写任务数量与状态
     * → 任务报满则置完工 → 工单全部任务完工则累加工单已生产数量。
     *
     * @param dto 报工入参
     * @return 报工结果摘要（报工编号、任务与工单的最新数量与状态）
     */
    Map<String, Object> feedback(ProFeedbackDTO dto);

    /**
     * 报工预览：返回该任务"还剩多少可报"，供前端做上限校验与默认值
     *
     * @param taskId 任务ID
     * @return 任务信息 + 已报工数量 + 剩余可报数量
     */
    Map<String, Object> preview(Long taskId);

    /**
     * 报工冲销（红冲）—— 报工报错了怎么办
     *
     * 报工是真实发生过的业务事实，所以冲销**不删行**：把状态置为 REVERSED，
     * 记下冲销人/时间/原因，然后把这次报工带来的影响全部回退：
     *
     *   1. pro_feedback     状态 → REVERSED（留痕）
     *   2. pro_task         已生产/合格/不良 减回，状态按剩余数量退回
     *   3. pro_trans_consume 按 feedback_id 删掉这次倒冲出来的用料
     *   4. pro_card_process 本道产出减回、下一道投入减回
     *   5. pro_workorder    工单因本次冲销不再完工时，已生产数扣回并退回已下达
     *
     * 五步在同一个事务里，任一步失败全部回滚 —— 绝不能出现
     * "报工撤了但产量还挂着"或"产量扣了但料没退"。
     *
     * 会被拒绝的情形（L3：账不能乱）：
     *   - 报工不存在 / 已被冲销
     *   - 下游工序已经产出成品（说明这批料已经流下去了，要先冲下游）
     *   - 任务已生产数量小于本次报工数量（账已经对不上，得先查清楚）
     *
     * @param id     报工ID
     * @param reason 冲销原因（必填，留痕用）
     * @return 冲销结果摘要（回退数量、回退用料条数、任务与工单的最新状态）
     */
    Map<String, Object> reverse(Long id, String reason);

    /** 按主键逻辑删除报工记录（不做数量扣回，正式冲销走 reverse） */
    int deleteById(Long id);
}
