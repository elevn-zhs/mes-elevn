package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmWarehouse;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import com.elevn.mes.wm.service.WmWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库 Controller
 *
 * 仓储管理的第一个页面，对应 wm_warehouse。
 * 仓库 → 库区 → 库位 → 库存 → 单据，这里是整条链路的起点。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/wm/warehouse")
public class WmWarehouseController {

    @Autowired
    private WmWarehouseService wmWarehouseService;

    /**
     * 新增仓库
     */
    @PostMapping
    public Result<WmWarehouse> save(@RequestBody @Validated(CreateOption.class) WmWarehouse wmWarehouse) {
        return wmWarehouseService.save(wmWarehouse) == 1 ? Result.success(wmWarehouse) : Result.error("保存失败");
    }

    /**
     * 修改仓库（编码不可改，传了不同的编码后端会拒绝）
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) WmWarehouse wmWarehouse) {
        return wmWarehouseService.updateById(wmWarehouse) == 1 ? Result.success(wmWarehouse) : Result.error("修改失败");
    }

    /**
     * 根据ID查询仓库详情（含库区数量、库位数、库存总量、库存物料种数）
     */
    @GetMapping("/{id}")
    public Result<WmWarehouse> queryById(@PathVariable Long id) {
        WmWarehouse wmWarehouse = wmWarehouseService.queryById(id);
        if (wmWarehouse == null) {
            return Result.error("仓库不存在或已被删除");
        }
        return Result.success(wmWarehouse);
    }

    /**
     * 分页 + 多条件查询仓库列表（支持编码/名称/是否启用筛选）
     */
    @GetMapping("/page")
    public Result<PageInfo<WmWarehouse>> page(WmWarehouse wmWarehouse,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmWarehouseService.page(pageNum, pageSize, wmWarehouse));
    }

    /**
     * 查询全部启用的仓库（库区库位、单据选仓库的下拉数据源）
     */
    @GetMapping("/all")
    public Result<List<WmWarehouse>> queryAllEnabled() {
        return Result.success(wmWarehouseService.queryAllEnabled());
    }

    /**
     * 删除仓库（逻辑删除；下挂库区库位或还有库存时后端会拒绝）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(wmWarehouseService.deleteById(id));
    }

    /**
     * 批量删除仓库
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(wmWarehouseService.deleteBatch(ids));
    }

    /**
     * 根据编码查询仓库，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byWarehouseCode/{warehouseCode}")
    public Result<WmWarehouse> queryByWarehouseCode(@PathVariable String warehouseCode) {
        return Result.success(wmWarehouseService.queryByWarehouseCode(warehouseCode));
    }
}
