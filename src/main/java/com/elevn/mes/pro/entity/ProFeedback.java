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
 * 生产报工 实体类
 * 对应表：pro_feedback
 *
 * 报工是 MES 三层里的"反馈层"，也是整个生产链路唯一产生真实产量的地方：
 *   工单（pro_workorder）  = 要做什么、做多少        —— 计划层
 *   任务（pro_task）       = 谁做、什么时候做        —— 执行层
 *   报工（pro_feedback）   = 实际做了多少、合格多少  —— 反馈层（本表）
 *
 * 一条报工挂在一条工序任务上（task_id 非空），一次可以报一部分数量（分批报工）。
 * 报工后由 Service 一体回写：
 *   pro_task.quantity_produced / qualified / unqualified 累加
 *   pro_task.status 首次报工 → WORKING，报满 → FINISHED
 *   该工单全部任务完工 → pro_workorder.quantity_produced 累加
 *
 * 数量口径（三者关系必须自洽）：
 *   本次报工数量 = 合格品数量 + 不良品数量 + 待检测数量
 *   其中"待检测数量"是推给质量模块（C）做检验的，本模块不判定合格与否。
 *
 * 报错了怎么办 —— 冲销（reverse）而不是删除：
 *   报工会连带改任务数量、生成物料消耗、推进流转卡。直接删行会让这些下游数据
 *   变成"没有来源"的孤儿，所以冲销要把它们一起回退，并把本行状态置为 REVERSED
 *   同时记下冲销人/时间/原因。删行只有一种情况允许：这条报工从没回写过数量。
 *
 */
@Data
public class ProFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @NotNull(groups = UpdateOption.class, message = "记录ID不能为空")
    private Long recordId;

    /** 报工类型（FIRST首件 / PROCESS过程 / FINISH完工），字典 feedback_type */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "报工类型不能为空")
    private String feedbackType;

    /** 报工单编号（FB + 日期 + 流水） */
    @Length(max = 64, message = "报工单编号长度不能超过 64 个字符")
    private String feedbackCode;

    /** 工作站ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工作站不能为空")
    private Long workstationId;

    /** 工作站编号（冗余） */
    private String workstationCode;

    /** 工作站名称（冗余） */
    private String workstationName;

    /** 生产工单ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工单不能为空")
    private Long workorderId;

    /** 生产工单编号（冗余） */
    private String workorderCode;

    /** 生产工单名称（冗余） */
    private String workorderName;

    /** 工艺流程ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工艺路线不能为空")
    private Long routeId;

    /** 工艺流程编号（冗余） */
    private String routeCode;

    /** 工序ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工序不能为空")
    private Long processId;

    /** 工序编码（冗余） */
    private String processCode;

    /** 工序名称（冗余） */
    private String processName;

    /**
     * 生产任务ID（报工必须挂在工序任务上，不允许直接对工单报工）
     * 只有挂在任务上才能算出"这道工序还剩多少没报"
     */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "生产任务不能为空")
    private Long taskId;

    /** 生产任务编号（冗余） */
    private String taskCode;

    /** 产品物料ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "产品不能为空")
    private Long itemId;

    /** 产品物料编码（冗余） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料编码不能为空")
    private String itemCode;

    /** 产品物料名称（冗余） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料名称不能为空")
    private String itemName;

    /** 单位 */
    private String unitOfMeasure;

    /** 单位名称 */
    private String unitName;

    /** 规格型号（冗余） */
    private String specification;

    /** 过期日期（批次有效期） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireDate;

    /** 生产批号 */
    @Length(max = 128, message = "生产批号长度不能超过 128 个字符")
    private String lotNumber;

    /** 排产数量（报工时刻该任务的排产数量，做快照便于追溯） */
    private BigDecimal quantity;

    /** 本次报工数量 */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "本次报工数量不能为空")
    @DecimalMin(value = "0.000001", message = "本次报工数量必须大于 0")
    private BigDecimal quantityFeedback;

    /** 合格品数量 */
    private BigDecimal quantityQualified;

    /** 不良品数量 */
    private BigDecimal quantityUnqualified;

    /** 待检测数量（推给质量模块检验，本模块不判定） */
    private BigDecimal quantityUncheck;

    /** 报工用户名 */
    private String userName;

    /** 昵称 */
    private String nickName;

    /** 报工途径（PC / SCAN / TERMINAL），字典 feedback_channel */
    private String feedbackChannel;

    /** 报工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feedbackTime;

    /** 记录人 */
    private String recordUser;

    /** 记录人名称 */
    private String recordNick;

    /** 报工状态（SUBMITTED/CONFIRMED/REVERSED），字典 pro_feedback_status */
    private String status;

    /**
     * 冲销原因（冲销时必填，留痕用）
     *
     * 冲销刻意不删行 —— 报工是真实发生过的业务事实，删了以后就再也说不清
     * "这批到底报过没有、为什么又撤了"。所以状态改成 REVERSED 并留下原因。
     */
    @Length(max = 500, message = "冲销原因长度不能超过 500 个字符")
    private String reverseReason;

    /** 冲销人 */
    private String reverseBy;

    /** 冲销时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reverseTime;

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
