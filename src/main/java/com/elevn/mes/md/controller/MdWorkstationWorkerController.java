package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdWorkstationWorker;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdWorkstationWorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作站人力资源 Controller
 *
 * 工作站人力资源。描述该工位需要哪些岗位、各几人。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/workstationWorker")
public class MdWorkstationWorkerController {

    @Autowired
    private MdWorkstationWorkerService mdWorkstationWorkerService;

    /**
     * 新增工作站人力资源
     */
    @PostMapping
    public Result<MdWorkstationWorker> save(@RequestBody @Validated(CreateOption.class) MdWorkstationWorker mdWorkstationWorker) {
        return mdWorkstationWorkerService.save(mdWorkstationWorker) == 1 ? Result.success(mdWorkstationWorker) : Result.error("保存失败");
    }

    /**
     * 修改工作站人力资源
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdWorkstationWorker mdWorkstationWorker) {
        return mdWorkstationWorkerService.updateById(mdWorkstationWorker) == 1 ? Result.success(mdWorkstationWorker) : Result.error("修改失败");
    }

    /**
     * 根据ID查询工作站人力资源详情
     */
    @GetMapping("/{id}")
    public Result<MdWorkstationWorker> queryById(@PathVariable Long id) {
        MdWorkstationWorker mdWorkstationWorker = mdWorkstationWorkerService.queryById(id);
        if (mdWorkstationWorker == null) {
            return Result.error("工作站人力资源不存在或已被删除");
        }
        return Result.success(mdWorkstationWorker);
    }

    /**
     * 分页 + 多条件查询工作站人力资源列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdWorkstationWorker>> page(MdWorkstationWorker mdWorkstationWorker,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdWorkstationWorkerService.page(pageNum, pageSize, mdWorkstationWorker));
    }

    /**
     * 删除工作站人力资源（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdWorkstationWorkerService.deleteById(id));
    }

    /**
     * 批量删除工作站人力资源
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdWorkstationWorkerService.deleteBatch(ids));
    }

    /**
     * 按所属ID查询工作站人力资源列表
     */
    @GetMapping("/byWorkstationId/{workstationId}")
    public Result<List<MdWorkstationWorker>> queryByWorkstationId(@PathVariable Long workstationId) {
        return Result.success(mdWorkstationWorkerService.queryByWorkstationId(workstationId));
    }

}
