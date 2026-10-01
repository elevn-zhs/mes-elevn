package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysRoleMenu;
import com.elevn.mes.sys.service.SysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色授权 Controller
 *
 * 这一层管的是「角色能看到哪些菜单」，是整个权限体系的开关。
 * 接口刻意只开放了三个常用动作：查已授权、重新授权、以及关联本身的基础 CRUD。
 *
 * 前端的典型用法：
 *   1. GET  /api/menu/tree              拿到完整菜单树
 *   2. GET  /api/roleMenu/menuIds/{roleId} 拿到这个角色已经勾了哪些
 *   3. 用户在树上勾勾选选
 *   4. POST /api/roleMenu/assign        提交新的 menuIds 数组
 *
 */
@RestController
@RequestMapping("/api/roleMenu")
public class SysRoleMenuController {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    /**
     * 给角色重新授权（全量覆盖）
     *
     * roleId 走 @RequestParam 挂在 URL 上，menuIds 数组走 @RequestBody 放在请求体里，
     * 这样省得再定义一个 "RoleMenuDto" 出来。前端这样调：
     *   request.post('/roleMenu/assign?roleId=1', [1,2,3])
     *
     * 返回的是本次写入的关联条数，前端拿它弹提示 "已授权 12 个菜单"。
     */
    @PostMapping("/assign")
    public Result<Integer> assignMenu(@RequestParam Long roleId, @RequestBody Long[] menuIds){
        return Result.success(sysRoleMenuService.assignMenu(roleId, menuIds));
    }

    /**
     * 查询某角色已授权的菜单ID，用于授权弹窗的勾选出显
     */
    @GetMapping("/menuIds/{roleId}")
    public Result<List<Long>> selectMenuIdsByRoleId(@PathVariable Long roleId){
        return Result.success(sysRoleMenuService.selectMenuIdsByRoleId(roleId));
    }

    /**
     * 关联记录的基础 CRUD
     * 日常排查「这个角色到底授权了啥」的时候比空想管用
     */
    @GetMapping("/page")
    public Result<PageInfo<SysRoleMenu>> page(SysRoleMenu sysRoleMenu,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(sysRoleMenuService.page(pageNum, pageSize, sysRoleMenu));
    }

    @GetMapping("/{id}")
    public Result<SysRoleMenu> queryById(@PathVariable Long id){
        return Result.success(sysRoleMenuService.queryById(id));
    }

    /**
     * 单条新增关联
     * 注意：正常授权请走 /assign，这个接口只适合补一条漏的、
     * 或者写测试数据时用 —— 直接用它批量塞，很容易塞出重复授权。
     */
    @PostMapping
    public Result<SysRoleMenu> save(@RequestBody SysRoleMenu sysRoleMenu){
        return sysRoleMenuService.save(sysRoleMenu) == 1 ? Result.success(sysRoleMenu) : Result.error("保存失败");
    }

    @PutMapping
    public Result updateById(@RequestBody SysRoleMenu sysRoleMenu){
        return sysRoleMenuService.updateById(sysRoleMenu) == 1 ? Result.success(sysRoleMenu) : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(sysRoleMenuService.deleteById(id));
    }
}
