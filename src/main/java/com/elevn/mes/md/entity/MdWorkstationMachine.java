package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备资源表 实体类
 * 对应表：md_workstation_machine
 *
 * 工作站挂了哪些设备（一台 / 多台），是"工作站 → 设备"的多对多中间表
 * 设备主数据在 dv_machinery，这里只冗余 code / name 便于列表直接展示
 *
 */
@Data
public class MdWorkstationMachine implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @NotNull(groups = UpdateOption.class, message = "记录ID不能为空")
    private Long recordId;

    /** 工作站ID（关联 md_workstation.workstation_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工作站ID不能为空")
    private Long workstationId;

    /** 设备ID（关联 dv_machinery.machinery_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "设备ID不能为空")
    private Long machineryId;

    /** 设备编码（冗余字段） */
    @Length(max = 64, message = "设备编码长度不能超过 64 个字符")
    private String machineryCode;

    /** 设备名称（冗余字段） */
    @Length(max = 255, message = "设备名称长度不能超过 255 个字符")
    private String machineryName;

    /** 数量（同一设备在该工作站上的台数，至少 1） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于 1")
    private Integer quantity;

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
