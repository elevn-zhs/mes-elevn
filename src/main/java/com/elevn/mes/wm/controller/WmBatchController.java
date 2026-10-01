package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmBatch;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import com.elevn.mes.wm.service.WmBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 批次 Controller
 *
 * 对应 wm_batch。批次是追溯的抓手：同一物料来自不同供应商、不同生产日期的，
 * 在库存账上是不同的行。
 *
 * 【批次要带哪些属性由物料决定】
 * 新增时后端会去查 E 线的 md_item_batch_config（按物料的 14 个属性开关），
 * 哪个开关是 Y 就要求哪个字段必填。前端拉到配置后可以只显示需要填的字段，
 * 但校验以服务端为准。
 *
 */
@RestController
@RequestMapping("/api/wm/batch")
public class WmBatchController {

    @Autowired
    private WmBatchService wmBatchService;

    /**
     * 新增批次（批次编号由后端生成，不用传）
     */
    @PostMapping
    public Result<WmBatch> save(@RequestBody @Validated(CreateOption.class) WmBatch wmBatch) {
        return wmBatchService.save(wmBatch) == 1 ? Result.success(wmBatch) : Result.error("保存失败");
    }

    /**
     * 修改批次（批次编号与所属物料不允许修改）
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) WmBatch wmBatch) {
        return wmBatchService.updateById(wmBatch) == 1 ? Result.success(wmBatch) : Result.error("修改失败");
    }

    /**
     * 根据ID查询批次详情（含库存分布与库存总量）
     */
    @GetMapping("/{id}")
    public Result<WmBatch> queryById(@PathVariable Long id) {
        WmBatch wmBatch = wmBatchService.queryById(id);
        if (wmBatch == null) {
            return Result.error("批次不存在或已被删除");
        }
        return Result.success(wmBatch);
    }

    /**
     * 分页 + 多条件查询批次列表（支持物料/批号/生产日期区间筛选）
     */
    @GetMapping("/page")
    public Result<PageInfo<WmBatch>> page(WmBatch wmBatch,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmBatchService.page(pageNum, pageSize, wmBatch));
    }

    /**
     * 删除批次（逻辑删除；还有库存时后端会拒绝）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(wmBatchService.deleteById(id));
    }

    /**
     * 批量删除批次
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(wmBatchService.deleteBatch(ids));
    }
}
