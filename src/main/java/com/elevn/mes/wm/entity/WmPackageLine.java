package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.wm.options.CreateOption;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 装箱单明细行 实体类
 * 对应表：wm_package_line
 *
 * 【一行 = 从某个库存行拿了多少个装进箱子】
 *   material_stock_id 指向库存行（软引用），行上的物料/批次/仓库/库位
 *   全是装箱那一刻从库存行拷贝的快照 —— 库存行后来变了快照也不变，
 *   这和单据行冗余物料快照是同一个道理：追溯要的是"当时的事实"。
 *
 * 【装箱不改库存】
 *   装箱只是给货套包装层级，不扣减 wm_material_stock；
 *   货真正离开仓库要走出库单过账。所以这张表只管"箱里有什么"。
 *
 */
@Data
public class WmPackageLine implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 明细行ID */
    private Long lineId;

    /** 装箱单ID */
    @NotNull(groups = CreateOption.class, message = "装箱单ID不能为空")
    private Long packageId;

    /** 来源库存行ID（软引用，快照的出处） */
    @NotNull(groups = CreateOption.class, message = "来源库存行不能为空")
    private Long materialStockId;

    /** 物料ID */
    private Long itemId;

    /** 物料编码（快照） */
    private String itemCode;

    /** 物料名称（快照） */
    private String itemName;

    /** 规格型号（快照） */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 装箱数量 */
    @NotNull(groups = CreateOption.class, message = "装箱数量不能为空")
    private BigDecimal quantity;

    /** 工单ID（成品装箱时溯源到工单） */
    private Long workorderId;

    /** 工单编码（快照） */
    private String workorderCode;

    /** 批次编码（快照；无批次物料为空串） */
    private String batchCode;

    /** 仓库ID（快照） */
    private Long warehouseId;

    /** 仓库编码（快照） */
    private String warehouseCode;

    /** 仓库名称（快照） */
    private String warehouseName;

    /** 库位ID（快照） */
    private Long locationId;

    /** 库位编码（快照） */
    private String locationCode;

    /** 库位名称（快照） */
    private String locationName;

    /** 库区ID（快照） */
    private Long areaId;

    /** 库区编码（快照） */
    private String areaCode;

    /** 库区名称（快照） */
    private String areaName;

    /** 有效期（快照） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireDate;

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
