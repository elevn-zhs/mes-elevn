package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdWorkstationMachine;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdWorkstationMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作站设备资源 Controller
 *
 * 工作站设备资源。描述该工位绑定了哪些设备、各几台。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/workstationMachine")
public class MdWorkstationMachineController {

    @Autowired
    private MdWorkstationMachineService mdWorkstationMachineService;

    /**
     * 新增工作站设备资源
     */
    @PostMapping
    public Result<MdWorkstationMachine> save(@RequestBody @Validated(CreateOption.class) MdWorkstationMachine mdWorkstationMachine) {
        return mdWorkstationMachineService.save(mdWorkstationMachine) == 1 ? Result.success(mdWorkstationMachine) : Result.error("保存失败");
    }

    /**
     * 修改工作站设备资源
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdWorkstationMachine mdWorkstationMachine) {
        return mdWorkstationMachineService.updateById(mdWorkstationMachine) == 1 ? Result.success(mdWorkstationMachine) : Result.error("修改失败");
    }

    /**
     * 根据ID查询工作站设备资源详情
     */
    @GetMapping("/{id}")
    public Result<MdWorkstationMachine> queryById(@PathVariable Long id) {
        MdWorkstationMachine mdWorkstationMachine = mdWorkstationMachineService.queryById(id);
        if (mdWorkstationMachine == null) {
            return Result.error("工作站设备资源不存在或已被删除");
        }
        return Result.success(mdWorkstationMachine);
    }

    /**
     * 分页 + 多条件查询工作站设备资源列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdWorkstationMachine>> page(MdWorkstationMachine mdWorkstationMachine,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdWorkstationMachineService.page(pageNum, pageSize, mdWorkstationMachine));
    }

    /**
     * 删除工作站设备资源（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdWorkstationMachineService.deleteById(id));
    }

    /**
     * 批量删除工作站设备资源
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdWorkstationMachineService.deleteBatch(ids));
    }

    /**
     * 按所属ID查询工作站设备资源列表
     */
    @GetMapping("/byWorkstationId/{workstationId}")
    public Result<List<MdWorkstationMachine>> queryByWorkstationId(@PathVariable Long workstationId) {
        return Result.success(mdWorkstationMachineService.queryByWorkstationId(workstationId));
    }

}
