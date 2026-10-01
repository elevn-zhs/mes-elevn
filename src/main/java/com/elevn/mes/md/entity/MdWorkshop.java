package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车间表 实体类
 * 对应表：md_workshop
 *
 * 车间是工作站的上一级组织维度，产线排程、设备台账、人员排班通常按车间聚合统计
 *
 */
@Data
public class MdWorkshop implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 车间ID */
    @NotNull(groups = UpdateOption.class, message = "车间ID不能为空")
    private Long workshopId;

    /** 车间编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "车间编码不能为空")
    @Length(max = 64, message = "车间编码长度不能超过 64 个字符")
    private String workshopCode;

    /** 车间名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "车间名称不能为空")
    @Length(max = 255, message = "车间名称长度不能超过 255 个字符")
    private String workshopName;

    /** 面积（平方米） */
    @DecimalMin(value = "0", message = "面积不能小于 0")
    private BigDecimal area;

    /** 负责人 */
    @Length(max = 64, message = "负责人长度不能超过 64 个字符")
    private String charge;

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
