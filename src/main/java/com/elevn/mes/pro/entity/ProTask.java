package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产任务 实体类
 * 对应表：pro_task
 *
 * 生产任务是排产的产物 —— 一张工单按产品制程的工艺路线，拆成 N 道工序任务。
 * 它是 MES 里真正"落地到产线"的那一层：
 *   工单（pro_workorder）  = 要做什么、做多少        —— 计划层
 *   任务（pro_task）       = 谁做、什么时候做        —— 执行层（本表）
 *   报工（pro_feedback）   = 实际做了多少、合格多少  —— 反馈层
 *
 * 排产生成规则：
 *   1. 工单必须是 SELF 自产 + CONFIRMED 已下达
 *   2. 路线来自 pro_route_product（下达时已校验过必须存在）
 *   3. 路线上的每道工序生成一条任务，数量 = 本次排产数量
 *   4. 工作站由用户为每道工序指定（同一工序可能对应多个工作站）
 *   5. 时间按 link_type 分流：FS 顺排接上一道结束时间，SS 与上一道同时开工
 *
 * 任务编号规则：workorderCode + '-' + 序位（如 WO202609200011-03），
 * 一眼能看出属于哪张工单的第几道工序，语义比流水号强。
 *
 */
@Data
public class ProTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务ID */
    @NotNull(groups = UpdateOption.class, message = "任务ID不能为空")
    private Long taskId;

    /** 任务编号（workorderCode-序位） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "任务编号不能为空")
    @Length(max = 64, message = "任务编号长度不能超过 64 个字符")
    private String taskCode;

    /** 任务名称（工序名 + 工单名） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "任务名称不能为空")
    @Length(max = 255, message = "任务名称长度不能超过 255 个字符")
    private String taskName;

    /** 生产工单ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工单不能为空")
    private Long workorderId;

    /** 生产工单编号（冗余） */
    private String workorderCode;

    /** 工单名称（冗余） */
    private String workorderName;

    /** 工作站ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工作站不能为空")
    private Long workstationId;

    /** 工作站编号（冗余） */
    private String workstationCode;

    /** 工作站名称（冗余） */
    private String workstationName;

    /** 工艺路线ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工艺路线不能为空")
    private Long routeId;

    /** 工艺路线编号（冗余） */
    private String routeCode;

    /** 工序ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工序不能为空")
    private Long processId;

    /** 工序编码（冗余） */
    private String processCode;

    /** 工序名称（冗余） */
    private String processName;

    /** 产品物料ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "产品不能为空")
    private Long itemId;

    /** 产品物料编码（冗余） */
    private String itemCode;

    /** 产品物料名称（冗余） */
    private String itemName;

    /** 规格型号（冗余） */
    private String specification;

    /** 单位 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "单位不能为空")
    private String unitOfMeasure;

    /** 单位名称 */
    private String unitName;

    /** 排产数量（本次排产的数量，不是工单总量） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "排产数量不能为空")
    @DecimalMin(value = "0.000001", message = "排产数量必须大于 0")
    private BigDecimal quantity;

    /** 已生产数量（报工时累加，排产不能手改） */
    private BigDecimal quantityProduced;

    /** 合格品数量（报工时累加） */
    private BigDecimal quantityQualified;

    /** 不良品数量（报工时累加） */
    private BigDecimal quantityUnqualified;

    /** 调整数量 */
    private BigDecimal quantityChanged;

    /** 客户ID（冗余自工单） */
    private Long clientId;

    /** 客户编码（冗余） */
    private String clientCode;

    /** 客户名称（冗余） */
    private String clientName;

    /** 客户简称（冗余） */
    private String clientNick;

    /** 开始生产时间（排产算出） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 生产时长（分钟，排产算出） */
    private Integer duration;

    /** 完成生产时间（开始时间 + 时长） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 甘特图显示颜色（取自路线工序的 color_code） */
    @Length(max = 7, message = "颜色值长度只能为 7 个字符（#RRGGBB）")
    private String colorCode;

    /** 需求日期（冗余自工单） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;

    /** 完成日期（实际完工时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishDate;

    /** 取消日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelDate;

    /** 生产状态（NORMAL/WORKING/FINISHED/CANCELED），字典 pro_task_status */
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

    // ============================================================
    // 非表字段：排产入参 / 展示用
    // ============================================================

    /**
     * 排产时每道工序要用的工作站ID
     * 排产接口的入参不是完整的 ProTask，而是"工单 + 每道工序选哪个工作站"，
     * 所以这个字段只用于接收前端选择，不落库。
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private transient Long scheduleWorkstationId;
}
