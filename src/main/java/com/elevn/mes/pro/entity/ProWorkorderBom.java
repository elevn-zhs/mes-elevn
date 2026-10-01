package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产工单BOM组成 实体类
 * 对应表：pro_workorder_bom
 *
 * 工单下达时，按产品 BOM（md_product_bom）展开生成，代表"这张工单预计要消耗这些料"。
 * 注意区分两个 quantity：
 *   pro_workorder_bom.quantity  预计使用量 = BOM 单位用量 × 工单生产数量
 *   md_product_bom.quantity     单件用量（BOM 里配的比例）
 * 实际消耗记录在 pro_trans_consume 里，两者对比就是用料差异。
 *
 */
@Data
public class ProWorkorderBom implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 行ID */
    @NotNull(groups = UpdateOption.class, message = "行ID不能为空")
    private Long lineId;

    /** 生产工单ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工单ID不能为空")
    private Long workorderId;

    /** BOM物料ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "物料不能为空")
    private Long itemId;

    /** BOM物料编号 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "物料编号不能为空")
    private String itemCode;

    /** BOM物料名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "物料名称不能为空")
    private String itemName;

    /** 规格型号 */
    private String itemSpc;

    /** 单位 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "单位不能为空")
    private String unitOfMeasure;

    /** 单位名称 */
    private String unitName;

    /** 物料产品标识（ITEM 物料 / PRODUCT 产品） */
    private String itemOrProduct;

    /** 预计使用量 */
    private BigDecimal quantity;

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
