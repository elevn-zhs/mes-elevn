package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmTransaction;
import com.elevn.mes.wm.service.WmTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存事务（流水账）Controller
 *
 * 对应 wm_transaction。流水只能由单据过账生成，这里没有新增/修改/删除接口。
 *
 */
@RestController
@RequestMapping("/api/wm/transaction")
public class WmTransactionController {

    @Autowired
    private WmTransactionService wmTransactionService;

    /**
     * 分页查询库存事务流水
     *
     * 支持物料 / 批次 / 仓库 / 单据编号 / 事务类型 / 事务日期区间筛选。
     * 列表按主键倒序 —— 看的是"最近发生了什么"。
     */
    @GetMapping("/page")
    public Result<PageInfo<WmTransaction>> page(WmTransaction wmTransaction,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmTransactionService.page(pageNum, pageSize, wmTransaction));
    }

    /**
     * 按物料 + 批次查流水（批次追溯）
     *
     * 与列表查询的区别是排序：这里按时间【正序】，
     * 因为追溯看的是"这批货一路怎么走过来的"。
     * itemId 必填。
     */
    @GetMapping("/byItem")
    public Result<List<WmTransaction>> listByItemAndBatch(WmTransaction wmTransaction) {
        return Result.success(wmTransactionService.listByItemAndBatch(wmTransaction));
    }

    /**
     * 事务类型统计（每类单据一共入了多少、出了多少）
     */
    @GetMapping("/statByType")
    public Result<List<WmTransaction>> statByType(WmTransaction wmTransaction) {
        return Result.success(wmTransactionService.statByType(wmTransaction));
    }

    /**
     * 查看事务详情（含来源单据信息，软引用）
     */
    @GetMapping("/{id}")
    public Result<WmTransaction> queryById(@PathVariable Long id) {
        return Result.success(wmTransactionService.queryById(id));
    }
}
