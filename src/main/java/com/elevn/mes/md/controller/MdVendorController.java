package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdVendor;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商 Controller
 *
 * 供应商主数据，采购入库的供货方。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/vendor")
public class MdVendorController {

    @Autowired
    private MdVendorService mdVendorService;

    /**
     * 新增供应商
     */
    @PostMapping
    public Result<MdVendor> save(@RequestBody @Validated(CreateOption.class) MdVendor mdVendor) {
        return mdVendorService.save(mdVendor) == 1 ? Result.success(mdVendor) : Result.error("保存失败");
    }

    /**
     * 修改供应商
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdVendor mdVendor) {
        return mdVendorService.updateById(mdVendor) == 1 ? Result.success(mdVendor) : Result.error("修改失败");
    }

    /**
     * 根据ID查询供应商详情
     */
    @GetMapping("/{id}")
    public Result<MdVendor> queryById(@PathVariable Long id) {
        MdVendor mdVendor = mdVendorService.queryById(id);
        if (mdVendor == null) {
            return Result.error("供应商不存在或已被删除");
        }
        return Result.success(mdVendor);
    }

    /**
     * 分页 + 多条件查询供应商列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdVendor>> page(MdVendor mdVendor,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdVendorService.page(pageNum, pageSize, mdVendor));
    }

    /**
     * 删除供应商（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdVendorService.deleteById(id));
    }

    /**
     * 批量删除供应商
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdVendorService.deleteBatch(ids));
    }

    /**
     * 根据编码查询供应商，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byVendorCode/{vendorCode}")
    public Result<MdVendor> queryByVendorCode(@PathVariable String vendorCode) {
        return Result.success(mdVendorService.queryByVendorCode(vendorCode));
    }

}
