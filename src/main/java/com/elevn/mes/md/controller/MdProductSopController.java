package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdProductSop;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdProductSopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品SOP Controller
 *
 * 产品SOP（标准作业指导书），按产品+工序维护生产步骤。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/productSop")
public class MdProductSopController {

    @Autowired
    private MdProductSopService mdProductSopService;

    /**
     * 新增产品SOP
     */
    @PostMapping
    public Result<MdProductSop> save(@RequestBody @Validated(CreateOption.class) MdProductSop mdProductSop) {
        return mdProductSopService.save(mdProductSop) == 1 ? Result.success(mdProductSop) : Result.error("保存失败");
    }

    /**
     * 修改产品SOP
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdProductSop mdProductSop) {
        return mdProductSopService.updateById(mdProductSop) == 1 ? Result.success(mdProductSop) : Result.error("修改失败");
    }

    /**
     * 根据ID查询产品SOP详情
     */
    @GetMapping("/{id}")
    public Result<MdProductSop> queryById(@PathVariable Long id) {
        MdProductSop mdProductSop = mdProductSopService.queryById(id);
        if (mdProductSop == null) {
            return Result.error("产品SOP不存在或已被删除");
        }
        return Result.success(mdProductSop);
    }

    /**
     * 分页 + 多条件查询产品SOP列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdProductSop>> page(MdProductSop mdProductSop,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdProductSopService.page(pageNum, pageSize, mdProductSop));
    }

    /**
     * 删除产品SOP（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdProductSopService.deleteById(id));
    }

    /**
     * 批量删除产品SOP
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdProductSopService.deleteBatch(ids));
    }

    /**
     * 按所属ID查询产品SOP列表
     */
    @GetMapping("/byItemId/{itemId}")
    public Result<List<MdProductSop>> queryByItemId(@PathVariable Long itemId) {
        return Result.success(mdProductSopService.queryByItemId(itemId));
    }

}
