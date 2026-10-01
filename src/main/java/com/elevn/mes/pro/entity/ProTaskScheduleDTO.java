package com.elevn.mes.pro.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 排产请求 DTO
 *
 * 排产的入参不是一条 ProTask，而是"给哪张工单选了哪些工作站、排多少数量"，
 * 所以单独定义这个结构，避免把界面上的选择塞进实体类。
 *
 * 为什么要让用户逐道工序选工作站：
 *   同一道工序在车间里可能对应多个工作站（如 PR-20 焊接有 ST-02 焊接工作站、
 *   ST-04 波峰焊接线），到底用哪个是排产员的决策，系统不该替他决定。
 *
 */
@Data
public class ProTaskScheduleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 要排产的工单ID */
    @NotNull(message = "工单不能为空")
    private Long workorderId;

    /**
     * 本次排产数量
     * 支持分批排产：可以只排工单的一部分，剩余数量以后再排。
     * 后端会校验"累计已排 + 本次 <= 工单数量"。
     */
    @NotNull(message = "本次排产数量不能为空")
    @DecimalMin(value = "0.000001", message = "本次排产数量必须大于 0")
    private BigDecimal quantity;

    /** 计划开工时间（第一道工序的开始时间） */
    @NotNull(message = "计划开工时间不能为空")
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime planStartTime;

    /**
     * 每道工序选定的工作站
     * 必须覆盖路线的全部工序，缺一道后端就拒绝 ——
     * 少一道工序的任务，整条工艺链就断了。
     */
    @NotEmpty(message = "请为每道工序指定工作站")
    private List<Item> items;

    /** 工序 -> 工作站 的单条映射 */
    @Data
    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 工序ID */
        @NotNull(message = "工序不能为空")
        private Long processId;

        /** 该工序使用的工作站ID */
        @NotNull(message = "工作站不能为空")
        private Long workstationId;
    }
}
