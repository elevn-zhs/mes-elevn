package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmMaterialStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存记录（现存量）Mapper
 * 对应表：wm_material_stock
 * 映射文件：resources/mapper/wm/WmMaterialStockMapper.xml
 *
 * 【本表页面上只读，只能由单据过账驱动】
 *   所以这里没有"随便改数量"的方法，只有两条有明确业务含义的增减：
 *     addOnhand    —— 入库累加（过账时 io_flag = I）
 *     reduceOnhand —— 出库扣减（过账时 io_flag = O / T 的源仓）
 *
 * 【reduceOnhand 的 where 里带了可用量判断，这是有意的】
 *   Service 在调它之前已经校验过一次可用量，看起来重复。
 *   但两次点击之间可能刚好有人把货出走了 —— 数据库这一层判断
 *   （quantity_onhand - quantity_reserved >= #{quantity}）返回 0 行，
 *   Service 就知道"余额已经不够了"，整个事务回滚。
 *   校验放在数据库层，才是真正堵死并发的那一道。
 *
 */
@Mapper
public interface WmMaterialStockMapper {

    /**
     * 按 uk_stock 维度找库存行（入库"找到累加、找不到新建"的第一步）
     *
     * 维度 = 物料 + 批次 + 仓库 + 库区 + 库位 + 容器（六件套，与唯一键一致）。
     * 注意这几个字段不许为 NULL —— MySQL 唯一索引不比较 NULL，
     * 一旦有 NULL，"同一批料只允许一行"这条约束就废了，
     * 所以调用方要用 0 / 空串表示"无"。
     *
     * @param stock 只需填六个维度字段
     * @return 命中的库存行，没有则返回 null
     */
    WmMaterialStock selectByDimension(WmMaterialStock stock);

    /**
     * 新建库存行（主键回填到 materialStockId）
     * @param stock 库存对象
     * @return 影响行数
     */
    int insert(WmMaterialStock stock);

    /**
     * 入库累加在库数量
     * @param materialStockId 库存记录ID
     * @param quantity 本次入库数量（正数）
     * @return 影响行数
     */
    int addOnhand(@Param("materialStockId") Long materialStockId,
                  @Param("quantity") BigDecimal quantity);

    /**
     * 出库扣减在库数量
     *
     * where 里带 `quantity_onhand - quantity_reserved >= #{quantity}`：
     * 余额不足时返回 0 行，Service 据此抛"可用库存不足"并回滚事务。
     *
     * @param materialStockId 库存记录ID
     * @param quantity 本次出库数量（正数）
     * @return 影响行数（0 表示可用量不足或库存行已被改动）
     */
    int reduceOnhand(@Param("materialStockId") Long materialStockId,
                     @Param("quantity") BigDecimal quantity);

    /**
     * 按ID查询单条（详情、校验用）
     * @param id 库存记录ID
     * @return 库存对象，不存在返回 null
     */
    WmMaterialStock selectById(Long id);

    /**
     * 查可出库的库存行（出库行没有指定 material_stock_id 时，由后端自己找货）
     *
     * 排序是按入库时间升序 —— 先进先出（FIFO）：
     * 老批次先出去，留新批次在库里，减少过期报废。
     *
     * @param stock 查询条件：itemId 必填；batchId / warehouseId / locationId 可选
     * @return 可用量大于 0 的库存行（含算出来的 quantity_available）
     */
    List<WmMaterialStock> selectAvailableForOutbound(WmMaterialStock stock);

    /**
     * 多条件查询库存（库存查询页用）
     * @param stock 查询条件（itemId / itemCode / itemName / batchCode / warehouseId /
     *              locationId / areaId / 只看有货 / 只看冻结）
     * @return 库存列表
     */
    List<WmMaterialStock> selectByCondition(WmMaterialStock stock);

    /**
     * 统计某物料在某仓库的库存记录数（仓库删除前的 L3 校验用）
     * @param warehouseId 仓库ID
     * @return 库存记录数
     */
    int countByWarehouseId(@Param("warehouseId") Long warehouseId);

    // ====================================================================
    // 汇总与预警（库存查询页 / 库存预警页用）
    // ====================================================================

    /**
     * 按物料汇总（同一个物料散在多个仓库库位的数量加起来看总数）
     *
     * 返回的 quantityOnhand / quantityAvailable 是【求和结果】，
     * 不是某一行库存的数量 —— 这一点页面上要写清楚，否则容易看错。
     *
     * @param stock 过滤条件（itemCode / itemName / onlyStock）
     * @return 每个物料一行
     */
    List<WmMaterialStock> selectSummaryByItem(WmMaterialStock stock);

    /**
     * 按仓库汇总（每个仓库里有多少种物料、总量多少）
     * @param stock 过滤条件（warehouseId / onlyStock）
     * @return 每个仓库一行
     */
    List<WmMaterialStock> selectSummaryByWarehouse(WmMaterialStock stock);

    /**
     * 库存预警：按物料汇总后，低于最低库存或超过最高库存的挑出来
     *
     * 安全库存取自 E 线的 md_item.min_stock / max_stock。
     * 为什么按物料汇总再比，而不是逐行比 —— 一个物料散在 3 个库位各 30，
     * 最低库存是 50，逐行看每行都不低，加起来其实只有 90 也不低；
     * 但如果是各 10，逐行都不报，实际只有 30，早就该补货了。
     *
     * @return 预警列表（含 warningType：LOW / HIGH）
     */
    List<WmMaterialStock> selectWarningList();

    /**
     * 盘点范围展开：按 仓库 / 库区 / 物料分类 三选一（可都为 null = 全部），
     * 只取现存数量 &gt; 0 的库存行 —— 账上没货的行盘不出差异，
     * 真要盘"账上有实际没有"的场景，账面数就是这些行的 onhand，不至于漏。
     *
     * @param warehouseId 按仓库圈（范围类型 WAREHOUSE）
     * @param areaId      按库区圈（范围类型 AREA）
     * @param itemTypeId  按物料分类圈（范围类型 ITEM_TYPE）
     */
    List<WmMaterialStock> selectByScope(@Param("warehouseId") Long warehouseId,
                                        @Param("areaId") Long areaId,
                                        @Param("itemTypeId") Long itemTypeId);
}
