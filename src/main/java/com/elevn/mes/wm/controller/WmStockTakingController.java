package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmStockTaking;
import com.elevn.mes.wm.entity.WmStockTakingLine;
import com.elevn.mes.wm.entity.WmStockTakingPlan;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import com.elevn.mes.wm.service.WmStockTakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 盘点 Controller
 *
 * 【路由口径】按任务表要求走 /api/wm/stock/taking/*，
 * 注意和库存查询的 /api/wm/stock/* 不冲突：taking 是自己的子路径
 *
 */
@RestController
@RequestMapping("/api/wm/stock/taking")
public class WmStockTakingController {

    @Autowired
    private WmStockTakingService takingService;

    // ==================== 计划 ====================

    /** 新增盘点计划（body 里带 scopeList 范围行） */
    @PostMapping("/plan")
    public Result<WmStockTakingPlan> createPlan(
            @Validated(CreateOption.class) @RequestBody WmStockTakingPlan plan) {
        return Result.success(takingService.createPlan(plan));
    }

    /** 分页查询盘点计划 */
    @GetMapping("/plan/page")
    public Result<PageInfo<WmStockTakingPlan>> pagePlan(WmStockTakingPlan query,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(takingService.pagePlan(pageNum, pageSize, query));
    }

    /** 计划详情（含范围行） */
    @GetMapping("/plan/{planId}")
    public Result<WmStockTakingPlan> getPlanById(@PathVariable Long planId) {
        return Result.success(takingService.getPlanById(planId));
    }

    /** 编辑计划（仅"待执行"；范围整批替换） */
    @PutMapping("/plan")
    public Result<Void> updatePlan(@Validated(UpdateOption.class) @RequestBody WmStockTakingPlan plan) {
        takingService.updatePlan(plan);
        return Result.success();
    }

    /** 删除计划（已生成盘点单的禁止删） */
    @DeleteMapping("/plan/{planId}")
    public Result<Void> deletePlan(@PathVariable Long planId) {
        takingService.deletePlan(planId);
        return Result.success();
    }

    // ==================== 生成 / 盘点单 ====================

    /** 按计划展开范围生成盘点单 */
    @PostMapping("/generate/{planId}")
    public Result<WmStockTaking> generate(@PathVariable Long planId) {
        return Result.success(takingService.generate(planId, "admin"));
    }

    /** 分页查询盘点单 */
    @GetMapping("/page")
    public Result<PageInfo<WmStockTaking>> page(WmStockTaking query,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(takingService.page(pageNum, pageSize, query));
    }

    /** 盘点单详情（含明细行；盲盘单不回传账面数量） */
    @GetMapping("/{takingId}")
    public Result<WmStockTaking> getById(@PathVariable Long takingId) {
        return Result.success(takingService.getById(takingId));
    }

    /** 批量录入实盘数（body 是明细行数组：lineId + takingQuantity） */
    @PutMapping("/{takingId}/lines")
    public Result<Void> saveTakingQuantities(@PathVariable Long takingId,
                                             @RequestBody List<WmStockTakingLine> lines) {
        takingService.saveTakingQuantities(takingId, lines);
        return Result.success();
    }

    /** 过账：盈亏写库存 + 流水（一个事务） */
    @PostMapping("/post/{takingId}")
    public Result<Void> post(@PathVariable Long takingId) {
        takingService.post(takingId);
        return Result.success();
    }

    /** 差异明细（diff != 0 的行） */
    @GetMapping("/{takingId}/diff")
    public Result<List<WmStockTakingLine>> diffLines(@PathVariable Long takingId) {
        return Result.success(takingService.diffLines(takingId));
    }

    /** 删除盘点单（仅"待录入"） */
    @DeleteMapping("/{takingId}")
    public Result<Void> delete(@PathVariable Long takingId) {
        takingService.delete(takingId);
        return Result.success();
    }
}
