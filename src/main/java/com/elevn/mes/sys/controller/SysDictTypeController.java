package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysDictType;
import com.elevn.mes.sys.options.UpdateOption;
import com.elevn.mes.sys.service.SysDictTypeService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dictType")
public class SysDictTypeController {
    @Autowired
    private SysDictTypeService dictTypeService;

    @GetMapping("/queryByType")
    public Result<SysDictType> queryByType(@NotBlank(message = "字典类型不能空") @Length(min = 3,max = 50, message = "字典类型的长度应该在3~50之前") String dictType){
        return Result.success(dictTypeService.queryByType(dictType));
    }

    @PostMapping("/deleteBatch")
    public Result<Integer> deleteBatch(@RequestBody Long [] ids){
        return Result.success(dictTypeService.deleteBatch(ids));
    }

    @PostMapping
    public Result<SysDictType> save(@RequestBody @Validated SysDictType dictType){
        return dictTypeService.save(dictType) == 1?Result.success(dictType):Result.error("保存失败");
    };
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) SysDictType dictType){
        return dictTypeService.updateById(dictType) == 1?Result.success(dictType):Result.error("修改失败");
    };

    @GetMapping("/{id}")
    public Result<SysDictType> queryById(@PathVariable long id){
        return Result.success(dictTypeService.queryById(id));
    }
    @GetMapping("/page")
    public Result<PageInfo<SysDictType>> page(SysDictType dictType,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(dictTypeService.page(pageNum,pageSize,dictType));
    }


    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(dictTypeService.deleteById(id));
    };

}
