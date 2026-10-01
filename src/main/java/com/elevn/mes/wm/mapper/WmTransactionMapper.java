package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 库存事务 / 流水账 Mapper
 * 对应表：wm_transaction
 * 映射文件：resources/mapper/wm/WmTransactionMapper.xml
 *
 * 【只有插入，没有修改和删除】
 *   流水账是会计凭证性质的东西：发生过就发生过。
 *   写错了要用一张反向单冲回去，而不是改这条记录 ——
 *   否则谁也说不清月底对账时看到的是不是被改过的数。
 *
 * 【唯一允许的"更新"是回填配对ID】
 *   调拨要写两条流水（源仓出、目标仓进），
 *   第一条插入时还不知道第二条的 ID，所以先插两条，再互相回填
 *   related_transaction_id。这是本表唯一的一次 update。
 *
 */
@Mapper
public interface WmTransactionMapper {

    /**
     * 新增流水（主键回填到 transactionId）
     * @param transaction 流水对象
     * @return 影响行数
     */
    int insert(WmTransaction transaction);

    /**
     * 回填配对事务ID（调拨的两条流水互指，方便页面上点一条看另一条）
     * @param transactionId 本条ID
     * @param relatedTransactionId 配对的另一条ID
     * @return 影响行数
     */
    int updateRelatedTransactionId(@Param("transactionId") Long transactionId,
                                   @Param("relatedTransactionId") Long relatedTransactionId);

    /**
     * 根据ID查询单条
     * @param id 事务ID
     * @return 流水对象，不存在返回 null
     */
    WmTransaction selectById(Long id);

    /**
     * 多条件查询流水（库存事务页用）
     * @param transaction 查询条件（itemId / itemCode / itemName / batchCode /
     *                     warehouseId / transactionType / transactionFlag /
     *                     来源单据 / 事务日期区间）
     * @return 流水列表
     */
    List<WmTransaction> selectByCondition(WmTransaction transaction);

    /**
     * 按物料 + 批次查流水（批次追溯用）
     *
     * 【与列表查询的区别是排序】
     *   列表是按主键倒序（最近的在前面），看的是"最近发生了什么"；
     *   追溯要按时间正序，看的是"这批货一路是怎么走过来的" ——
     *   入库 → 出库 → 调拨 → 报废，正着看才是完整的故事。
     *
     * @param transaction 查询条件：itemId 必填，batchId / batchCode 可选
     * @return 流水列表（时间正序）
     */
    List<WmTransaction> selectByItemAndBatch(WmTransaction transaction);

    /**
     * 按事务类型统计进出数量（每类单据一共入了多少、出了多少）
     * @param transaction 过滤条件（warehouseId / 事务日期区间，可为空）
     * @return 每个事务类型一行，数量放在 inQuantity / outQuantity / netQuantity 上
     */
    List<WmTransaction> statByType(WmTransaction transaction);
}
