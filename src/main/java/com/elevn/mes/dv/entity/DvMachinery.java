package com.elevn.mes.dv.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.dv.options.CreateOption;
import com.elevn.mes.dv.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备表 实体类
 * 对应表：dv_machinery
 *
 * 设备主数据（一台物理设备一条记录），是"工作站 → 设备"关联表的子表来源。
 * machinery_type / workshop 的编码名称是冗余字段，保存时由后端从主表带出，
 * 列表查询就不用 join 了。
 *
 * 本次只为"工作站详情页选料下拉"提供查询接口，增删改留给设备模块（dv 线）的同学实现。
 *
 */
@Data
public class DvMachinery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 设备ID */
    @NotNull(groups = UpdateOption.class, message = "设备ID不能为空")
    private Long machineryId;

    /** 设备编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "设备编码不能为空")
    @Length(max = 64, message = "设备编码长度不能超过 64 个字符")
    private String machineryCode;

    /** 设备名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "设备名称不能为空")
    @Length(max = 255, message = "设备名称长度不能超过 255 个字符")
    private String machineryName;

    /** 品牌 */
    @Length(max = 255, message = "品牌长度不能超过 255 个字符")
    private String machineryBrand;

    /** 规格型号 */
    @Length(max = 255, message = "规格型号长度不能超过 255 个字符")
    private String machinerySpec;

    /** 设备类型ID（关联 dv_machinery_type.machinery_type_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "设备类型ID不能为空")
    private Long machineryTypeId;

    /** 设备类型编码（冗余字段） */
    @Length(max = 64, message = "设备类型编码长度不能超过 64 个字符")
    private String machineryTypeCode;

    /** 设备类型名称（冗余字段） */
    @Length(max = 255, message = "设备类型名称长度不能超过 255 个字符")
    private String machineryTypeName;

    /** 所属车间ID（关联 md_workshop.workshop_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "所属车间ID不能为空")
    private Long workshopId;

    /** 所属车间编码（冗余字段） */
    @Length(max = 64, message = "所属车间编码长度不能超过 64 个字符")
    private String workshopCode;

    /** 所属车间名称（冗余字段） */
    @Length(max = 255, message = "所属车间名称长度不能超过 255 个字符")
    private String workshopName;

    /** 最近保养时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMaintenTime;

    /** 最近点检时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckTime;

    /** 设备状态（RUN 运行 / STOP 停机 / MAINT 保养中 / SCRAP 报废） */
    @Length(max = 64, message = "设备状态长度不能超过 64 个字符")
    private String status;

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
