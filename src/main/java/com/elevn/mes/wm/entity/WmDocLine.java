package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出入库单据行 实体类
 * 对应表：wm_doc_line
 *
 * 【行是"打算动多少货"，明细是"实际落在哪个库位"】
 *   行（这张表）      ：选物料 + 批次 + 数量 + 建议库位
 *   明细（wm_doc_detail）：过账时按实际库存位置拆出来的落位记录
 *   一条行可以拆成多条明细 —— 比如要出 100 件，A 库位只有 60、B 库位有 80，
 *   就会落成两条明细（60 + 40）。
 *
 * 【出库时 material_stock_id 的意义】
 *   出库和调拨类的行，指向"从哪一行库存扣"。留空则由后端按
 *   物料 + 批次 + 建议库位 去自动匹配（FIFO，先入库的先出）。
 *
 */
@Data
public class WmDocLine implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 行ID */
    @NotNull(groups = UpdateOption.class, message = "行ID不能为空")
    private Long lineId;

    /** 单据ID */
    @NotNull(groups = CreateOption.class, message = "单据ID不能为空")
    private Long docId;

    /** 单据类型（冗余字段，便于按类型统计与防串数据） */
    private String docType;

    /** 行号 */
    private Integer lineNo;

    /** 来源单据行ID（通知单行等） */
    private Long sourceLineId;

    /** 库存记录ID（出库/调拨类必填，指明从哪一行库存扣） */
    private Long materialStockId;

    /** 产品物料ID */
    @NotNull(groups = CreateOption.class, message = "物料不能为空")
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

    /** 单据数量 */
    @NotNull(groups = CreateOption.class, message = "数量不能为空")
    @DecimalMin(value = "0.000001", groups = CreateOption.class, message = "数量必须大于 0")
    private BigDecimal quantity;

    /** 实际数量（收货差异、退货实退等场景） */
    private BigDecimal quantityActual;

    /** 批次ID */
    private Long batchId;

    /** 批次号 */
    private String batchCode;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime produceDate;

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireDate;

    /** 生产批号 */
    private String lotNumber;

    /** 生产工单ID */
    private Long workorderId;

    /** 生产工单编码 */
    private String workorderCode;

    /** 生产工单名称 */
    private String workorderName;

    /** 是否检验（Y是 N否） */
    private String qcFlag;

    /** 检验单ID */
    private Long qcId;

    /** 检验单编号 */
    private String qcCode;

    /** 质量状态 */
    private String qualityStatus;

    // ---------- 以下为"建议库位"，实际落位以 wm_doc_detail 为准 ----------

    /** 建议仓库ID */
    private Long warehouseId;

    /** 建议仓库编码 */
    private String warehouseCode;

    /** 建议仓库名称 */
    private String warehouseName;

    /** 建议库位ID */
    private Long locationId;

    /** 建议库位编码 */
    private String locationCode;

    /** 建议库位名称 */
    private String locationName;

    /** 建议库区ID */
    private Long areaId;

    /** 建议库区编码 */
    private String areaCode;

    /** 建议库区名称 */
    private String areaName;

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

    // ==================== 以下为非表字段 ====================

    /** 当前可用库存（新增/编辑行时给前端做提示，不参与入库保存） */
    private BigDecimal availableQuantity;
}
