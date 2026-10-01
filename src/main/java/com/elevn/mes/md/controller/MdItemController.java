package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 物料产品 Controller
 *
 * 物料产品主数据。item_or_product 区分物料/产品；batch_flag=Y 时出入库必须录批次号。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/item")
public class MdItemController {

    @Autowired
    private MdItemService mdItemService;


    /**
     * BOM 选料专用分页查询（新增接口，不动原来的 /page）
     *
     * 给某个物料挑 BOM 子件时调用：在普通多条件查询的基础上，
     * 自动排除当前物料自身、它的所有上级（祖先）和所有下级（子孙），避免选出闭环或重复嵌套的物料。
     *
     * excludeItemId 写成 required = false，是为了让"没传参数"走到 Service 里
     * 抛出 BusinessException（人话提示），而不是被框架拦成 400 参数绑定错误。
     *
     * 注意：本路径 /pageForBom 是字面量路径，Spring 匹配优先级高于 /{id}，不会被详情接口吃掉。
     */
    @GetMapping("/pageForBom")
    public Result<PageInfo<MdItem>> pageForBom(MdItem mdItem,
                                               @RequestParam(required = false) Long excludeItemId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdItemService.pageForBom(pageNum, pageSize, mdItem, excludeItemId));
    }

    /**
     * 新增物料产品
     */
    @PostMapping
    public Result<MdItem> save(@RequestBody @Validated(CreateOption.class) MdItem mdItem) {
        return mdItemService.save(mdItem) == 1 ? Result.success(mdItem) : Result.error("保存失败");
    }

    /**
     * 修改物料产品
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdItem mdItem) {
        return mdItemService.updateById(mdItem) == 1 ? Result.success(mdItem) : Result.error("修改失败");
    }

    /**
     * 根据ID查询物料产品详情
     */
    @GetMapping("/{id}")
    public Result<MdItem> queryById(@PathVariable Long id) {
        MdItem mdItem = mdItemService.queryById(id);
        if (mdItem == null) {
            return Result.error("物料产品不存在或已被删除");
        }
        return Result.success(mdItem);
    }

    /**
     * 分页 + 多条件查询物料产品列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdItem>> page(MdItem mdItem,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdItemService.page(pageNum, pageSize, mdItem));
    }

    /**
     * 删除物料产品（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdItemService.deleteById(id));
    }

    /**
     * 批量删除物料产品
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdItemService.deleteBatch(ids));
    }

    /**
     * 根据编码查询物料产品，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byItemCode/{itemCode}")
    public Result<MdItem> queryByItemCode(@PathVariable String itemCode) {
        return Result.success(mdItemService.queryByItemCode(itemCode));
    }

}
