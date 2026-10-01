package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdProductSip;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdProductSipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 产品SIP Controller
 *
 * 产品SIP（标准检验指导书），按产品+工序维护检验方法与判定标准。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/productSip")
public class MdProductSipController {

    @Autowired
    private MdProductSipService mdProductSipService;

    /**
     * 新增产品SIP
     */
    @PostMapping
    public Result<MdProductSip> save(@RequestBody @Validated(CreateOption.class) MdProductSip mdProductSip) {
        return mdProductSipService.save(mdProductSip) == 1 ? Result.success(mdProductSip) : Result.error("保存失败");
    }

    /**
     * 修改产品SIP
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdProductSip mdProductSip) {
        return mdProductSipService.updateById(mdProductSip) == 1 ? Result.success(mdProductSip) : Result.error("修改失败");
    }

    /**
     * 根据ID查询产品SIP详情
     */
    @GetMapping("/{id}")
    public Result<MdProductSip> queryById(@PathVariable Long id) {
        MdProductSip mdProductSip = mdProductSipService.queryById(id);
        if (mdProductSip == null) {
            return Result.error("产品SIP不存在或已被删除");
        }
        return Result.success(mdProductSip);
    }

    /**
     * 分页 + 多条件查询产品SIP列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdProductSip>> page(MdProductSip mdProductSip,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdProductSipService.page(pageNum, pageSize, mdProductSip));
    }

    /**
     * 删除产品SIP（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdProductSipService.deleteById(id));
    }

    /**
     * 批量删除产品SIP
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdProductSipService.deleteBatch(ids));
    }

    /**
     * 按所属ID查询产品SIP列表
     */
    @GetMapping("/byItemId/{itemId}")
    public Result<List<MdProductSip>> queryByItemId(@PathVariable Long itemId) {
        return Result.success(mdProductSipService.queryByItemId(itemId));
    }

}
