package com.elevn.mes.sys.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户表 实体类
 * 对应表：sys_user
 *
 */
@Data
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 所属部门ID */
    private Long deptId;

    /** 所属岗位ID（一人一主岗，如需多岗位另建中间表） */
    private Long postId;

    /** 登录账号 */
    private String userName;

    /** 用户昵称 */
    private String nickName;

    /** 用户类型（00系统用户 01普通用户） */
    private String userType;

    /** 邮箱 */
    private String email;

    /** 手机号码 */
    private String phonenumber;

    /** 用户性别（0男 1女 2未知） */
    private String sex;

    /** 头像地址 */
    private String avatar;

    /** 密码（BCrypt 加密，60 位） */
    private String password;

    /** 账号状态（0正常 1停用） */
    private String status;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    private LocalDateTime loginDate;

    /** 密码最后更新时间（用于强制改密策略） */
    private LocalDateTime pwdUpdateDate;

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

    /**
     * 下面三个字段在 sys_user 表里没有对应的列，
     * 是列表查询时 left join 部门表 / 岗位表、以及子查询聚合角色带出来的「展示用名称」。
     * 手写 MyBatis 里不需要额外注解，只要 Mapper.xml 的 resultMap 里有对应映射就能填进来。
     */

    /** 所属部门名称（join sys_dept 得到） */
    private String deptName;

    /** 所属岗位名称（join sys_post 得到） */
    private String postName;

    /** 已分配的角色名称，多个用逗号拼接（子查询 group_concat 得到） */
    private String roleNames;

    /** 已分配的角色列表，目前查询接口没填充，留给后续需要角色明细的场景 */
    private List<SysRole> roleList = new ArrayList<>();

}
