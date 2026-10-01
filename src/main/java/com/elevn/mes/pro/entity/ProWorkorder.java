package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
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
 * 生产工单 实体类
 * 对应表：pro_workorder
 *
 * 工单是生产的源头单据，一单一产品（要多个产品就开多张单）。
 * 三个维度决定它的行为：
 *   1. order_source 来源类型 —— ORDER 客户订单（必须填订单编号 + 选客户）
 *                              STORE 库存备货（不填订单编号与客户）
 *   2. workorder_type 工单类型 —— SELF 自产（需要排产，按产品制程拆工序任务）
 *                                OUTSOURCE 外协 / PURCHASE 外购（需要选供应商，不排产）
 *   3. status 单据状态 —— PREPARE 待下达 → CONFIRMED 已下达 → FINISHED 已完工
 *                                                     ↘ CANCELED 已取消
 *
 * 工单本身只管下达，真正的工序拆分在 pro_task（生产任务）；
 * 下达时会把产品 BOM 展开写进 pro_workorder_bom。
 *
 */
@Data
public class ProWorkorder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工单ID */
    @NotNull(groups = UpdateOption.class, message = "工单ID不能为空")
    private Long workorderId;

    /** 工单编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "工单编码不能为空")
    @Length(max = 64, message = "工单编码长度不能超过 64 个字符")
    private String workorderCode;

    /** 工单名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "工单名称不能为空")
    @Length(max = 255, message = "工单名称长度不能超过 255 个字符")
    private String workorderName;

    /** 工单类型（SELF 自产 / OUTSOURCE 外协 / PURCHASE 外购），字典 workorder_type */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "工单类型不能为空")
    @Pattern(regexp = "^(SELF|OUTSOURCE|PURCHASE)$",
            message = "工单类型只能是 SELF / OUTSOURCE / PURCHASE")
    private String workorderType;

    /** 来源类型（ORDER 客户订单 / STORE 库存备货），字典 order_source */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "来源类型不能为空")
    @Pattern(regexp = "^(ORDER|STORE)$", message = "来源类型只能是 ORDER 或 STORE")
    private String orderSource;

    /** 来源单据 —— 客户订单来源时存订单编号（原样存文本，不校验订单是否在系统里） */
    @Length(max = 64, message = "来源单据长度不能超过 64 个字符")
    private String sourceCode;

    /** 产品ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "产品不能为空")
    private Long productId;

    /** 产品编号 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 规格型号 */
    private String productSpc;

    /** 单位 */
    private String unitOfMeasure;

    /** 生产数量 */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "生产数量不能为空")
    @DecimalMin(value = "0.000001", message = "生产数量必须大于 0")
    private BigDecimal quantity;

    /** 已生产数量（报工时回写，工单编辑不能手改） */
    private BigDecimal quantityProduced;

    /** 调整数量（数量调整留痕） */
    private BigDecimal quantityChanged;

    /** 已排产数量（排产时累加，只有自产工单会用到） */
    private BigDecimal quantityScheduled;

    /** 客户ID */
    private Long clientId;

    /** 客户编码 */
    private String clientCode;

    /** 客户名称 */
    private String clientName;

    /** 供应商ID（外协/外购时必填） */
    private Long vendorId;

    /** 供应商编号 */
    private String vendorCode;

    /** 供应商名称 */
    private String vendorName;

    /** 批次号 */
    private String batchCode;

    /** 需求日期 */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "需求日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;

    /** 父工单（拆单时指向父工单ID，0 表示顶层工单） */
    private Long parentId;

    /** 所有父节点ID（逗号串，用于树形展示） */
    private String ancestors;

    /** 取消日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelDate;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishDate;

    /** 单据状态（PREPARE/CONFIRMED/FINISHED/CANCELED），字典 production_order_status */
    private String status;

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
