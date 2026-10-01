package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存记录（现存量）实体类
 * 对应表：wm_material_stock
 *
 * 【这张表是仓储模块的"账"】
 *   页面上是只读的 —— 不能手动改数量，只能由出入库单据过账驱动。
 *   原因很简单：库存是结果，单据才是原因。放开了手改，账和单据就对不上，
 *   出了问题谁也说不清货是怎么少的。
 *
 * 【uk_stock 唯一约束（全表最重要的口径）】
 *   同一「物料 + 批次 + 仓库 + 库区 + 库位 + 容器」只允许存在一条记录。
 *   入库时不是无脑 insert，而是先按这组维度去"找行"：
 *     找得到 → 累加 quantity_onhand
 *     找不到 → 新建一行
 *   做错了同一批料就会散成好几行，对账时永远对不平。
 *   注意：批次/库区/库位/容器用 0 与空串表示"无"，不允许 NULL
 *   （MySQL 唯一索引里 NULL 不参与比较，NULL 会让唯一约束形同虚设）。
 *
 */
@Data
public class WmMaterialStock implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 库存记录ID */
    private Long materialStockId;

    /** 物料类型ID */
    private Long itemTypeId;

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

    /** 批次ID（0 表示无批次） */
    private Long batchId;

    /** 批次号 */
    private String batchCode;

    /** 生产工单ID */
    private Long workorderId;

    /** 生产工单编号 */
    private String workorderCode;

    /** 供应商ID */
    private Long vendorId;

    /** 供应商编号 */
    private String vendorCode;

    /** 供应商名称 */
    private String vendorName;

    /** 供应商简称 */
    private String vendorNick;

    /** 客户ID */
    private Long clientId;

    /** 客户编码 */
    private String clientCode;

    /** 客户名称 */
    private String clientName;

    /** 客户简称 */
    private String clientNick;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库编码 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 库位ID（0 表示未指定） */
    private Long locationId;

    /** 库位编码 */
    private String locationCode;

    /** 库位名称 */
    private String locationName;

    /** 库区ID（0 表示未指定） */
    private Long areaId;

    /** 库区编码 */
    private String areaCode;

    /** 库区名称 */
    private String areaName;

    /** 容器ID（0 表示未装箱） */
    private Long packageId;

    /** 容器编号 */
    private String packageCode;

    /** 在库数量 */
    private BigDecimal quantityOnhand;

    /** 保留数量（已分配未出库） */
    private BigDecimal quantityReserved;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime productionDate;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recptDate;

    /** 库存有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireDate;

    /** 是否冻结（Y冻结 N正常） */
    private String frozenFlag;

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

    // ==================== 以下为非表字段 ====================

    /** 可用数量 = 在库数 - 保留数（出库校验用的就是它，前端直接显示省得自己算） */
    private BigDecimal quantityAvailable;

    /** 物料最低库存（取自 md_item.min_stock，库存预警用） */
    private BigDecimal minStock;

    /** 物料最大库存（取自 md_item.max_stock） */
    private BigDecimal maxStock;

    /** 查询用：只看有货的行（传 'Y' 时过滤 quantity_onhand > 0，非表字段） */
    private String onlyStock;

    /** 汇总用：该分组下有多少种物料（按仓库汇总时才有意义，非表字段） */
    private Integer itemKindCount;

    /**
     * 预警类型（异常预警查询用，非表字段）
     *   LOW  低于最低库存（安全库存），该补货了
     *   HIGH 超过最高库存，压了太多资金
     *   空串 正常
     */
    private String warningType;
}
