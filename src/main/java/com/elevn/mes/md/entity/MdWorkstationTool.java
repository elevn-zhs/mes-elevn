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
 * 工装夹具资源表 实体类
 * 对应表：md_workstation_tool
 *
 * 描述"这个工位需要哪类工装夹具、几套"，按类型（tool_type）而非单个实物管理
 * 具体到某一把夹具的寿命/借还，走 dv 设备模块或专门的工装台账
 *
 */
@Data
public class MdWorkstationTool implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @NotNull(groups = UpdateOption.class, message = "记录ID不能为空")
    private Long recordId;

    /** 工作站ID（关联 md_workstation.workstation_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工作站ID不能为空")
    private Long workstationId;

    /** 工装夹具类型ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工装夹具类型ID不能为空")
    private Long toolTypeId;

    /** 工装夹具类型编码（冗余字段） */
    @Length(max = 64, message = "类型编码长度不能超过 64 个字符")
    private String toolTypeCode;

    /** 工装夹具类型名称（冗余字段） */
    @Length(max = 255, message = "类型名称长度不能超过 255 个字符")
    private String toolTypeName;

    /** 数量（该类型所需套数，至少 1） */
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
