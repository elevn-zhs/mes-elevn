package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProMaterialRequire;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProTransConsume;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料消耗 Service
 *
 * 报工总线的第三件事就是这里 —— 分工方案 S7 原话：
 *   POST /pro/feedback —— 报工总线（回写 + 生成消耗 + 推进流转）
 *   回写数量台账  -> ProFeedbackServiceImpl.applyProducedQuantity
 *   推进流转卡    -> ProCardService.advanceOnFeedback
 *   生成物料消耗  -> 本类的 generateOnFeedback（倒冲）
 *
 * 三个动作在同一个报工事务里完成，所以报工一旦成功，消耗必定跟着落账，
 * 不会出现"报了工但料没扣"的账面缺口。
 *
 */
public interface ProTransConsumeService {

    // ============================================================
    // 查询
    // ============================================================

    /** 分页条件查询（联查任务/工单/工作站补出展示字段） */
    PageInfo<ProTransConsume> page(int pageNum, int pageSize, ProTransConsume condition);

    /** 条件查询全部（导出用） */
    List<ProTransConsume> queryList(ProTransConsume condition);

    /** 按主键查询 */
    ProTransConsume queryById(Long recordId);

    /** 按任务查消耗明细 */
    List<ProTransConsume> queryByTaskId(Long taskId);

    /** 按工单查消耗明细 */
    List<ProTransConsume> queryByWorkorderId(Long workorderId);

    /** 按工单汇总各物料消耗（工单详情页"用料汇总"用） */
    List<ProTransConsume> summaryByWorkorder(Long workorderId);

    /**
     * 工序用料需求：这道工序该用哪些料、应耗多少、已耗多少、还差多少
     *
     * 「投入产出比对」的载体，也是报工弹窗里给报工人看的"我领了什么料"。
     *
     * @param taskId 生产任务ID
     * @return 用料需求列表（按物料编码排序）
     */
    List<ProMaterialRequire> queryMaterialRequire(Long taskId);

    /**
     * 工单用料比对：工单BOM 上的预计使用量（应耗） vs 实际累计消耗（实耗）
     *
     * 投入产出比对的工单级视图 —— 哪样料超了、哪样还没用够。
     * 应耗 = 工单下达时按 BOM 展开并乘以工单数量的结果（pro_workorder_bom.quantity）。
     *
     * @param workorderId 生产工单ID
     * @return 比对列表（按物料编码排序）
     */
    List<ProMaterialRequire> compareByWorkorder(Long workorderId);

    // ============================================================
    // 写入（只被报工/冲销调用，不对外暴露新增接口）
    // ============================================================

    /**
     * 报工倒冲：按本次产出 × 制程BOM 单位用量生成物料消耗记录。
     * 由 ProFeedbackServiceImpl.feedback 在同一事务里调用。
     *
     * 静默跳过的情形（都不是错误）：
     *   任务缺少路线/工序/产品信息（历史数据）
     *   本次产出为 0
     *   这道工序在制程BOM 上没有配用料（如测试、老化这类不耗料的工序）
     *
     * @param task        生产任务（已排产，带路线/工序/产品快照）
     * @param outputQty   本次产出数量（合格 + 不良，不良也吃掉了料）
     * @param consumeTime 消耗时间（取报工时间，与过站时间对齐）
     * @param userName    操作工账号
     * @param feedbackId  来源报工ID（冲销时据此精确回退，必须回填）
     * @return 生成的消耗记录条数
     */
    int generateOnFeedback(ProTask task, BigDecimal outputQty,
                           LocalDateTime consumeTime, String userName, Long feedbackId);

    /**
     * 按来源报工回退消耗记录（物理删除）。
     * 报工冲销时调用：报工不存在了，它倒冲出来的消耗也不该留着。
     *
     * @param feedbackId 报工ID
     * @return 回退条数
     */
    int removeByFeedbackId(Long feedbackId);

    /**
     * 按任务清空消耗记录（物理删除）。
     * 撤销排产时用：任务都没了，挂在它下面的消耗自然也不该留。
     *
     * @param taskId 生产任务ID
     * @return 删除条数
     */
    int removeByTaskId(Long taskId);

    /** 按主键逻辑删除（单条误录时用） */
    int deleteById(Long recordId);
}
