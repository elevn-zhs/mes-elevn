package com.elevn.mes.sys.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户与角色关联表 实体类
 * 对应表：sys_user_role
 *
 * 注意：表中没有 (user_id, role_id) 唯一索引。
 * 业务层做法：先查是否存在同组合的软删记录，有则改 delFlag='0'，无则新增。
 *
 */
@Data
public class SysUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者（复用通用实体时不会缺列） */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

}
