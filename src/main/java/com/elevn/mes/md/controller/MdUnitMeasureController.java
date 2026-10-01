package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdUnitMeasure;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdUnitMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 计量单位 Controller
 *
 * 计量单位。支持主辅单位换算：primary_flag=N 时通过 primary_id + change_rate 换算到主单位。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/unitMeasure")
public class MdUnitMeasureController {

    @Autowired
    private MdUnitMeasureService mdUnitMeasureService;

    /**
     * 新增计量单位
     */
    @PostMapping
    public Result<MdUnitMeasure> save(@RequestBody @Validated(CreateOption.class) MdUnitMeasure mdUnitMeasure) {
        return mdUnitMeasureService.save(mdUnitMeasure) == 1 ? Result.success(mdUnitMeasure) : Result.error("保存失败");
    }

    /**
     * 修改计量单位
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdUnitMeasure mdUnitMeasure) {
        return mdUnitMeasureService.updateById(mdUnitMeasure) == 1 ? Result.success(mdUnitMeasure) : Result.error("修改失败");
    }

    /**
     * 根据ID查询计量单位详情
     */
    @GetMapping("/{id}")
    public Result<MdUnitMeasure> queryById(@PathVariable Long id) {
        MdUnitMeasure mdUnitMeasure = mdUnitMeasureService.queryById(id);
        if (mdUnitMeasure == null) {
            return Result.error("计量单位不存在或已被删除");
        }
        return Result.success(mdUnitMeasure);
    }

    /**
     * 分页 + 多条件查询计量单位列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdUnitMeasure>> page(MdUnitMeasure mdUnitMeasure,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdUnitMeasureService.page(pageNum, pageSize, mdUnitMeasure));
    }

    /**
     * 删除计量单位（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdUnitMeasureService.deleteById(id));
    }

    /**
     * 批量删除计量单位
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdUnitMeasureService.deleteBatch(ids));
    }

    /**
     * 根据编码查询计量单位，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byMeasureCode/{measureCode}")
    public Result<MdUnitMeasure> queryByMeasureCode(@PathVariable String measureCode) {
        return Result.success(mdUnitMeasureService.queryByMeasureCode(measureCode));
    }

}
