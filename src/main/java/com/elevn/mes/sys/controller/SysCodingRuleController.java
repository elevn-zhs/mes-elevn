package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysCodingRule;
import com.elevn.mes.sys.service.SysCodingRuleService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/codingRule")
public class SysCodingRuleController {
    @Autowired
    private SysCodingRuleService codingRuleService;


    @GetMapping("/auto")
    public Result<String> auto(String ruleCode){
        return Result.success(codingRuleService.autoCode(ruleCode));
    }

    @GetMapping("/queryByCode")
    public Result<SysCodingRule> queryByCode(String ruleCode){
        return Result.success(codingRuleService.selectByRuleCode(ruleCode));
    }

    @PostMapping
    public Result<SysCodingRule> save(@RequestBody SysCodingRule codingRule){
        return codingRuleService.save(codingRule) == 1?Result.success(codingRule):Result.error("保存失败");
    };

    @GetMapping("/{id}")
    public Result<SysCodingRule> queryById(@PathVariable long id){
        return Result.success(codingRuleService.queryById(id));
    }
    @GetMapping("/page")
    public Result<PageInfo<SysCodingRule>> page(SysCodingRule codingRule,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(codingRuleService.page(pageNum,pageSize,codingRule));
    }
    @PutMapping
    public Result updateById(@RequestBody SysCodingRule codingRule){
        return codingRuleService.updateById(codingRule) == 1?Result.success(codingRule):Result.error("修改失败");
    };

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(codingRuleService.deleteById(id));
    };

}
