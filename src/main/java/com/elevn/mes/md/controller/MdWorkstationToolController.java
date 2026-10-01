package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdWorkstationTool;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdWorkstationToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作站工装夹具 Controller
 *
 * 工作站工装夹具资源。按类型（tool_type）而非单个实物管理。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/workstationTool")
public class MdWorkstationToolController {

    @Autowired
    private MdWorkstationToolService mdWorkstationToolService;

    /**
     * 新增工作站工装夹具
     */
    @PostMapping
    public Result<MdWorkstationTool> save(@RequestBody @Validated(CreateOption.class) MdWorkstationTool mdWorkstationTool) {
        return mdWorkstationToolService.save(mdWorkstationTool) == 1 ? Result.success(mdWorkstationTool) : Result.error("保存失败");
    }

    /**
     * 修改工作站工装夹具
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdWorkstationTool mdWorkstationTool) {
        return mdWorkstationToolService.updateById(mdWorkstationTool) == 1 ? Result.success(mdWorkstationTool) : Result.error("修改失败");
    }

    /**
     * 根据ID查询工作站工装夹具详情
     */
    @GetMapping("/{id}")
    public Result<MdWorkstationTool> queryById(@PathVariable Long id) {
        MdWorkstationTool mdWorkstationTool = mdWorkstationToolService.queryById(id);
        if (mdWorkstationTool == null) {
            return Result.error("工作站工装夹具不存在或已被删除");
        }
        return Result.success(mdWorkstationTool);
    }

    /**
     * 分页 + 多条件查询工作站工装夹具列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdWorkstationTool>> page(MdWorkstationTool mdWorkstationTool,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdWorkstationToolService.page(pageNum, pageSize, mdWorkstationTool));
    }

    /**
     * 删除工作站工装夹具（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdWorkstationToolService.deleteById(id));
    }

    /**
     * 批量删除工作站工装夹具
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdWorkstationToolService.deleteBatch(ids));
    }

    /**
     * 按所属ID查询工作站工装夹具列表
     */
    @GetMapping("/byWorkstationId/{workstationId}")
    public Result<List<MdWorkstationTool>> queryByWorkstationId(@PathVariable Long workstationId) {
        return Result.success(mdWorkstationToolService.queryByWorkstationId(workstationId));
    }

}
