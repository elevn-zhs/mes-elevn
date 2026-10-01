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
 * 物料产品表 实体类
 * 对应表：md_item
 *
 * item_or_product 用来区分"物料 / 产品"两类主数据，值由字典 md_item_or_product 维护
 * batch_flag = 'Y' 时，该物料在出入库环节必须录入批次号，并可配合 md_item_batch_config 配置批次属性
 *
 */
@Data
public class MdItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 产品物料ID（新增时不用传，修改时必传） */
    @NotNull(groups = UpdateOption.class, message = "产品物料ID不能为空")
    private Long itemId;

    /** 产品物料编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料编码不能为空")
    @Length(max = 64, message = "产品物料编码长度不能超过 64 个字符")
    private String itemCode;

    /** 产品物料名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料名称不能为空")
    @Length(max = 255, message = "产品物料名称长度不能超过 255 个字符")
    private String itemName;

    /** 规格型号 */
    @Length(max = 500, message = "规格型号长度不能超过 500 个字符")
    private String specification;

    /** 单位编码（关联 md_unit_measure.measure_code） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "单位编码不能为空")
    private String unitOfMeasure;

    /** 单位名称（冗余字段，避免列表查询频繁 join 单位表） */
    private String unitName;

    /** 产品物料标识（ITEM=物料 PRODUCT=产品） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料标识不能为空")
    @Length(max = 20, message = "产品物料标识长度不能超过 20 个字符")
    private String itemOrProduct;

    /** 物料类型ID（关联 md_item_type.item_type_id） */
    private Long itemTypeId;

    /** 物料类型编码（冗余字段） */
    @Length(max = 64, message = "物料类型编码长度不能超过 64 个字符")
    private String itemTypeCode;

    /** 物料类型名称（冗余字段） */
    @Length(max = 255, message = "物料类型名称长度不能超过 255 个字符")
    private String itemTypeName;

    /** 是否启用（Y是 N否） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "是否启用不能为空")
    @Length(max = 1, message = "是否启用长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "是否启用只能是 Y 或 N")
    private String enableFlag;

    /** 是否设置安全库存（Y是 N否） */
    @Length(max = 1, message = "是否设置安全库存长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "是否设置安全库存只能是 Y 或 N")
    private String safeStockFlag;

    /** 最低库存量（safe_stock_flag='Y' 时才有意义，库存低于该值触发预警） */
    @DecimalMin(value = "0", message = "最低库存量不能小于 0")
    private BigDecimal minStock;

    /** 最大库存量（0 表示不限制） */
    @DecimalMin(value = "0", message = "最大库存量不能小于 0")
    private BigDecimal maxStock;

    /** 高价值物资（Y是 N否，高价值物资出库通常需要额外审批） */
    @Length(max = 1, message = "高价值物资标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "高价值物资标志只能是 Y 或 N")
    private String highValue;

    /** 是否批次管理（Y是 N否） */
    @Length(max = 1, message = "批次管理标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "批次管理标志只能是 Y 或 N")
    private String batchFlag;

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
