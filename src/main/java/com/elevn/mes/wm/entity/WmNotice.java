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
 * 仓储通知单 实体类
 * 对应表：wm_notice
 *
 * 【通知单和出入库单据有什么不一样】
 *   出入库单据（wm_doc）是"已经发生/正在发生的动作"，过账会改库存；
 *   通知单只是【预告】—— 供应商说下周送货、客户说明天要提货、产线说这个工单要备料。
 *   它自己不动库存，作用是：① 提前告诉仓库要做准备 ② 触发质量检验（到货→IQC / 发货→OQC）
 *   ③ 作为下游单据的来源（备料申请 → 生产领料单）。
 *
 * 【3 类合一，靠 notice_type 区分】
 *   ARRIVAL          到货通知：供应商送货前预告，到货后该做来料检验（IQC）
 *   SALES            发货通知：客户要货预告，发货前该做出货检验（OQC）
 *   MATERIAL_REQUEST 备料申请：产线按工单申请备料，是生产领料单的上游
 *
 * 【所以三类必填的字段不一样，由 notice_type 决定】
 *   到货 → 必须有供应商；发货 → 必须有客户；备料申请 → 必须有生产工单。
 *   这条规则在后端校验（checkBizRules），前端只是跟着显示。
 *
 */
@Data
public class WmNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知单ID */
    @NotNull(groups = UpdateOption.class, message = "通知单ID不能为空")
    private Long noticeId;

    /** 通知类型 ARRIVAL 到货 / SALES 发货 / MATERIAL_REQUEST 备料申请 */
    @NotBlank(groups = CreateOption.class, message = "通知类型不能为空")
    @Pattern(regexp = "^(ARRIVAL|SALES|MATERIAL_REQUEST)$",
            message = "通知类型只能是 ARRIVAL / SALES / MATERIAL_REQUEST")
    private String noticeType;

    /** 通知单编号（后端按编码规则生成，前端不用传） */
    private String noticeCode;

    /** 通知单名称 */
    @Length(max = 255, message = "通知单名称长度不能超过 255 个字符")
    private String noticeName;

    /** 采购订单编号 */
    private String poCode;

    /** 销售订单编号 */
    private String soCode;

    // ---------- 往来对象（供应商 / 客户） ----------

    /** 往来对象类型 VENDOR / CLIENT */
    private String partnerType;

    /** 往来对象ID */
    private Long partnerId;

    /** 往来对象编码 */
    private String partnerCode;

    /** 往来对象名称 */
    private String partnerName;

    /** 往来对象简称 */
    private String partnerNick;

    // ---------- 生产相关（备料申请用） ----------

    /** 生产工单ID */
    private Long workorderId;

    /** 生产工单编号 */
    private String workorderCode;

    /** 工作站ID */
    private Long workstationId;

    /** 工作站编号 */
    private String workstationCode;

    /** 工作站名称 */
    private String workstationName;

    // ---------- 申请人（备料申请用） ----------

    /** 申请人ID */
    private Long applicantId;

    /** 申请人用户名 */
    private String applicantName;

    /** 申请人姓名 */
    private String applicantNick;

    // ---------- 时间与联系方式 ----------

    /** 申请 / 需求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestTime;

    /** 要求开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 要求完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 通知日期（到货日期 / 发货日期） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime noticeDate;

    /** 联系人 */
    private String contact;

    /** 联系方式 */
    private String tel;

    /** 收货地址 */
    private String address;

    /** 单据状态 PREPARE 待处理 / CONFIRMED 已触发检验 / FINISHED 已完成 / CANCELED 已取消 */
    @Pattern(regexp = "^(PREPARE|CONFIRMED|FINISHED|CANCELED)$",
            message = "通知单状态只能是 PREPARE / CONFIRMED / FINISHED / CANCELED")
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

    // ==================== 以下为非表字段 ====================

    /** 通知类型中文名（从字典带出） */
    private String noticeTypeName;

    /** 查询用：通知日期区间 - 起 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime noticeDateStart;

    /** 查询用：通知日期区间 - 止 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime noticeDateEnd;

    /** 明细行列表（详情接口一次带出） */
    private List<WmNoticeLine> lineList;

    /** 本次要触发的检验类型 IQC / OQC（触发检验接口返回，非表字段） */
    private String qcType;
}
