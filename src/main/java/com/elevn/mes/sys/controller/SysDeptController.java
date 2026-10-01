package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysDept;
import com.elevn.mes.sys.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dept")
public class SysDeptController {
    @Autowired
    private SysDeptService deptService;
    
    @GetMapping("/all")
    public Result<List<SysDept>> queryAll(){
        return Result.success(deptService.queryAllForTree());
    }

    @PostMapping
    public Result<SysDept> save(@RequestBody SysDept dept){
        return deptService.save(dept) == 1?Result.success(dept):Result.error("保存失败");
    };

    @GetMapping("/{id}")
    public Result<SysDept> queryById(@PathVariable long id){
        return Result.success(deptService.queryById(id));
    }
    @GetMapping("/page")
    public Result<PageInfo<SysDept>> page(SysDept dept,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(deptService.page(pageNum,pageSize,dept));
    }
    @PutMapping
    public Result updateById(@RequestBody SysDept dept){
        return deptService.updateById(dept) == 1?Result.success(dept):Result.error("修改失败");
    };

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(deptService.deleteById(id));
    };

}
