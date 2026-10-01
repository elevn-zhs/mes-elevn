package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdWorkshop;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdWorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 车间 Controller
 *
 * 车间。工作站的上一级组织维度。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/workshop")
public class MdWorkshopController {

    @Autowired
    private MdWorkshopService mdWorkshopService;

    /**
     * 新增车间
     */
    @PostMapping
    public Result<MdWorkshop> save(@RequestBody @Validated(CreateOption.class) MdWorkshop mdWorkshop) {
        return mdWorkshopService.save(mdWorkshop) == 1 ? Result.success(mdWorkshop) : Result.error("保存失败");
    }

    /**
     * 修改车间
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdWorkshop mdWorkshop) {
        return mdWorkshopService.updateById(mdWorkshop) == 1 ? Result.success(mdWorkshop) : Result.error("修改失败");
    }

    /**
     * 根据ID查询车间详情
     */
    @GetMapping("/{id}")
    public Result<MdWorkshop> queryById(@PathVariable Long id) {
        MdWorkshop mdWorkshop = mdWorkshopService.queryById(id);
        if (mdWorkshop == null) {
            return Result.error("车间不存在或已被删除");
        }
        return Result.success(mdWorkshop);
    }

    /**
     * 分页 + 多条件查询车间列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdWorkshop>> page(MdWorkshop mdWorkshop,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdWorkshopService.page(pageNum, pageSize, mdWorkshop));
    }

    /**
     * 删除车间（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdWorkshopService.deleteById(id));
    }

    /**
     * 批量删除车间
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdWorkshopService.deleteBatch(ids));
    }

    /**
     * 根据编码查询车间，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byWorkshopCode/{workshopCode}")
    public Result<MdWorkshop> queryByWorkshopCode(@PathVariable String workshopCode) {
        return Result.success(mdWorkshopService.queryByWorkshopCode(workshopCode));
    }

}
