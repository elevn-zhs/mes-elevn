package com.elevn.mes.tm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.tm.entity.TmToolType;
import com.elevn.mes.tm.service.TmToolTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工装夹具类型 Controller
 *
 * 本次只提供"工作站详情页选料下拉"需要的两个查询接口：
 *   GET /api/tm/toolType/page  分页条件查询（下拉里搜索用）
 *   GET /api/tm/toolType/{id}  按 ID 查详情（回显用）
 *
 * 工装类型的新增 / 修改 / 删除属于工装模块（tm 线）的职责，留给分组同学实现，
 * 这里不越界，避免把学生要做的活全写完。
 *
 */
@RestController
@RequestMapping("/api/tm/toolType")
public class TmToolTypeController {

    @Autowired
    private TmToolTypeService tmToolTypeService;

    /**
     * 分页 + 多条件查询工装夹具类型列表
     */
    @GetMapping("/page")
    public Result<PageInfo<TmToolType>> page(TmToolType tmToolType,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(tmToolTypeService.page(pageNum, pageSize, tmToolType));
    }

    /**
     * 根据ID查询工装夹具类型详情
     */
    @GetMapping("/{id}")
    public Result<TmToolType> queryById(@PathVariable Long id) {
        TmToolType tmToolType = tmToolTypeService.queryById(id);
        if (tmToolType == null) {
            return Result.error("工装夹具类型不存在或已被删除");
        }
        return Result.success(tmToolType);
    }

}
