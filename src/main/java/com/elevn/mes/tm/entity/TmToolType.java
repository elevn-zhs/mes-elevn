package com.elevn.mes.tm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.tm.options.CreateOption;
import com.elevn.mes.tm.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工装夹具类型表 实体类
 * 对应表：tm_tool_type
 *
 * 工装夹具的类型主数据（扳手、夹具、模具……），
 * "工作站 → 工装"关联表（md_workstation_tool）按类型绑定并记数量。
 *
 * 本次只为"工作站详情页选料下拉"提供查询接口，增删改留给工装模块（tm 线）的同学实现。
 *
 */
@Data
public class TmToolType implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工装夹具类型ID */
    @NotNull(groups = UpdateOption.class, message = "工装夹具类型ID不能为空")
    private Long toolTypeId;

    /** 类型编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "类型编码不能为空")
    @Length(max = 64, message = "类型编码长度不能超过 64 个字符")
    private String toolTypeCode;

    /** 类型名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "类型名称不能为空")
    @Length(max = 255, message = "类型名称长度不能超过 255 个字符")
    private String toolTypeName;

    /** 是否编码管理（Y 每件工装有唯一编号 / N 只按类型管理数量） */
    @Pattern(regexp = "^[YN]$", message = "是否编码管理只能是 Y 或 N")
    private String codeFlag;

    /** 保养维护类型（PERIOD 定期 / CONDITION 视情 / NONE 不保养） */
    @Length(max = 20, message = "保养维护类型长度不能超过 20 个字符")
    private String maintenType;

    /** 保养周期（天，maintenType 为 PERIOD 时有意义） */
    private Integer maintenPeriod;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    @Length(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
