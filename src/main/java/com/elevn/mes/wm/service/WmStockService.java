package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmMaterialStock;

import java.util.List;

/**
 * 库存查询 Service
 *
 * 【本模块全部是只读的，一个写方法都没有】
 *   库存是结果，单据才是原因 —— 想让库存变，只能去开出库入库单然后过账。
 *   页面上放开"直接改库存数量"看着方便，实际是灾难：
 *   账和单据对不上，出了问题谁也说不清货是怎么少的。
 *
 * 【三个视图的区别，别混】
 *   库存明细（query）        —— 一行 = 一个「物料+批次+仓库+库区+库位+容器」的现存记录
 *   按物料汇总（summaryByItem）—— 一行 = 一个物料，把散在各库位的数量加起来
 *   按仓库汇总（summaryByWarehouse）—— 一行 = 一个仓库，看这个仓里压了多少种料、总量多少
 *   汇总里的数量是【求和结果】，不是某一行库存的数量，页面上要写清楚。
 *
 */
public interface WmStockService {

    /**
     * 分页查询库存明细（支持物料/批次/仓库/库区/库位组合查询）
     */
    PageInfo<WmMaterialStock> query(int pageNum, int pageSize, WmMaterialStock stock);

    /**
     * 按库存记录ID查询单条
     */
    WmMaterialStock queryById(Long materialStockId);

    /**
     * 按物料汇总
     */
    List<WmMaterialStock> summaryByItem(WmMaterialStock stock);

    /**
     * 按仓库汇总
     */
    List<WmMaterialStock> summaryByWarehouse(WmMaterialStock stock);

    /**
     * 库存预警：低于最低库存 / 高于最高库存的物料（安全库存取自 md_item）
     */
    List<WmMaterialStock> warningList();

    // AI助手需要的方法
    /**
     * 按仓库汇总
     */
    List<WmMaterialStock> summaryByWarehouseAndItem(Long warehouseId,Long itemId);
}
