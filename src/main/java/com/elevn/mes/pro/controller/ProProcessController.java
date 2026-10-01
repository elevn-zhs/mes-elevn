package com.elevn.mes.pro.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProProcess;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import com.elevn.mes.pro.service.ProProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工序 Controller
 *
 * 工艺管理的第一个页面。工序是最基础的字典数据，
 * 后面的工艺路线、工作站、排产全都建立在它上面。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/pro/process")
public class ProProcessController {

    @Autowired
    private ProProcessService proProcessService;

    /**
     * 新增工序
     */
    @PostMapping
    public Result<ProProcess> save(@RequestBody @Validated(CreateOption.class) ProProcess proProcess) {
        return proProcessService.save(proProcess) == 1 ? Result.success(proProcess) : Result.error("保存失败");
    }

    /**
     * 修改工序
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) ProProcess proProcess) {
        return proProcessService.updateById(proProcess) == 1 ? Result.success(proProcess) : Result.error("修改失败");
    }

    /**
     * 根据ID查询工序详情（含工序内容列表）
     */
    @GetMapping("/{id}")
    public Result<ProProcess> queryById(@PathVariable Long id) {
        ProProcess proProcess = proProcessService.queryById(id);
        if (proProcess == null) {
            return Result.error("工序不存在或已被删除");
        }
        return Result.success(proProcess);
    }

    /**
     * 分页 + 多条件查询工序列表
     */
    @GetMapping("/page")
    public Result<PageInfo<ProProcess>> page(ProProcess proProcess,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(proProcessService.page(pageNum, pageSize, proProcess));
    }

    /**
     * 查询全部启用的工序（工艺路线、工作站选工序的下拉数据源）
     */
    @GetMapping("/all")
    public Result<List<ProProcess>> queryAllEnabled() {
        return Result.success(proProcessService.queryAllEnabled());
    }

    /**
     * 删除工序（逻辑删除；被工作站/工艺路线引用时后端会拒绝）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(proProcessService.deleteById(id));
    }

    /**
     * 批量删除工序
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(proProcessService.deleteBatch(ids));
    }

    /**
     * 根据编码查询工序，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byProcessCode/{processCode}")
    public Result<ProProcess> queryByProcessCode(@PathVariable String processCode) {
        return Result.success(proProcessService.queryByProcessCode(processCode));
    }
}
