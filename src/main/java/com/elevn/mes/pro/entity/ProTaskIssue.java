package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产任务投料 实体类
 * 对应表：pro_task_issue
 *
 * 【这张表是 A/B 两条线的接口】
 *   仓库那边把生产领料单过账了，货真的从库里出去了；
 *   生产这边需要一个"这个任务领了多少料"的凭据，就是本表。
 *   所以它的 source_doc_* 三个字段指向的是 wm_doc（领料单）或 wm_notice（备料申请）。
 *
 * 【source_doc_id 是 NOT NULL】
 *   投料必须有来源。没有来源单据就凭空的投料记录，追溯时解释不通
 *   （这条料到底是从哪张领料单出的？），所以建表时就把这一列设成非空。
 *
 * 【三个数量的含义，别混】
 *   quantity_issued    —— 这张单投了多少（一次领料的总量）
 *   quantity_available —— 当前还能用多少（没用完的部分）
 *   quantity_used      —— 已经用掉多少
 *   恒等式：quantity_available + quantity_used = quantity_issued
 *
 */
@Data
public class ProTaskIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 生产任务ID */
    private Long taskId;

    /** 生产工单ID */
    private Long workorderId;

    /** 工作站ID */
    private Long workstationId;

    /** 来源单据表名（wm_doc=领料单 / wm_notice=备料申请单） */
    private String sourceDocTable;

    /** 来源单据ID（NOT NULL：投料必须有来源） */
    private Long sourceDocId;

    /** 来源单据类型（ISSUE=生产领料 / MATERIAL_REQUEST=备料申请） */
    private String sourceDocType;

    /** 来源单据编号 */
    private String sourceDocCode;

    /** 投料批次 */
    private String batchCode;

    /** 来源单据行ID */
    private Long sourceLineId;

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

    /** 总的投料数量 */
    private BigDecimal quantityIssued;

    /** 当前可用数量 */
    private BigDecimal quantityAvailable;

    /** 当前使用数量 */
    private BigDecimal quantityUsed;

    /** 删除标志（0存在 1删除） */
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
}
