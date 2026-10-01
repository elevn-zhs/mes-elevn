package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmPackage;
import com.elevn.mes.wm.entity.WmPackageLine;
import com.elevn.mes.wm.service.WmPackageService;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
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
 * 装箱单 Controller
 * 路由前缀 /api/wm/package（B 线统一 wm 前缀）
 *
 */
@RestController
@RequestMapping("/api/wm/package")
public class WmPackageController {

    @Autowired
    private WmPackageService packageService;

    /** 分页查询装箱单（支持箱号/销售订单/客户/状态/装箱日期区间筛选） */
    @GetMapping("/page")
    public Result<PageInfo<WmPackage>> page(WmPackage query,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(packageService.page(pageNum, pageSize, query));
    }

    /** 详情（表头 + 明细行 + 直接子箱） */
    @GetMapping("/{packageId}")
    public Result<WmPackage> getById(@PathVariable Long packageId) {
        return Result.success(packageService.getById(packageId));
    }

    /** 新增箱（parentId 传 0 或不传 = 顶层箱；传父箱 ID = 箱套箱） */
    @PostMapping
    public Result<WmPackage> create(@Validated(CreateOption.class) @RequestBody WmPackage wmPackage) {
        return Result.success(packageService.create(wmPackage));
    }

    /** 编辑箱信息（编号/父级/状态不可改，仅"装箱中"可编辑） */
    @PutMapping
    public Result<Void> update(@Validated(UpdateOption.class) @RequestBody WmPackage wmPackage) {
        packageService.update(wmPackage);
        return Result.success();
    }

    /** 完成装箱（PREPARE → PACKED，明细锁定） */
    @PutMapping("/finish/{packageId}")
    public Result<Void> finish(@PathVariable Long packageId) {
        packageService.finish(packageId);
        return Result.success();
    }

    /** 删除箱（有已完成子箱拦截；删除时连子树和明细一起删） */
    @DeleteMapping("/{packageId}")
    public Result<Void> delete(@PathVariable Long packageId) {
        packageService.delete(packageId);
        return Result.success();
    }

    /** 加明细行（body 里传 materialStockId + quantity，快照后端从库存行回填） */
    @PostMapping("/line")
    public Result<WmPackageLine> addLine(@Validated(CreateOption.class) @RequestBody WmPackageLine line) {
        return Result.success(packageService.addLine(line));
    }

    /** 改明细行（只允许改数量与备注） */
    @PutMapping("/line")
    public Result<Void> updateLine(@RequestBody WmPackageLine line) {
        packageService.updateLine(line);
        return Result.success();
    }

    /** 删明细行 */
    @DeleteMapping("/line/{lineId}")
    public Result<Void> deleteLine(@PathVariable Long lineId) {
        packageService.deleteLine(lineId);
        return Result.success();
    }
}
