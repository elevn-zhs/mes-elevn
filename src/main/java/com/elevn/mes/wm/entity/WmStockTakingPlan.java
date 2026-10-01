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
 * 盘点计划 实体类
 * 对应表：wm_stock_taking_plan
 *
 * 【计划和盘点单为什么要分开两张表】
 *   计划回答"盘什么、什么时候盘、谁盘"—— 是安排；
 *   盘点单回答"实际盘出来多少、差多少"—— 是凭证。
 *   一个计划可以生成多张盘点单（比如分仓库分人各领一张），
 *   计划上圈的范围存 wm_stock_taking_scope，生成时展开成明细行。
 *
 * 【盲盘 blind_flag】
 *   盲盘 = 录入的人看不到账面数量，防"照着账抄"。
 *   账面数量照样存在 wm_stock_taking_line.quantity 里，
 *   只是接口不回传给录入页面 —— 防作弊防的是"看见"，不是"存在"。
 *
 */
@Data
public class WmStockTakingPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 计划ID */
    @NotNull(groups = UpdateOption.class, message = "盘点计划ID不能为空")
    private Long planId;

    /**
     * 计划编号
     * 由后端按「PDJH + 日期 + 序列」生成，前端不用传，也不允许改
     */
    private String planCode;

    /** 计划名称 */
    @NotBlank(groups = CreateOption.class, message = "计划名称不能为空")
    @Length(max = 128, message = "计划名称长度不能超过 128 个字符")
    private String planName;

    /** 盘点类型 FULL全盘 / PART按范围 */
    @NotBlank(groups = CreateOption.class, message = "盘点类型不能为空")
    @Pattern(regexp = "^(FULL|PART)$", message = "盘点类型只能是 FULL / PART")
    private String takingType;

    /** 盘点开始时间 */
    @NotNull(groups = CreateOption.class, message = "盘点开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 盘点结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 是否盲盘（Y 录入页不显示账面数量） */
    private String blindFlag;

    /** 是否冻结（盘点期间该范围的库存建议停动，本版本只记录不做强制拦截） */
    private String frozenFlag;

    /** 状态 PREPARE待执行 / CONFIRMED已生成盘点单 / CANCELED已取消 */
    @Pattern(regexp = "^(PREPARE|CONFIRMED|CANCELED)$", message = "状态只能是 PREPARE / CONFIRMED / CANCELED")
    private String status;

    /** 是否启用 */
    private String enableFlag;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    @Length(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // ==================== 以下为非表字段 ====================

    /** 已生成盘点单数（列表页显示"该计划生成过几张单"） */
    private Integer takingCount;

    /** 盘点范围（创建时一起提交，生成时按它展开） */
    private List<WmStockTakingScope> scopeList;
}
