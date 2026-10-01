package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料消耗记录 实体类
 * 对应表：pro_trans_consume
 *
 * ============================================================
 * 一、这张表在链路里的位置
 * ============================================================
 *   报工总线的完整定义（分工方案 S7 原话）：
 *     回写数量台账 + 生成物料消耗 + 推进流转卡
 *   本表就是其中的「生成物料消耗」那一半。
 *
 *   报工人报的是"我干了多少活"，消耗记录回答的是"干这些活用掉了多少料"。
 *   两者在同一个事务里产生，所以只要报工成功，消耗一定跟着落账。
 *
 * ============================================================
 * 二、消耗数量怎么来的：倒冲（backflush）
 * ============================================================
 *   车间现场不会为每颗螺丝单独开一张领料单，真实做法是：
 *     按产出的成品数量 × 制程BOM 上的单位用量，反推回去记消耗 —— 这叫倒冲。
 *
 *   所以：本次消耗 = 本次产出（合格 + 不良）× 单位用量
 *
 *   为什么不良也要算消耗？
 *     料已经投进去、已经加工过了，做成不良品它照样把料吃掉了。
 *     这正是「损耗」的来源 —— 合格品只拿走合格的部分，不良品白搭一份料。
 *
 * ============================================================
 * 三、用料比例从哪取
 * ============================================================
 *   pro_route_product_bom 是「产品 + 路线 + 工序 + 物料」四维的用料表：
 *     route_id   = 工艺路线
 *     process_id = 工序（这道工序要用哪些料）
 *     product_id = 产品物料ID（md_item.item_id，回答"这是哪个产品的用料"）
 *     item_id    = 被消耗的物料ID
 *     quantity   = 单位用量（做 1 件产品，这道工序要摊多少该物料）
 *   任务上恰好带齐了 route_id / process_id / item_id，不用额外关联。
 *
 * ============================================================
 * 四、与流转卡的关系
 * ============================================================
 *   trans_order_id / trans_order_code 存的是流转卡（pro_card）——
 *   料是跟着流转卡走的，这样按卡（批次）就能把"投入 → 产出 → 消耗"串成一条链。
 *
 */
@Data
public class ProTransConsume implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 流转单ID（此处指向流转卡 pro_card.card_id） */
    private Long transOrderId;

    /** 流转单编号（流卡编号，如 LC20260923001001） */
    private String transOrderCode;

    /** 生产任务ID */
    private Long taskId;

    /**
     * 来源报工ID（倒冲自动生成时回填）
     *
     * 冲销一条报工要把它倒冲出来的消耗一起回退。如果消耗记录不挂回来源报工，
     * 回退就只能按「任务 + 时间」去猜 —— 同一道工序分批报工的场景下必然误杀。
     */
    private Long feedbackId;

    /** 工作站ID（消耗发生时所在工位） */
    private Long workstationId;

    /** 工序ID */
    private Long processId;

    /** 生产工单ID */
    private Long workorderId;

    /** 批次号 */
    private String batchCode;

    /** 来源单据表名（wm_doc=领料单 / wm_notice=备料申请单；倒冲场景留空） */
    private String sourceDocTable;

    /** 被消耗单据ID（倒冲场景留空） */
    private Long sourceDocId;

    /** 被消耗单据编号 */
    private String sourceDocCode;

    /** 来源单据类型（ISSUE=生产领料 / MATERIAL_REQUEST=备料申请 / BACKFLUSH=倒冲） */
    private String sourceDocType;

    /** 被消耗单据行ID */
    private Long sourceLineId;

    /** 被消耗物料批次号 */
    private String sourceBatchCode;

    /** 被消耗产品物料ID */
    private Long itemId;

    /** 被消耗产品物料编码 */
    private String itemCode;

    /** 被消耗产品物料名称 */
    private String itemName;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 消耗数量 */
    private BigDecimal quantityConsumed;

    /** 消耗时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeDate;

    /** 删除标志 0-存在 1-删除 */
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

    // ============================================================
    // 以下为非表字段：列表页联查带出，resultMap 不映射
    // ============================================================

    /** 任务编号（联查 pro_task 带出） */
    private String taskCode;

    /** 工单编号（联查 pro_workorder 带出） */
    private String workorderCode;

    /** 工序编码（联查 pro_task 带出） */
    private String processCode;

    /** 工序名称（联查 pro_task 带出） */
    private String processName;

    /** 工作站编码（联查 md_workstation 带出） */
    private String workstationCode;

    /** 工作站名称（联查 md_workstation 带出） */
    private String workstationName;

    /** 产品名称（联查 pro_workorder 带出，列表页好认） */
    private String productName;

    /** 消耗时间起（查询条件，非表字段） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeDateFrom;

    /** 消耗时间止（查询条件，非表字段） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeDateTo;
}
