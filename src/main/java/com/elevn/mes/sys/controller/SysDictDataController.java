package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysDictData;
import com.elevn.mes.sys.service.SysDictDataService;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dictData")
public class SysDictDataController {
    @Autowired
    private SysDictDataService dictDataService;


    @GetMapping("/queryByType")
    public Result<List<SysDictData>> queryByType(@NotBlank(message = "参数有误，字典类别不能为空")  String dictType){
        return Result.success(dictDataService.queryByType(dictType));
    }

    @PostMapping
    public Result<SysDictData> save(@RequestBody SysDictData dictData){
        return dictDataService.save(dictData) == 1?Result.success(dictData):Result.error("保存失败");
    };

    @GetMapping("/{id}")
    public Result<SysDictData> queryById(@PathVariable long id){
        return Result.success(dictDataService.queryById(id));
    }
    @GetMapping("/page")
    public Result<PageInfo<SysDictData>> page(SysDictData dictData,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(dictDataService.page(pageNum,pageSize,dictData));
    }
    @PutMapping
    public Result updateById(@RequestBody SysDictData dictData){
        return dictDataService.updateById(dictData) == 1?Result.success(dictData):Result.error("修改失败");
    };

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(dictDataService.deleteById(id));
    };

}
