package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.wm.options.UpdateOption;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单 实体类
 * 对应表：wm_stock_taking
 *
 * 【盘点单是"差异凭证"】
 *   账面数量来自生成那一刻的库存（不是实时的！生成后别人入库出库，
 *   账面数也不跟着动 —— 盘点比的就是"生成那一刻的账"和"实际数出来的货"）。
 *   过账时差异写进库存与流水，写完就锁单。
 *
 */
@Data
public class WmStockTaking implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 盘点单ID */
    private Long takingId;

    /**
     * 盘点单编号
     * 由后端按「PDD + 日期 + 序列」生成
     */
    private String takingCode;

    /** 盘点单名称（默认取计划名称 + 生成日期） */
    private String takingName;

    /** 盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime takingDate;

    /** 盘点类型（从计划带过来） */
    private String takingType;

    /** 盘点人用户名 */
    private String userId;

    /** 盘点人名称 */
    private String userName;

    /** 盘点人昵称 */
    private String nickName;

    /** 是否盲盘（从计划带过来，Y 则列表接口不给前端账面数量） */
    private String blindFlag;

    /** 是否冻结（从计划带过来） */
    private String frozenFlag;

    /** 来源计划ID */
    private Long planId;

    /** 来源计划编号 */
    private String planCode;

    /** 来源计划名称 */
    private String planName;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 状态 PREPARE待录入 / CONFIRMED已过账 / CANCELED已取消 */
    private String status;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    private String remark;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private String updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // ==================== 以下为非表字段 ====================

    /** 盘点日期区间查询 - 起 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime takingDateStart;

    /** 盘点日期区间查询 - 止 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime takingDateEnd;

    /** 盲盘时为 true：响应里剥掉账面数量（Service 层处理） */
    private Boolean blind;

    /** 盘点明细行（详情接口返回） */
    private List<WmStockTakingLine> lineList;

    /** 统计：明细行总数 */
    private Integer lineCount;

    /** 统计：已录行数 */
    private Integer countedCount;

    /** 统计：差异行数 */
    private Integer diffCount;

    /** 统计：盘盈合计（diff > 0 之和） */
    private BigDecimal profitQuantity;

    /** 统计：盘亏合计（diff < 0 之和的绝对值） */
    private BigDecimal lossQuantity;
}
