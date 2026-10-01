package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysUserRole;
import com.elevn.mes.sys.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户分配角色 Controller
 *
 * 和 SysRoleMenuController 是一对：那个管「角色能干啥」，这个管「谁能扮演这个角色」。
 * 一个用户可以有多个角色，所以这里是典型的一对多中间表。
 *
 */
@RestController
@RequestMapping("/api/userRole")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 给用户重新分配角色（全量覆盖）
     * 前端：request.post('/userRole/assign?userId=1', [2,3])
     *
     * roleIds 传空数组等于把该用户所有角色清空，前端弹「取消」的时候别急着请求。
     */
    @PostMapping("/assign")
    public Result<Integer> assignRole(@RequestParam Long userId, @RequestBody Long[] roleIds){
        return Result.success(sysUserRoleService.assignRole(userId, roleIds));
    }

    /**
     * 查询某用户已拥有的角色ID，用于「分配角色」弹窗的勾选回显
     */
    @GetMapping("/roleIds/{userId}")
    public Result<List<Long>> selectRoleIdsByUserId(@PathVariable Long userId){
        return Result.success(sysUserRoleService.selectRoleIdsByUserId(userId));
    }

    @GetMapping("/page")
    public Result<PageInfo<SysUserRole>> page(SysUserRole sysUserRole,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(sysUserRoleService.page(pageNum, pageSize, sysUserRole));
    }

    @GetMapping("/{id}")
    public Result<SysUserRole> queryById(@PathVariable Long id){
        return Result.success(sysUserRoleService.queryById(id));
    }

    @PostMapping
    public Result<SysUserRole> save(@RequestBody SysUserRole sysUserRole){
        return sysUserRoleService.save(sysUserRole) == 1 ? Result.success(sysUserRole) : Result.error("保存失败");
    }

    @PutMapping
    public Result updateById(@RequestBody SysUserRole sysUserRole){
        return sysUserRoleService.updateById(sysUserRole) == 1 ? Result.success(sysUserRole) : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(sysUserRoleService.deleteById(id));
    }
}
