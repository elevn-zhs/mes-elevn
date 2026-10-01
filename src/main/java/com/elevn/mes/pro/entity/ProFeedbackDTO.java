package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报工入参 DTO
 *
 * 报工不是"插入一条 pro_feedback"那么简单，它要一次性完成：
 *   1. 校验任务可报数量（累计不许超过排产数量）
 *   2. 写报工记录
 *   3. 回写任务已生产/合格/不良数量与状态
 *   4. 任务报满后累加工单已生产数量
 * 所以入参只收"用户真正要填的东西"，其余冗余字段（工单编码、工序名、产品名…）
 * 一律由后端按 taskId 现查主表回填，不信前端传的，避免脏数据。
 *
 */
@Data
public class ProFeedbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 生产任务ID（报工的落点，其余冗余信息都从这条任务派生） */
    @NotNull(message = "生产任务不能为空")
    private Long taskId;

    /** 报工类型（FIRST / PROCESS / FINISH），字典 feedback_type */
    private String feedbackType;

    /** 报工途径（PC / SCAN / TERMINAL），字典 feedback_channel */
    private String feedbackChannel;

    /** 本次报工数量（= 合格 + 不良 + 待检） */
    @NotNull(message = "本次报工数量不能为空")
    @DecimalMin(value = "0.000001", message = "本次报工数量必须大于 0")
    private BigDecimal quantityFeedback;

    /** 合格品数量（不填按 0 算） */
    @DecimalMin(value = "0", message = "合格品数量不能为负")
    private BigDecimal quantityQualified;

    /** 不良品数量（不填按 0 算） */
    @DecimalMin(value = "0", message = "不良品数量不能为负")
    private BigDecimal quantityUnqualified;

    /** 待检测数量（不填时后端自动算：本次报工 - 合格 - 不良） */
    @DecimalMin(value = "0", message = "待检测数量不能为负")
    private BigDecimal quantityUncheck;

    /** 生产批号（不填则沿用任务所属工单的批次号） */
    private String lotNumber;

    /** 报工时间（不填取当前时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feedbackTime;

    /** 备注 */
    private String remark;
}
