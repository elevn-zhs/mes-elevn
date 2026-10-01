package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作站表 实体类
 * 对应表：md_workstation
 *
 * 工作站 = 生产执行的最小单元，一张表串起四条线：
 *   车间 md_workshop｜工序 pro_process｜线边库 wm_warehouse｜线边库位 wm_location
 * warehouse_id / area_id / location_id 三个库位相关字段在表中是 not null default 0，
 * 未配置线边库时填 0，所以新增接口要求必传（前端传 0 即可，表示"暂不绑定"）
 *
 */
@Data
public class MdWorkstation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工作站ID */
    @NotNull(groups = UpdateOption.class, message = "工作站ID不能为空")
    private Long workstationId;

    /** 工作站编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "工作站编码不能为空")
    @Length(max = 64, message = "工作站编码长度不能超过 64 个字符")
    private String workstationCode;

    /** 工作站名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "工作站名称不能为空")
    @Length(max = 255, message = "工作站名称长度不能超过 255 个字符")
    private String workstationName;

    /** 工作站地点 */
    @Length(max = 255, message = "工作站地点长度不能超过 255 个字符")
    private String workstationAddress;

    /** 所在车间ID（关联 md_workshop.workshop_id） */
    private Long workshopId;

    /** 所在车间编码（冗余字段） */
    @Length(max = 64, message = "所在车间编码长度不能超过 64 个字符")
    private String workshopCode;

    /** 所在车间名称（冗余字段） */
    @Length(max = 255, message = "所在车间名称长度不能超过 255 个字符")
    private String workshopName;

    /** 工序ID（关联 pro_process） */
    private Long processId;

    /** 工序编码（冗余字段） */
    @Length(max = 64, message = "工序编码长度不能超过 64 个字符")
    private String processCode;

    /** 工序名称（冗余字段） */
    @Length(max = 255, message = "工序名称长度不能超过 255 个字符")
    private String processName;

    /** 线边库ID（关联 wm_warehouse，未绑定填 0） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "线边库ID不能为空")
    private Long warehouseId;

    /** 线边库编码（冗余字段） */
    @Length(max = 64, message = "线边库编码长度不能超过 64 个字符")
    private String warehouseCode;

    /** 线边库名称（冗余字段） */
    @Length(max = 255, message = "线边库名称长度不能超过 255 个字符")
    private String warehouseName;

    /** 库区ID（关联 wm_location，location_type=AREA，未绑定填 0） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "库区ID不能为空")
    private Long areaId;

    /** 库区编码（冗余字段） */
    @Length(max = 64, message = "库区编码长度不能超过 64 个字符")
    private String areaCode;

    /** 库区名称（冗余字段） */
    @Length(max = 255, message = "库区名称长度不能超过 255 个字符")
    private String areaName;

    /** 库位ID（关联 wm_location，location_type=LOCATION，未绑定填 0） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "库位ID不能为空")
    private Long locationId;

    /** 库位编码（冗余字段） */
    @Length(max = 64, message = "库位编码长度不能超过 64 个字符")
    private String locationCode;

    /** 库位名称（冗余字段） */
    @Length(max = 255, message = "库位名称长度不能超过 255 个字符")
    private String locationName;

    /** 是否启用（Y是 N否） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "是否启用不能为空")
    @Length(max = 1, message = "是否启用长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "是否启用只能是 Y 或 N")
    private String enableFlag;

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
