package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdWorkstation;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdWorkstationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作站 Controller
 *
 * 工作站。生产执行最小单元，串起车间 / 工序 / 线边库 / 线边库位四条线。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/workstation")
public class MdWorkstationController {

    @Autowired
    private MdWorkstationService mdWorkstationService;

    /**
     * 新增工作站
     */
    @PostMapping
    public Result<MdWorkstation> save(@RequestBody @Validated(CreateOption.class) MdWorkstation mdWorkstation) {
        return mdWorkstationService.save(mdWorkstation) == 1 ? Result.success(mdWorkstation) : Result.error("保存失败");
    }

    /**
     * 修改工作站
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdWorkstation mdWorkstation) {
        return mdWorkstationService.updateById(mdWorkstation) == 1 ? Result.success(mdWorkstation) : Result.error("修改失败");
    }

    /**
     * 根据ID查询工作站详情
     */
    @GetMapping("/{id}")
    public Result<MdWorkstation> queryById(@PathVariable Long id) {
        MdWorkstation mdWorkstation = mdWorkstationService.queryById(id);
        if (mdWorkstation == null) {
            return Result.error("工作站不存在或已被删除");
        }
        return Result.success(mdWorkstation);
    }

    /**
     * 分页 + 多条件查询工作站列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdWorkstation>> page(MdWorkstation mdWorkstation,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdWorkstationService.page(pageNum, pageSize, mdWorkstation));
    }

    /**
     * 删除工作站（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdWorkstationService.deleteById(id));
    }

    /**
     * 批量删除工作站
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdWorkstationService.deleteBatch(ids));
    }

    /**
     * 根据编码查询工作站，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byWorkstationCode/{workstationCode}")
    public Result<MdWorkstation> queryByWorkstationCode(@PathVariable String workstationCode) {
        return Result.success(mdWorkstationService.queryByWorkstationCode(workstationCode));
    }

    /**
     * 按工序ID查询可承担该工序的启用工作站（排产选工作站的下拉数据源）
     *
     * 一道工序可能对应多个工作站（如"焊接"既有手工焊接站也有波峰焊线），
     * 排产时要让用户按实际产能选，所以返回列表而不是单条。
     */
    @GetMapping("/byProcessId/{processId}")
    public Result<List<MdWorkstation>> queryByProcessId(@PathVariable Long processId) {
        return Result.success(mdWorkstationService.queryByProcessId(processId));
    }

}
