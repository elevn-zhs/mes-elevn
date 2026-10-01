package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.wm.options.UpdateOption;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SN（序列号）实体类
 * 对应表：wm_sn
 *
 * 【SN = 一物一码】
 *   批次管"一批"，SN 管"一个"。控制柜这种贵重设备出质量问题，
 *   要精确到"是哪一台"，靠的就是 SN。
 *   sn_code 按规则 WM_SN 生成（SN+日期+流水），终身不复用。
 *
 * 【和批次/工单的关系】
 *   一个 SN 必须挂在一个批次上（批次管质量状态），
 *   生成时可以带工单（溯源到是哪张工单产出的）。
 *   过站明细（pro_sn_process）是 A 线报工时写的，B 线只负责读出来展示。
 *
 */
@Data
public class WmSn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SN ID */
    private Long snId;

    /**
     * SN 码
     * 由后端按「SN + 日期 + 流水」生成，一物一码，终身不复用
     */
    private String snCode;

    /** 产品物料ID */
    @NotNull(groups = UpdateOption.class, message = "产品物料不能为空")
    private Long itemId;

    /** 产品物料编码 */
    private String itemCode;

    /** 产品物料名称 */
    private String itemName;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 批次编码（SN 必须挂在批次上，批次管质量状态） */
    private String batchCode;

    /** 生成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime genDate;

    /** 工单ID（溯源到是哪张工单产出的，选填） */
    private Long workorderId;

    /** 状态 IN_STOCK在库 / SHIPPED已发货 / FROZEN冻结 */
    @Pattern(regexp = "^(IN_STOCK|SHIPPED|FROZEN)$", message = "状态只能是 IN_STOCK / SHIPPED / FROZEN")
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

    /** 生成数量（批量生成接口的入参，不落库） */
    private Integer count;

    /** 工单编码（join pro_workorder 展示） */
    private String workorderCode;

    /** 查询用：SN 码模糊 */
    private String snKeyword;
}
