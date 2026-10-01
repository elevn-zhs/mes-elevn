package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序流转卡 实体类
 * 对应表：pro_card
 *
 * 流转卡是跟着物料走的那张"随工单"——现实车间里每批活儿挂一张卡，
 * 过一道工序盖一个章。系统里一张卡对应一张生产工单（一工单一卡），
 * 卡上的每道工序是一条 pro_card_process 过站记录。
 *
 * 生命周期（状态字典 pro_card_status）：
 *   PENDING  待流转 —— 排产确认后建卡，还没人开工
 *   RUNNING  流转中 —— 首道工序有产出（首次报工推进）
 *   FINISHED 已完工 —— 末道工序产出达到流转数量
 *   CANCELED 已取消 —— 工单取消时一并取消
 *
 * 数量口径：
 *   quantity_transferred = 流转数量（取工单数量，本批工单计划流转的总量）
 *   过站明细的 quantity_input = 上一道累计产出（首道 = 流转数量）
 *   过站明细的 quantity_output = 累计产出（合格 + 不良），不良单记 quantity_unqualified
 *
 * 卡与过站记录的维护只有一个入口：
 *   排产（ProTaskServiceImpl.schedule）建卡 + 铺工序行
 *   报工（ProFeedbackServiceImpl.feedback）推进过站 —— 一切发生在同一个事务里
 *
 */
@Data
public class ProCard implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流转卡ID */
    @NotNull(groups = UpdateOption.class, message = "流转卡ID不能为空")
    private Long cardId;

    /** 流转卡编号（LC + 日期 + 流水，建卡后回填） */
    @Length(max = 64, message = "流转卡编号长度不能超过 64 个字符")
    private String cardCode;

    /** 生产工单ID */
    @NotNull(groups = CreateOption.class, message = "生产工单不能为空")
    private Long workorderId;

    /** 生产工单编号（冗余） */
    private String workorderCode;

    /** 生产工单名称（冗余） */
    private String workorderName;

    /** 批次号（默认取工单编号，等 B 线批次管理接入后可换成真实批次） */
    @Length(max = 64, message = "批次号长度不能超过 64 个字符")
    private String batchCode;

    /** 产品物料ID */
    private Long itemId;

    /** 产品物料编码 */
    @NotBlank(groups = CreateOption.class, message = "产品编码不能为空")
    private String itemCode;

    /** 产品物料名称 */
    @NotBlank(groups = CreateOption.class, message = "产品名称不能为空")
    private String itemName;

    /** 规格型号（冗余） */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 赋码地址（打印二维码用，预留） */
    private String barcodeUrl;

    /** 流转数量（取工单数量） */
    private BigDecimal quantityTransferred;

    /** 流转卡状态（PENDING待流转 / RUNNING流转中 / FINISHED已完工 / CANCELED已取消），字典 pro_card_status */
    private String status;

    /** 删除标志 0-存在 1-删除 */
    private String delFlag;

    /** 备注 */
    private String remark;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
