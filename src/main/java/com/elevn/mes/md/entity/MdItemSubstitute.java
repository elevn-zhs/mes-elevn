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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物料替代品 实体类
 * 对应表：md_item_substitute
 *
 * 一行 = 「主物料 item_id 可以被替代物料 sub_item_id 顶替」的一条替代关系。
 *
 * substitute_type 决定方向：
 *   ONE_WAY 单向：只有主物料能被替代物料顶替，反过来不成立（物料存在多条替代方案时用这种）；
 *   TWO_WAY 双向：两个物料互为替代，一条记录即可，查询时两个方向都要认。
 *
 * substitute_ratio 是用量比：每消耗 1 个主物料，需要用掉多少个替代物料。
 * 默认 1 表示等量替换；换成规格不完全一样的料时会不等于 1（例如计入加工损耗的 1.05）。
 *
 */
@Data
public class MdItemSubstitute implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流水号 */
    @NotNull(groups = UpdateOption.class, message = "流水号不能为空")
    private Long substituteId;

    /** 主物料ID（关联 md_item.item_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "主物料ID不能为空")
    private Long itemId;

    /** 主物料编码（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "主物料编码不能为空")
    @Length(max = 64, message = "主物料编码长度不能超过 64 个字符")
    private String itemCode;

    /** 主物料名称（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "主物料名称不能为空")
    @Length(max = 255, message = "主物料名称长度不能超过 255 个字符")
    private String itemName;

    /** 替代物料ID（关联 md_item.item_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "替代物料ID不能为空")
    private Long subItemId;

    /** 替代物料编码（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "替代物料编码不能为空")
    @Length(max = 64, message = "替代物料编码长度不能超过 64 个字符")
    private String subItemCode;

    /** 替代物料名称（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "替代物料名称不能为空")
    @Length(max = 255, message = "替代物料名称长度不能超过 255 个字符")
    private String subItemName;

    /** 替代物料规格（冗余字段） */
    @Length(max = 500, message = "替代物料规格长度不能超过 500 个字符")
    private String subItemSpec;

    /** 替代物料单位（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "替代物料单位不能为空")
    @Length(max = 64, message = "替代物料单位长度不能超过 64 个字符")
    private String unitOfMeasure;

    /** 替代类型（ONE_WAY=单向 TWO_WAY=双向） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "替代类型不能为空")
    @Length(max = 20, message = "替代类型长度不能超过 20 个字符")
    @Pattern(regexp = "^(ONE_WAY|TWO_WAY)$", message = "替代类型只能是 ONE_WAY 或 TWO_WAY")
    private String substituteType;

    /** 用量比（每 1 个主物料需要消耗多少个替代物料） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "用量比不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "用量比必须大于 0")
    private BigDecimal substituteRatio;

    /** 优先级（数字越小越优先） */
    private Integer priority;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    /** 是否启用（Y是 N否） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "是否启用不能为空")
    @Length(max = 1, message = "是否启用长度不能超过 1 个字符")
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
