package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdItemType;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料产品分类 Controller
 *
 * 物料/产品分类树。parent_type_id=0 为顶级；ancestors 存祖级路径便于一次查整棵子树。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/itemType")
public class MdItemTypeController {

    @Autowired
    private MdItemTypeService mdItemTypeService;


    @GetMapping("/queryByParentId")
    public  Result<List<MdItemType>> queryByParentId(Long parentId){
        return Result.success(mdItemTypeService.queryByParentId(parentId));
    };


    @GetMapping("/tree")
    public Result<List<MdItemType>> tree(String type){
        return Result.success(mdItemTypeService.queryTree(type));
    }

    /**
     * 新增物料产品分类
     */
    @PostMapping
    public Result<MdItemType> save(@RequestBody @Validated(CreateOption.class) MdItemType mdItemType) {
        return mdItemTypeService.save(mdItemType) == 1 ? Result.success(mdItemType) : Result.error("保存失败");
    }

    /**
     * 修改物料产品分类
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdItemType mdItemType) {
        return mdItemTypeService.updateById(mdItemType) == 1 ? Result.success(mdItemType) : Result.error("修改失败");
    }

    /**
     * 根据ID查询物料产品分类详情
     */
    @GetMapping("/{id}")
    public Result<MdItemType> queryById(@PathVariable Long id) {
        MdItemType mdItemType = mdItemTypeService.queryById(id);
        if (mdItemType == null) {
            return Result.error("物料产品分类不存在或已被删除");
        }
        return Result.success(mdItemType);
    }

    /**
     * 分页 + 多条件查询物料产品分类列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdItemType>> page(MdItemType mdItemType,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdItemTypeService.page(pageNum, pageSize, mdItemType));
    }

    /**
     * 删除物料产品分类（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdItemTypeService.deleteById(id));
    }

    /**
     * 批量删除物料产品分类
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdItemTypeService.deleteBatch(ids));
    }

    /**
     * 根据编码查询物料产品分类，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byItemTypeCode/{itemTypeCode}")
    public Result<MdItemType> queryByItemTypeCode(@PathVariable String itemTypeCode) {
        return Result.success(mdItemTypeService.queryByItemTypeCode(itemTypeCode));
    }

}
