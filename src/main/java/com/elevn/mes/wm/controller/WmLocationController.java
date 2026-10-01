package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmLocation;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import com.elevn.mes.wm.service.WmLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库区库位 Controller
 *
 * 一张表两级：库区(AREA) 是父、库位(LOCATION) 是子，
 * 新增时用同一个 POST 接口，靠 location_type 区分 —— 库区固定 parent_id=0，
 * 库位必须带 parent_id 指向库区（后端会强校验）。
 *
 */
@RestController
@RequestMapping("/api/wm/location")
public class WmLocationController {

    @Autowired
    private WmLocationService wmLocationService;

    /**
     * 新增库区 / 库位
     */
    @PostMapping
    public Result<WmLocation> save(@RequestBody @Validated(CreateOption.class) WmLocation wmLocation) {
        return wmLocationService.save(wmLocation) == 1 ? Result.success(wmLocation) : Result.error("保存失败");
    }

    /**
     * 修改库区 / 库位（类型、所属库区、所属仓库均不允许修改）
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) WmLocation wmLocation) {
        return wmLocationService.updateById(wmLocation) == 1 ? Result.success(wmLocation) : Result.error("修改失败");
    }

    /**
     * 根据ID查询详情（库位会带出当前存放的物料与批次）
     */
    @GetMapping("/{id}")
    public Result<WmLocation> queryById(@PathVariable Long id) {
        WmLocation wmLocation = wmLocationService.queryById(id);
        if (wmLocation == null) {
            return Result.error("库区库位不存在或已被删除");
        }
        return Result.success(wmLocation);
    }

    /**
     * 分页 + 多条件查询列表（支持仓库 / 类型筛选）
     */
    @GetMapping("/page")
    public Result<PageInfo<WmLocation>> page(WmLocation wmLocation,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmLocationService.page(pageNum, pageSize, wmLocation));
    }

    /**
     * 树形展示库区库位（库区为父、库位为子）
     * 不支持按类型过滤 —— 过滤掉库区就没法挂库位了，树会散架
     */
    @GetMapping("/tree")
    public Result<List<WmLocation>> tree(WmLocation wmLocation) {
        return Result.success(wmLocationService.queryTree(wmLocation));
    }

    /**
     * 删除库区 / 库位（逻辑删除；有下级库位或仍有库存时后端会拒绝）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(wmLocationService.deleteById(id));
    }

    /**
     * 批量删除库区库位
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(wmLocationService.deleteBatch(ids));
    }

    /**
     * 按类型 + 编码查询，可用于"编码是否已占用"的实时校验
     * 例：/api/wm/location/byCode?locationType=LOCATION&locationCode=A01-01
     */
    @GetMapping("/byCode")
    public Result<WmLocation> queryByCode(@RequestParam String locationType,
                                          @RequestParam String locationCode) {
        return Result.success(wmLocationService.queryByCodeAndType(locationType, locationCode));
    }
}
