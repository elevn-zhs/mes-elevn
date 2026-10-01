package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物料供应商 实体类
 * 对应表：md_item_vendor
 *
 * 一行 = 「某个物料可以向某家供应商采购」的一条供货关系，是物料与供应商的多对多中间表。
 * md_vendor 只是供应商档案，本身不挂物料，所以中间表必须存在。
 *
 * 同一个物料通常会挂多家供应商，方便比价与应急补货，primary_flag 标记主供应商（唯一）。
 * vendor_item_code 是供应商自己那边的料号 —— 我方编码和对方编码几乎不可能一样，必须分开存。
 *
 */
@Data
public class MdItemVendor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流水号 */
    @NotNull(groups = UpdateOption.class, message = "流水号不能为空")
    private Long itemVendorId;

    /** 物料产品ID（关联 md_item.item_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "物料产品ID不能为空")
    private Long itemId;

    /** 物料编码（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "物料编码不能为空")
    @Length(max = 64, message = "物料编码长度不能超过 64 个字符")
    private String itemCode;

    /** 物料名称（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "物料名称不能为空")
    @Length(max = 255, message = "物料名称长度不能超过 255 个字符")
    private String itemName;

    /** 供应商ID（关联 md_vendor.vendor_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "供应商ID不能为空")
    private Long vendorId;

    /** 供应商编码（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "供应商编码不能为空")
    @Length(max = 64, message = "供应商编码长度不能超过 64 个字符")
    private String vendorCode;

    /** 供应商名称（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "供应商名称不能为空")
    @Length(max = 255, message = "供应商名称长度不能超过 255 个字符")
    private String vendorName;

    /** 供应商侧的物料编码 */
    @Length(max = 64, message = "供应商侧的物料编码长度不能超过 64 个字符")
    private String vendorItemCode;

    /** 供应商侧的物料名称 */
    @Length(max = 255, message = "供应商侧的物料名称长度不能超过 255 个字符")
    private String vendorItemName;

    /** 采购单价 */
    @DecimalMin(value = "0", message = "采购单价不能为负数")
    private BigDecimal purchasePrice;

    /** 币种（默认 CNY） */
    @Length(max = 10, message = "币种长度不能超过 10 个字符")
    private String currency;

    /** 最小起订量 */
    @DecimalMin(value = "0", message = "最小起订量不能为负数")
    private BigDecimal minOrderQty;

    /** 交货周期（天） */
    @Min(value = 0, message = "交货周期不能为负数")
    private Integer leadTime;

    /** 是否主供应商（Y是 N否） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "是否主供应商不能为空")
    @Length(max = 1, message = "是否主供应商长度不能超过 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "是否主供应商只能是 Y 或 N")
    private String primaryFlag;

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
