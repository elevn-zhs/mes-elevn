package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdProductBom;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdProductBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品BOM Controller
 *
 * 产品BOM明细。一行=父件(item_id)用到一个子件(bom_item_id)的用量关系。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/bom")
public class MdProductBomController {

    @Autowired
    private MdProductBomService mdProductBomService;

    /**
     * 新增产品BOM
     */
    @PostMapping
    public Result<MdProductBom> save(@RequestBody @Validated(CreateOption.class) MdProductBom mdProductBom) {
        return mdProductBomService.save(mdProductBom) == 1 ? Result.success(mdProductBom) : Result.error("保存失败");
    }

    /**
     * 修改产品BOM
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdProductBom mdProductBom) {
        return mdProductBomService.updateById(mdProductBom) == 1 ? Result.success(mdProductBom) : Result.error("修改失败");
    }

    /**
     * 根据ID查询产品BOM详情
     */
    @GetMapping("/{id}")
    public Result<MdProductBom> queryById(@PathVariable Long id) {
        MdProductBom mdProductBom = mdProductBomService.queryById(id);
        if (mdProductBom == null) {
            return Result.error("产品BOM不存在或已被删除");
        }
        return Result.success(mdProductBom);
    }

    /**
     * 分页 + 多条件查询产品BOM列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdProductBom>> page(MdProductBom mdProductBom,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdProductBomService.page(pageNum, pageSize, mdProductBom));
    }

    /**
     * 删除产品BOM（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdProductBomService.deleteById(id));
    }

    /**
     * 批量删除产品BOM
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdProductBomService.deleteBatch(ids));
    }

    /**
     * 按所属ID查询产品BOM列表
     */
    @GetMapping("/byItemId/{itemId}")
    public Result<List<MdProductBom>> queryByItemId(@PathVariable Long itemId) {
        return Result.success(mdProductBomService.queryByItemId(itemId));
    }

}
