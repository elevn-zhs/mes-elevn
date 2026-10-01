package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProMaterialRequire;
import com.elevn.mes.pro.entity.ProTransConsume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物料消耗记录 的Mapper接口
 * 对应表：pro_trans_consume
 * 映射文件：resources/mapper/pro/ProTransConsumeMapper.xml
 *
 */
@Mapper
public interface ProTransConsumeMapper {

    /**
     * 新增消耗记录（自增主键回写到实体）
     * @param proTransConsume 消耗记录
     * @return 影响行数
     */
    int insert(ProTransConsume proTransConsume);

    /**
     * 按主键查询
     * @param recordId 记录ID
     * @return 消耗记录
     */
    ProTransConsume selectById(@Param("recordId") Long recordId);

    /**
     * 多条件查询（列表页用，联查任务/工单/工作站补出展示字段）
     * @param proTransConsume 查询条件
     * @return 消耗记录列表
     */
    List<ProTransConsume> selectByCondition(ProTransConsume proTransConsume);

    /**
     * 按任务查消耗明细
     * @param taskId 生产任务ID
     * @return 消耗记录列表
     */
    List<ProTransConsume> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 按工单查消耗明细
     * @param workorderId 生产工单ID
     * @return 消耗记录列表
     */
    List<ProTransConsume> selectByWorkorderId(@Param("workorderId") Long workorderId);

    /**
     * 按工单+物料汇总消耗量（投入产出台账用）
     * @param workorderId 生产工单ID
     * @return 消耗明细（物料 + 累计消耗）
     */
    List<ProTransConsume> sumByWorkorderItem(@Param("workorderId") Long workorderId);

    /**
     * 汇总某任务某物料的累计消耗
     * @param taskId 生产任务ID
     * @param itemId 物料ID
     * @return 累计消耗数量
     */
    BigDecimal sumConsumedByTaskAndItem(@Param("taskId") Long taskId, @Param("itemId") Long itemId);

    /**
     * 汇总某任务的累计消耗（全部物料）
     * @param taskId 生产任务ID
     * @return 累计消耗数量
     */
    BigDecimal sumConsumedByTaskId(@Param("taskId") Long taskId);

    /**
     * 工序用料需求：这道工序该用哪些料、应耗多少、已耗多少
     *
     * 用料清单来自 pro_route_product_bom（产品+路线+工序维度），
     * 已耗从 pro_trans_consume 汇总。一次查询直接给出"投入产出比对"结果。
     *
     * @param routeId   工艺路线ID
     * @param processId 工序ID
     * @param productId 产品物料ID（md_item.item_id）
     * @param taskId    生产任务ID（用于汇总已耗；为空则已耗一律为 0）
     * @param taskQty   任务排产数量（用于算应耗）
     * @return 用料需求列表
     */
    List<ProMaterialRequire> selectMaterialRequire(@Param("routeId") Long routeId,
                                                   @Param("processId") Long processId,
                                                   @Param("productId") Long productId,
                                                   @Param("taskId") Long taskId,
                                                   @Param("taskQty") BigDecimal taskQty);

    /**
     * 工单用料比对：工单下达时展开的预计使用量（应耗） vs 实际累计消耗（实耗）
     *
     * 这是「投入产出比对」的工单级视图：哪样料用超了、哪样还没用够，一眼看出来。
     * 应耗来自 pro_workorder_bom（下达时按 BOM 展开并乘以工单数量的结果），
     * 实耗来自 pro_trans_consume。
     *
     * @param workorderId 生产工单ID
     * @return 比对列表（按物料编码排序）
     */
    List<ProMaterialRequire> selectWorkorderMaterialCompare(@Param("workorderId") Long workorderId);

    /**
     * 按任务删除消耗记录（物理删除：撤销排产时连根拔掉，不做逻辑删除堆积）
     * @param taskId 生产任务ID
     * @return 影响行数
     */
    int deleteByTaskId(@Param("taskId") Long taskId);

    /**
     * 按来源报工删除消耗记录（物理删除：报工冲销时回退用料）
     *
     * 精准按 feedback_id 定位，不按「任务 + 时间」猜 —— 同一道工序会分批报工，
     * 按时间猜必然误杀别人那次报工的账。
     *
     * @param feedbackId 报工ID
     * @return 影响行数
     */
    int deleteByFeedbackId(@Param("feedbackId") Long feedbackId);

    /**
     * 按主键逻辑删除
     * @param recordId 记录ID
     * @param updateBy 更新者
     * @return 影响行数
     */
    int deleteById(@Param("recordId") Long recordId, @Param("updateBy") String updateBy);
}
