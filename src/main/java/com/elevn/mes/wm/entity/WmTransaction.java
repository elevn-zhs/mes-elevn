package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存事务（流水账）实体类
 * 对应表：wm_transaction
 *
 * 【这张表是"账"，库存表是"余额"】
 *   库存表（wm_material_stock）只放当前还剩多少；
 *   流水表（这张）每一次变动都留一条记录，永远不倒推、不修改。
 *   对账的逻辑就是：某个物料某段时间的流水加起来 == 库存的变化。
 *
 * 【两个易错点】
 *   1) 方向记在 transaction_flag 上（1 入库 / -1 出库），数量永远是正数。
 *      "出库 -50" 和 "出库 50 且 flag=-1" 两种记法里，我们选后者，
 *      因为数量字段带负号后，求和统计时特别容易算错。
 *   2) 调拨必须生成【一进一出配对的两条】，并用 related_transaction_id 互指。
 *      只写一条的话，调拨就变成了凭空出库，总数对不上。
 *
 */
@Data
public class WmTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 事务ID */
    private Long transactionId;

    /** 事务类型（对应 wm_doc.doc_type） */
    private String transactionType;

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

    /** 容器ID */
    private Long packageId;

    /** 容器编号 */
    private String packageCode;

    // ---------- 来源单据（软引用） ----------

    /** 来源单据类型 */
    private String sourceDocType;

    /** 来源单据ID */
    private Long sourceDocId;

    /** 来源单据编号 */
    private String sourceDocCode;

    /** 来源单据行ID */
    private Long sourceDocLineId;

    /** 库存记录ID */
    private Long materialStockId;

    // ---------- 方向与数量 ----------

    /** 库存方向 1-入库(+) -1-出库(-) */
    private Integer transactionFlag;

    /** 事务数量（正数，方向看 transaction_flag） */
    private BigDecimal transactionQuantity;

    /** 事务日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    /** 关联的事务ID（调拨的配对行，两条互相指向对方） */
    private Long relatedTransactionId;

    /** ERP账期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime erpDate;

    /** 接收日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recptDate;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

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

    // ==================== 以下为非表字段 ====================

    /** 查询用：事务日期区间 - 起 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDateStart;

    /** 查询用：事务日期区间 - 止 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDateEnd;

    // ---------- 以下为按事务类型统计时的非表字段 ----------

    /** 统计用：该类型的入库合计（transaction_flag = 1 的数量之和） */
    private BigDecimal inQuantity;

    /** 统计用：该类型的出库合计（transaction_flag = -1 的数量之和） */
    private BigDecimal outQuantity;

    /** 统计用：净变动 = 入库 - 出库 */
    private BigDecimal netQuantity;

    /** 统计用：该类型下有多少条流水 */
    private Integer recordCount;
}
