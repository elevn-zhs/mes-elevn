package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdItemSubstitute;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdItemSubstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料替代品 Controller
 *
 * 一行 = 「主物料 item_id 可以被替代物料 sub_item_id 顶替」的一条替代关系。
 *
 * substitute_type 决定方向：
 *   ONE_WAY 单向：只有主物料能被替代物料顶替，反过来不成立（物料存在多条替代方案时用这种）；
 *   TWO_WAY 双向：两个物料互为替代，一条记录即可，查询时两个方向都要认。
 *
 * substitute_ratio 是用量比：每消耗 1 个主物料，需要用掉多少个替代物料。
 * 默认 1 表示等量替换；换成规格不完全一样的料时会不等于 1（例如计入加工损耗的 1.05）。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/itemSubstitute")
public class MdItemSubstituteController {

    @Autowired
    private MdItemSubstituteService mdItemSubstituteService;

    /**
     * 新增物料替代品
     */
    @PostMapping
    public Result<MdItemSubstitute> save(@RequestBody @Validated(CreateOption.class) MdItemSubstitute mdItemSubstitute) {
        return mdItemSubstituteService.save(mdItemSubstitute) == 1 ? Result.success(mdItemSubstitute) : Result.error("保存失败");
    }

    /**
     * 修改物料替代品
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdItemSubstitute mdItemSubstitute) {
        return mdItemSubstituteService.updateById(mdItemSubstitute) == 1 ? Result.success(mdItemSubstitute) : Result.error("修改失败");
    }

    /**
     * 根据ID查询物料替代品详情
     */
    @GetMapping("/{id}")
    public Result<MdItemSubstitute> queryById(@PathVariable Long id) {
        MdItemSubstitute mdItemSubstitute = mdItemSubstituteService.queryById(id);
        if (mdItemSubstitute == null) {
            return Result.error("物料替代品不存在或已被删除");
        }
        return Result.success(mdItemSubstitute);
    }

    /**
     * 分页 + 多条件查询物料替代品列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdItemSubstitute>> page(MdItemSubstitute mdItemSubstitute,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdItemSubstituteService.page(pageNum, pageSize, mdItemSubstitute));
    }

    /**
     * 删除（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdItemSubstituteService.deleteById(id));
    }

    /**
     * 批量删除物料替代品
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdItemSubstituteService.deleteBatch(ids));
    }

    /**
     * 按物料ID查询物料替代品列表（物料详情 tab 用）
     */
    @GetMapping("/byItemId/{itemId}")
    public Result<List<MdItemSubstitute>> queryByItemId(@PathVariable Long itemId) {
        return Result.success(mdItemSubstituteService.queryByItemId(itemId));
    }

}
