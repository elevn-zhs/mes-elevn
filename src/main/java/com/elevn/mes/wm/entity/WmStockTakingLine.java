package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盘点单明细行 实体类
 * 对应表：wm_stock_taking_line
 *
 * 【一行 = 一个库存行的"账实对账"】
 *   material_stock_id 锁定对比对象，quantity 是账面（生成时拷贝），
 *   taking_quantity 是实盘（人工录入），diff = 实盘 - 账面（后端算，不信前端）。
 *   盘盈 diff>0，盘亏 diff<0，平了就是 0。
 *
 */
@Data
public class WmStockTakingLine implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 明细行ID */
    private Long lineId;

    /** 盘点单ID */
    private Long takingId;

    /** 来源库存行ID（账实对比的锚点） */
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

    /** 单位名称 */
    private String unitName;

    /** 批次ID */
    private Long batchId;

    /** 批次编码（快照；无批次物料为空串） */
    private String batchCode;

    /** 账面数量（生成时从库存行拷贝的 quantity_onhand） */
    private BigDecimal quantity;

    /** 实盘数量（人工录入；null = 还没盘） */
    private BigDecimal takingQuantity;

    /** 差异 = 实盘 - 账面（后端计算回填） */
    private BigDecimal diffQuantity;

    /** 仓库三件套（快照） */
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    /** 库位三件套（快照） */
    private Long locationId;
    private String locationCode;
    private String locationName;

    /** 库区三件套（快照） */
    private Long areaId;
    private String areaCode;
    private String areaName;

    /** 行状态 PREPARE未录入 / CONFIRMED已录入待过账 */
    private String takingStatus;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    private String remark;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
