package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出入库单据头 实体类
 * 对应表：wm_doc
 *
 * 【整个仓储模块的心脏：14 类单据装在了一张表里】
 *   doc_type  决定这是什么业务（采购入库 / 生产领料 / 销售出库 / 调拨 ...）
 *   io_flag   决定库存往哪走：I 入库(+) / O 出库(-) / T 调拨(双向)
 *   status    决定这张单能不能改：PREPARE 待过账可改，CONFIRMED 已过账锁死
 *
 *   为什么不建 14 张表？因为它们的字段 100% 同构，
 *   合并后新增一类单据只要加字典 + 加编码规则，不用动代码。
 *
 * 【单据过账是模块里唯一会改动库存的动作】
 *   过账（execute）在一个事务里做四件事：
 *     ① 写 wm_doc_detail 落位  ② 改 wm_material_stock 库存
 *     ③ 生成 wm_transaction 流水  ④ 回写 A 线投料
 *   四件事必须同生共死 —— 少做一件，账就对不上。
 *
 */
@Data
public class WmDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单据ID */
    @NotNull(groups = UpdateOption.class, message = "单据ID不能为空")
    private Long docId;

    /** 单据类型 见字典 wm_doc_type（14 类） */
    @NotBlank(groups = CreateOption.class, message = "单据类型不能为空")
    private String docType;

    /** 单据编号（后端按编码规则生成，前端不用传） */
    private String docCode;

    /** 单据名称（默认用类型名 + 日期，可改） */
    @Length(max = 255, message = "单据名称长度不能超过 255 个字符")
    private String docName;

    /** 出入库标志 I入库 O出库 T调拨（由 docType 推导，前端不手选） */
    private String ioFlag;

    /** 业务日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bizDate;

    /** 需求日期（领料单的需求时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requiredDate;

    /** 单据状态 PREPARE待过账 / CONFIRMED已过账 / FINISHED已完成 / CANCELED已取消 */
    @Pattern(regexp = "^(PREPARE|CONFIRMED|FINISHED|CANCELED)$",
            message = "单据状态只能是 PREPARE / CONFIRMED / FINISHED / CANCELED")
    private String status;

    // ---------- 上游单据（软引用，无外键） ----------

    /** 来源单据类型（通知单、工单等） */
    private String sourceDocType;

    /** 来源单据ID */
    private Long sourceDocId;

    /** 来源单据编号 */
    private String sourceDocCode;

    // ---------- 往来对象（供应商 / 客户 / 外协厂商 三选一） ----------

    /** 往来对象类型 VENDOR / CLIENT / OUTSOURCE */
    private String partnerType;

    /** 往来对象ID */
    private Long partnerId;

    /** 往来对象编码 */
    private String partnerCode;

    /** 往来对象名称 */
    private String partnerName;

    /** 往来对象简称 */
    private String partnerNick;

    /** 采购订单编号 */
    private String poCode;

    /** 销售订单编号 */
    private String soCode;

    // ---------- 生产相关 ----------

    /** 生产工单ID */
    private Long workorderId;

    /** 生产工单编码 */
    private String workorderCode;

    /** 生产工单名称 */
    private String workorderName;

    /** 生产任务ID */
    private Long taskId;

    /** 生产任务编号 */
    private String taskCode;

    /** 生产任务名称 */
    private String taskName;

    /** 工作站ID */
    private Long workstationId;

    /** 工作站编码 */
    private String workstationCode;

    /** 工作站名称 */
    private String workstationName;

    /** 工序ID */
    private Long processId;

    /** 工序编号 */
    private String processCode;

    /** 工序名称 */
    private String processName;

    /** 关联的报工单ID */
    private Long feedbackId;

    // ---------- 整单物料（一单一产品场景，多数单据以行为准） ----------

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

    // ---------- 质量检验 ----------

    /** 检验类型 IQC/IPQC/OQC/FQC */
    private String qcType;

    /** 检验单ID */
    private Long qcId;

    /** 检验单编号 */
    private String qcCode;

    // ---------- 物流信息 ----------

    /** 承运商 */
    private String carrier;

    /** 运输单号 */
    private String shippingNumber;

    /** 收货人 */
    private String recipient;

    /** 联系方式 */
    private String tel;

    /** 收货地址 / 目的地 */
    private String address;

    // ---------- 业务分类与原因 ----------

    /** 业务类型（杂项类型 / 调拨类型 / 退料类型） */
    private String reasonType;

    /** 原因说明（退货原因等） */
    private String reason;

    // ---------- 源仓库（整单默认位置，实际落位以 wm_doc_detail 为准） ----------

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

    // ---------- 目标仓库（仅调拨单使用） ----------

    /** 目标仓库ID */
    private Long toWarehouseId;

    /** 目标仓库编码 */
    private String toWarehouseCode;

    /** 目标仓库名称 */
    private String toWarehouseName;

    /** 目标库位ID */
    private Long toLocationId;

    /** 目标库位编码 */
    private String toLocationCode;

    /** 目标库位名称 */
    private String toLocationName;

    /** 目标库区ID */
    private Long toAreaId;

    /** 目标库区编码 */
    private String toAreaCode;

    /** 目标库区名称 */
    private String toAreaName;

    // ---------- 标记位 ----------

    /** 是否配送（调拨单，Y是 N否） */
    @Pattern(regexp = "^[YN]$", message = "是否配送只能是 Y 或 N")
    private String deliveryFlag;

    /** 是否已确认（调拨单送达确认，Y是 N否） */
    @Pattern(regexp = "^[YN]$", message = "是否已确认只能是 Y 或 N")
    private String confirmFlag;

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

    /** 单据类型名称（从字典带过来，列表页直接显示中文，不用前端再转一次） */
    private String docTypeName;

    /** 关联的客户ID（新增销售出库/退货时用，Service 会同步到 partner_* 与客户快照） */
    private Long clientId;

    /** 关联的供应商ID（新增采购入库/退货时用） */
    private Long vendorId;

    /** 查询用：业务日期区间 - 起（非表字段） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bizDateStart;

    /** 查询用：业务日期区间 - 止（非表字段） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bizDateEnd;

    /** 明细行列表（详情接口一次带出） */
    private List<WmDocLine> lineList;

    /** 落位明细（详情接口一次带出，只有过账后才有数据） */
    private List<WmDocDetail> detailList;
}
