package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmMaterialStock;
import com.elevn.mes.wm.service.WmStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存查询 Controller
 *
 * 对应 wm_material_stock。整块都是只读的 —— 库存不能手改，只能由单据过账驱动。
 *
 * 三个视图：
 *   /query             库存明细（一行 = 一个物料+批次+仓库+库区+库位+容器的现存记录）
 *   /summary/byItem    按物料汇总（把散在各库位的数量加起来）
 *   /summary/byWarehouse 按仓库汇总
 *   /warning           库存预警（低于最低库存 / 高于最高库存）
 *
 */
@RestController
@RequestMapping("/api/wm/stock")
public class WmStockController {

    @Autowired
    private WmStockService wmStockService;

    /**
     * 库存查询（多条件）
     *
     * 支持物料 / 批次 / 仓库 / 库区 / 库位组合查询。
     * 返回里带 quantityAvailable（可用 = 在库 - 保留）与
     * minStock / maxStock（取自物料档案）以及 warningType，页面上可直接高亮。
     *
     * @param onlyStock 传 'Y' 只看还有货的行
     */
    @GetMapping("/query")
    public Result<PageInfo<WmMaterialStock>> query(WmMaterialStock wmMaterialStock,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmStockService.query(pageNum, pageSize, wmMaterialStock));
    }

    /**
     * 库存汇总视图 - 按物料
     *
     * 【注意】返回的数量是求和结果，不是某一行库存的数量。
     */
    @GetMapping("/summary/byItem")
    public Result<List<WmMaterialStock>> summaryByItem(WmMaterialStock wmMaterialStock) {
        return Result.success(wmStockService.summaryByItem(wmMaterialStock));
    }

    /**
     * 库存汇总视图 - 按仓库（每个仓里有几种料、总量多少）
     */
    @GetMapping("/summary/byWarehouse")
    public Result<List<WmMaterialStock>> summaryByWarehouse(WmMaterialStock wmMaterialStock) {
        return Result.success(wmStockService.summaryByWarehouse(wmMaterialStock));
    }

    /**
     * 库存预警查询
     *
     * 安全库存（最低库存 min_stock / 最高库存 max_stock）取自 E 线的物料档案。
     * 【为什么按物料汇总后再比】一个物料散在 3 个库位各 10，逐行看着都不低，
     * 加起来只有 30 —— 早就该补货了。
     */
    @GetMapping("/warning")
    public Result<List<WmMaterialStock>> warningList() {
        return Result.success(wmStockService.warningList());
    }

    /**
     * 查看单条库存记录
     */
    @GetMapping("/{id}")
    public Result<WmMaterialStock> queryById(@PathVariable Long id) {
        return Result.success(wmStockService.queryById(id));
    }
}
