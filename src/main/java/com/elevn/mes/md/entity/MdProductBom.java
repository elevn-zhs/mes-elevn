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
 * 产品BOM关系表 实体类
 * 对应表：md_product_bom
 *
 * 一行 = 父件（item_id）用到 1 个子件（bom_item_id）的用量关系
 * quantity 是"使用比例"，即生产 1 个父件需要消耗多少子件（注意：不是整单用量）
 *
 */
@Data
public class MdProductBom implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流水号 */
    @NotNull(groups = UpdateOption.class, message = "BOM记录ID不能为空")
    private Long bomId;

    /** 物料产品ID（父件，关联 md_item.item_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "物料产品ID不能为空")
    private Long itemId;

    /** BOM物料ID（子件，关联 md_item.item_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "BOM物料ID不能为空")
    private Long bomItemId;

    /** BOM物料编码（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "BOM物料编码不能为空")
    @Length(max = 64, message = "BOM物料编码长度不能超过 64 个字符")
    private String bomItemCode;

    /** BOM物料名称（冗余字段） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "BOM物料名称不能为空")
    @Length(max = 255, message = "BOM物料名称长度不能超过 255 个字符")
    private String bomItemName;

    /** BOM物料规格 */
    @Length(max = 500, message = "BOM物料规格长度不能超过 500 个字符")
    private String bomItemSpec;

    /** BOM物料单位 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "BOM物料单位不能为空")
    @Length(max = 64, message = "BOM物料单位长度不能超过 64 个字符")
    private String unitOfMeasure;

    /** 产品物料标识（ITEM=物料 PRODUCT=产品） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料标识不能为空")
    @Length(max = 20, message = "产品物料标识长度不能超过 20 个字符")
    private String itemOrProduct;

    /** 物料使用比例（生产 1 个父件的消耗量，必须大于 0） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "物料使用比例不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "物料使用比例必须大于 0")
    private BigDecimal quantity;

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
