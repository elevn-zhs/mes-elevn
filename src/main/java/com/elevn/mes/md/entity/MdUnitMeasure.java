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
 * 单位表 实体类
 * 对应表：md_unit_measure
 *
 * 主辅单位换算：primary_flag='Y' 的是主单位（primary_id 为空、change_rate 固定为 1）
 * 辅单位（如 箱、克）通过 primary_id 指到主单位，change_rate 表示"1 个当前单位 = ? 个主单位"
 * 例：主单位 千克(kg)，辅单位 克(g) 的 change_rate = 0.001
 *
 */
@Data
public class MdUnitMeasure implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单位ID */
    @NotNull(groups = UpdateOption.class, message = "单位ID不能为空")
    private Long measureId;

    /** 单位编码（如 kg / g / 个 / 箱，被 md_item.unit_of_measure 引用） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "单位编码不能为空")
    @Length(max = 64, message = "单位编码长度不能超过 64 个字符")
    private String measureCode;

    /** 单位名称（如 千克 / 克 / 个 / 箱） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "单位名称不能为空")
    @Length(max = 255, message = "单位名称长度不能超过 255 个字符")
    private String measureName;

    /** 是否是主单位（Y是 N否） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "是否是主单位不能为空")
    @Length(max = 1, message = "是否是主单位长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "是否是主单位只能是 Y 或 N")
    private String primaryFlag;

    /** 主单位ID（主单位本身为 null；辅单位必填，关联 md_unit_measure.measure_id） */
    private Long primaryId;

    /** 与主单位换算比例（1 个当前单位 = change_rate 个主单位，主单位固定为 1） */
    @DecimalMin(value = "0", inclusive = false, message = "与主单位换算比例必须大于 0")
    private BigDecimal changeRate;

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
