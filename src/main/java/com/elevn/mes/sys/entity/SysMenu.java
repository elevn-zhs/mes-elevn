package com.elevn.mes.sys.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限表（菜单权限）实体类
 * 对应表：sys_menu
 *
 * menu_type：M=目录  C=菜单  F=按钮  A=接口
 * 当 menu_type='A' 时填写 apiUrl + apiMethod，由后端拦截器校验接口级权限
 *
 */
@Data
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 权限ID */
    private Long menuId;

    /** 权限名称 */
    private String menuName;

    /** 父级ID（0=顶级） */
    private Long parentId;

    /** 显示顺序 */
    private Integer orderNum;

    /** 路由地址 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 路由参数 */
    private String query;

    /** 是否外链（0是 1否） */
    private String isFrame;

    /** 是否缓存（0缓存 1不缓存） */
    private String isCache;

    /** 权限类型（M目录 C菜单 F按钮 A接口） */
    private String menuType;

    /** 显示状态（0显示 1隐藏） */
    private String visible;

    /** 权限状态（0正常 1停用） */
    private String status;

    /** 权限标识（如 sys:user:list） */
    private String perms;

    /** 菜单图标 */
    private String icon;

    /** 接口地址（menu_type=A 时填写，如 /system/user/**） */
    private String apiUrl;

    /** 接口方法（GET/POST/PUT/DELETE/*） */
    private String apiMethod;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 子权限列表（非数据库字段，用于构建菜单树） */
    private List<SysMenu> children = new ArrayList<>();

}
