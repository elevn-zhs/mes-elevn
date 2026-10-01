package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出入库单据明细（库位落位记录）实体类
 * 对应表：wm_doc_detail
 *
 * 【为什么行之外还要一张落位表】
 *   行只说"要出 100 件螺丝"，但螺丝可能分散在 3 个库位。
 *   真正动库存时，必须一笔一笔写清楚"从 A-01-01 扣 60、从 A-01-02 扣 40"，
 *   这张表记的就是这些落位。
 *
 * 【数量一律记正数】
 *   方向由单据头的 io_flag 决定，明细里不存正负号 ——
 *   同一张单里出现负数会让人看半天才反应过来是出库。
 *
 * 【material_stock_id 很关键】
 *   出库类明细指向被扣减的那条库存记录，是"这批货从哪来"的直接证据；
 *   入库类明细在库存记录是新建时还没有 ID，Service 会在插完库存后回填。
 *
 */
@Data
public class WmDocDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 单据ID */
    private Long docId;

    /** 单据类型 */
    private String docType;

    /** 所属行ID */
    private Long lineId;

    /** 库存记录ID（出库类指向被扣减的库存） */
    private Long materialStockId;

    /** 产品物料ID */
    private Long itemId;

    /** 产品物料编码 */
    private String itemCode;

    /** 产品物料名称 */
    private String itemName;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 单位名称 */
    private String unitName;

    /** 数量（正数，方向由单据头的 io_flag 决定） */
    private BigDecimal quantity;

    /** 批次ID */
    private Long batchId;

    /** 批次号 */
    private String batchCode;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库编码 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 库位ID */
    private Long locationId;

    /** 库位编码 */
    private String locationCode;

    /** 库位名称 */
    private String locationName;

    /** 库区ID */
    private Long areaId;

    /** 库区编码 */
    private String areaCode;

    /** 库区名称 */
    private String areaName;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
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
