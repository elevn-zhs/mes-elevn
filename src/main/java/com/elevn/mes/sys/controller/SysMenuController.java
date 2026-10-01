package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysMenu;
import com.elevn.mes.sys.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单（权限）管理 Controller
 *
 * 菜单表是本系统权限模型的核心：
 *   角色 ──(sys_role_menu)── 菜单 ── 决定登录后能看到哪些菜单、能调哪些接口
 * menu_type 区分了三种东西：M 目录 / C 菜单 / F 按钮（对应 entity 里的 apiUrl + apiMethod）
 *
 */
@RestController
@RequestMapping("/api/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @PostMapping
    public Result<SysMenu> save(@RequestBody @Validated SysMenu sysMenu){
        return sysMenuService.save(sysMenu) == 1 ? Result.success(sysMenu) : Result.error("保存失败");
    }

    @PutMapping
    public Result updateById(@RequestBody @Validated SysMenu sysMenu){
        return sysMenuService.updateById(sysMenu) == 1 ? Result.success(sysMenu) : Result.error("修改失败");
    }

    @GetMapping("/{id}")
    public Result<SysMenu> queryById(@PathVariable Long id){
        SysMenu sysMenu = sysMenuService.queryById(id);
        return sysMenu == null ? Result.error("菜单不存在或已被删除") : Result.success(sysMenu);
    }

    /**
     * 菜单树（不分页）
     * 前端的 el-tree 直接吃这坨数据，每个节点的 children 里挂着子菜单。
     * 菜单管理的表格和角色授权的菜单树用的是同一份数据，只是前端渲染方式不同。
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree(SysMenu sysMenu){
        return Result.success(sysMenuService.tree(sysMenu));
    }

    @GetMapping("/page")
    public Result<PageInfo<SysMenu>> page(SysMenu sysMenu,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(sysMenuService.page(pageNum, pageSize, sysMenu));
    }

    /**
     * 删除菜单：下面还有子菜单会被 service 层拦下来
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(sysMenuService.deleteById(id));
    }
}
