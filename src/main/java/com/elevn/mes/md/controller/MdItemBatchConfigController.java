package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdItemBatchConfig;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdItemBatchConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 物料批次属性配置 Controller
 *
 * 批次属性配置。控制该物料入库时需要采集哪些批次属性（生产日期/有效期/供应商等）。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/itemBatchConfig")
public class MdItemBatchConfigController {

    @Autowired
    private MdItemBatchConfigService mdItemBatchConfigService;

    /**
     * 新增物料批次属性配置
     */
    @PostMapping
    public Result<MdItemBatchConfig> save(@RequestBody @Validated(CreateOption.class) MdItemBatchConfig mdItemBatchConfig) {
        return mdItemBatchConfigService.save(mdItemBatchConfig) == 1 ? Result.success(mdItemBatchConfig) : Result.error("保存失败");
    }

    /**
     * 修改物料批次属性配置
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdItemBatchConfig mdItemBatchConfig) {
        return mdItemBatchConfigService.updateById(mdItemBatchConfig) == 1 ? Result.success(mdItemBatchConfig) : Result.error("修改失败");
    }

    /**
     * 根据ID查询物料批次属性配置详情
     */
    @GetMapping("/{id}")
    public Result<MdItemBatchConfig> queryById(@PathVariable Long id) {
        MdItemBatchConfig mdItemBatchConfig = mdItemBatchConfigService.queryById(id);
        if (mdItemBatchConfig == null) {
            return Result.error("物料批次属性配置不存在或已被删除");
        }
        return Result.success(mdItemBatchConfig);
    }

    /**
     * 分页 + 多条件查询物料批次属性配置列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdItemBatchConfig>> page(MdItemBatchConfig mdItemBatchConfig,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdItemBatchConfigService.page(pageNum, pageSize, mdItemBatchConfig));
    }

    /**
     * 删除物料批次属性配置（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdItemBatchConfigService.deleteById(id));
    }

    /**
     * 批量删除物料批次属性配置
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdItemBatchConfigService.deleteBatch(ids));
    }

    /**
     * 按所属ID查询物料批次属性配置
     */
    @GetMapping("/byItemId/{itemId}")
    public Result<MdItemBatchConfig> queryByItemId(@PathVariable Long itemId) {
        return Result.success(mdItemBatchConfigService.queryByItemId(itemId));
    }

}
