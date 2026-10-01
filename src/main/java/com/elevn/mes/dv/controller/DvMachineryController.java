package com.elevn.mes.dv.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.dv.entity.DvMachinery;
import com.elevn.mes.dv.service.DvMachineryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备 Controller
 *
 * 本次只提供"工作站详情页选料下拉"需要的两个查询接口：
 *   GET /api/dv/machinery/page  分页条件查询（下拉里搜索用）
 *   GET /api/dv/machinery/{id}  按 ID 查详情（回显用）
 *
 * 设备的新增 / 修改 / 删除属于设备模块（dv 线）的职责，留给分组同学实现，
 * 这里不越界，避免把学生要做的活全写完。
 *
 */
@RestController
@RequestMapping("/api/dv/machinery")
public class DvMachineryController {

    @Autowired
    private DvMachineryService dvMachineryService;

    /**
     * 分页 + 多条件查询设备列表
     */
    @GetMapping("/page")
    public Result<PageInfo<DvMachinery>> page(DvMachinery dvMachinery,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(dvMachineryService.page(pageNum, pageSize, dvMachinery));
    }

    /**
     * 根据ID查询设备详情
     */
    @GetMapping("/{id}")
    public Result<DvMachinery> queryById(@PathVariable Long id) {
        DvMachinery dvMachinery = dvMachineryService.queryById(id);
        if (dvMachinery == null) {
            return Result.error("设备不存在或已被删除");
        }
        return Result.success(dvMachinery);
    }

}
