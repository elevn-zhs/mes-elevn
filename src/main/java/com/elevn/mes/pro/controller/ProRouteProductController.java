package com.elevn.mes.pro.controller;

import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProRouteProduct;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import com.elevn.mes.pro.service.ProRouteProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品制程 Controller
 *
 * 产品制程作为物料详情页的「工艺路线」Tab 维护，不需要独立列表页，
 * 所以接口只有按产品查全量、保存、修改、删除 —— 没有分页。
 *
 */
@RestController
@RequestMapping("/api/pro/routeProduct")
public class ProRouteProductController {

    @Autowired
    private ProRouteProductService proRouteProductService;

    /**
     * 按产品ID查询全部制程
     */
    @GetMapping("/byItemId/{itemId}")
    public Result<List<ProRouteProduct>> selectByItemId(@PathVariable Long itemId) {
        return Result.success(proRouteProductService.selectByItemId(itemId));
    }

    /**
     * 新增产品制程（挂接路线）
     */
    @PostMapping
    public Result<ProRouteProduct> save(@RequestBody @Validated(CreateOption.class) ProRouteProduct proRouteProduct) {
        return proRouteProductService.save(proRouteProduct) == 1 ? Result.success(proRouteProduct) : Result.error("保存失败");
    }

    /**
     * 修改产品制程（只能改数量/用时/备注）
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) ProRouteProduct proRouteProduct) {
        return proRouteProductService.updateById(proRouteProduct) == 1 ? Result.success(proRouteProduct) : Result.error("修改失败");
    }

    /**
     * 删除产品制程（联动清理制程BOM）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(proRouteProductService.deleteById(id));
    }
}
