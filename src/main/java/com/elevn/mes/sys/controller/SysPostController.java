package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysPost;
import com.elevn.mes.sys.service.SysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 岗位管理 Controller
 *
 * 岗位（post）和角色（role）是两回事，讲课的时候一定要掰开讲清楚：
 *   岗位 —— 一个人「是什么」，比如 车间主任、质检员，是行政编制，一人一岗
 *   角色 —— 一个人「能干啥」，比如 仓库管理员、质检主管，是系统权限的集合，一人可多角色
 * 所以 sys_user 里 post_id 是单值字段，而 user 和 role 之间还得靠 sys_user_role 中间表。
 *
 */
@RestController
@RequestMapping("/api/post")
public class SysPostController {

    @Autowired
    private SysPostService sysPostService;

    @PostMapping
    public Result<SysPost> save(@RequestBody @Validated SysPost sysPost){
        return sysPostService.save(sysPost) == 1 ? Result.success(sysPost) : Result.error("保存失败");
    }

    @PutMapping
    public Result updateById(@RequestBody @Validated SysPost sysPost){
        return sysPostService.updateById(sysPost) == 1 ? Result.success(sysPost) : Result.error("修改失败");
    }

    @GetMapping("/{id}")
    public Result<SysPost> queryById(@PathVariable Long id){
        SysPost sysPost = sysPostService.queryById(id);
        return sysPost == null ? Result.error("岗位不存在或已被删除") : Result.success(sysPost);
    }

    @GetMapping("/page")
    public Result<PageInfo<SysPost>> page(SysPost sysPost,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(sysPostService.page(pageNum, pageSize, sysPost));
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(sysPostService.deleteById(id));
    }
}
