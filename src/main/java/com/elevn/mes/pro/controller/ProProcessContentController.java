package com.elevn.mes.pro.controller;

import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProProcessContent;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import com.elevn.mes.pro.service.ProProcessContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工序内容 Controller
 *
 * 工序的子表。前端在工序详情页的「工序内容」Tab 里维护，
 * 所以接口只需要三件套：按工序查全量、保存、删除 —— 没有分页。
 *
 */
@RestController
@RequestMapping("/api/pro/processContent")
public class ProProcessContentController {

    @Autowired
    private ProProcessContentService proProcessContentService;

    /**
     * 按工序ID查询全部内容（按 order_num 升序）
     */
    @GetMapping("/byProcessId/{processId}")
    public Result<List<ProProcessContent>> selectByProcessId(@PathVariable Long processId) {
        return Result.success(proProcessContentService.selectByProcessId(processId));
    }

    /**
     * 新增工序内容
     */
    @PostMapping
    public Result<ProProcessContent> save(@RequestBody @Validated(CreateOption.class) ProProcessContent proProcessContent) {
        return proProcessContentService.save(proProcessContent) == 1 ? Result.success(proProcessContent) : Result.error("保存失败");
    }

    /**
     * 修改工序内容
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) ProProcessContent proProcessContent) {
        return proProcessContentService.updateById(proProcessContent) == 1 ? Result.success(proProcessContent) : Result.error("修改失败");
    }

    /**
     * 删除工序内容
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(proProcessContentService.deleteById(id));
    }

    /**
     * 批量删除工序内容
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(proProcessContentService.deleteBatch(ids));
    }
}
