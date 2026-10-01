package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysRole;
import com.elevn.mes.sys.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class SysRoleController {
    @Autowired
    private SysRoleService roleService;

    @GetMapping("/all")
    public Result<List<SysRole>> all(){
        return Result.success(roleService.queryAll());
    }

    @PostMapping
    public Result<SysRole> save(@RequestBody SysRole role){
        return roleService.save(role) == 1?Result.success(role):Result.error("保存失败");
    };

    @GetMapping("/{id}")
    public Result<SysRole> queryById(@PathVariable long id){
        return Result.success(roleService.queryById(id));
    }
    @GetMapping("/page")
    public Result<PageInfo<SysRole>> page(SysRole role,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(roleService.page(pageNum,pageSize,role));
    }
    @PutMapping
    public Result updateById(@RequestBody SysRole role){
        return roleService.updateById(role) == 1?Result.success(role):Result.error("修改失败");
    };

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(roleService.deleteById(id));
    };

}
