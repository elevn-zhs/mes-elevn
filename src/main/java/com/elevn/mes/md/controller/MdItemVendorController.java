package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdItemVendor;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdItemVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料供应商 Controller
 *
 * 一行 = 「某个物料可以向某家供应商采购」的一条供货关系，是物料与供应商的多对多中间表。
 * md_vendor 只是供应商档案，本身不挂物料，所以中间表必须存在。
 *
 * 同一个物料通常会挂多家供应商，方便比价与应急补货，primary_flag 标记主供应商（唯一）。
 * vendor_item_code 是供应商自己那边的料号 —— 我方编码和对方编码几乎不可能一样，必须分开存。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/itemVendor")
public class MdItemVendorController {

    @Autowired
    private MdItemVendorService mdItemVendorService;

    /**
     * 新增物料供应商
     */
    @PostMapping
    public Result<MdItemVendor> save(@RequestBody @Validated(CreateOption.class) MdItemVendor mdItemVendor) {
        return mdItemVendorService.save(mdItemVendor) == 1 ? Result.success(mdItemVendor) : Result.error("保存失败");
    }

    /**
     * 修改物料供应商
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdItemVendor mdItemVendor) {
        return mdItemVendorService.updateById(mdItemVendor) == 1 ? Result.success(mdItemVendor) : Result.error("修改失败");
    }

    /**
     * 根据ID查询物料供应商详情
     */
    @GetMapping("/{id}")
    public Result<MdItemVendor> queryById(@PathVariable Long id) {
        MdItemVendor mdItemVendor = mdItemVendorService.queryById(id);
        if (mdItemVendor == null) {
            return Result.error("物料供应商不存在或已被删除");
        }
        return Result.success(mdItemVendor);
    }

    /**
     * 分页 + 多条件查询物料供应商列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdItemVendor>> page(MdItemVendor mdItemVendor,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdItemVendorService.page(pageNum, pageSize, mdItemVendor));
    }

    /**
     * 删除（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdItemVendorService.deleteById(id));
    }

    /**
     * 批量删除物料供应商
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdItemVendorService.deleteBatch(ids));
    }

    /**
     * 按物料ID查询物料供应商列表（物料详情 tab 用）
     */
    @GetMapping("/byItemId/{itemId}")
    public Result<List<MdItemVendor>> queryByItemId(@PathVariable Long itemId) {
        return Result.success(mdItemVendorService.queryByItemId(itemId));
    }

}
