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
 * 批次 实体类
 * 对应表：wm_batch
 *
 * 【批次是追溯的抓手】
 *   库存是按「物料 + 批次 + 库位」记账的，所以同一颗螺丝，
 *   来自不同供应商、不同生产日期的，在账上是不同的行。
 *   出了问题要追溯"是哪一批的料"，靠的就是这张表和 wm_transaction 流水。
 *
 * 【批次要带哪些属性，由物料自己说了算】
 *   字段一大堆（生产日期、有效期、供应商、客户、工单、模具...），
 *   但不是每个物料都要填全 —— 具体填哪些，去查 E 线的
 *   md_item_batch_config（按物料配置的 14 个开关），
 *   Service 会按这张配置表做必填校验。
 *
 * 【批次编号生成后不允许改】
 *   batch_code 会冗余写进 wm_material_stock / wm_doc_line / wm_transaction，
 *   改了要批量刷一遍，漏一处账就错。要换就新建批次。
 *
 */
@Data
public class WmBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批次ID */
    @NotNull(groups = UpdateOption.class, message = "批次ID不能为空")
    private Long batchId;

    /**
     * 批次编号
     * 由后端按「PC + 日期 + 序列」生成，前端不用传，也不允许改
     */
    private String batchCode;

    /** 产品物料ID */
    @NotNull(groups = CreateOption.class, message = "物料不能为空")
    private Long itemId;

    /** 产品物料编码（Service 现查 md_item 回填） */
    private String itemCode;

    /** 产品物料名称（Service 现查 md_item 回填） */
    private String itemName;

    /** 规格型号（Service 现查 md_item 回填） */
    private String specification;

    /** 单位（Service 现查 md_item 回填） */
    private String unitOfMeasure;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime produceDate;

    /** 有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireDate;

    /** 入库日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recptDate;

    /** 供应商ID */
    private Long vendorId;

    /** 供应商编码 */
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

    /** 销售订单编号（数据库列名叫 co_code，这里的属性名跟实体语义保持一致） */
    private String soCode;

    /** 采购订单编号 */
    private String poCode;

    /** 生产工单ID */
    private Long workorderId;

    /** 生产工单编码 */
    private String workorderCode;

    /** 生产任务ID */
    private Long taskId;

    /** 生产任务编号 */
    private String taskCode;

    /** 工作站ID */
    private Long workstationId;

    /** 工作站编码 */
    private String workstationCode;

    /** 工具ID */
    private Long toolId;

    /** 工具编号 */
    private String toolCode;

    /** 模具ID */
    private Long moldId;

    /** 模具编号 */
    private String moldCode;

    /** 生产批号 */
    @Length(max = 128, message = "生产批号长度不能超过 128 个字符")
    private String lotNumber;

    /** 质量状态 PENDING待检 / QUALIFIED合格 / UNQUALIFIED不合格 / FROZEN冻结 */
    @Pattern(regexp = "^(PENDING|QUALIFIED|UNQUALIFIED|FROZEN)$",
            message = "质量状态只能是 PENDING / QUALIFIED / UNQUALIFIED / FROZEN")
    private String qualityStatus;

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

    /** 生产日期区间查询 - 起（仅查询条件用，不参与增改） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime produceDateStart;

    /** 生产日期区间查询 - 止（仅查询条件用，不参与增改） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime produceDateEnd;

    /** 该批次的库存分布（在哪些仓库库位各有多少），仅详情接口返回 */
    private List<WmMaterialStock> stockList;

    /** 库存总量（详情页汇总显示） */
    private java.math.BigDecimal totalQuantity;
}
