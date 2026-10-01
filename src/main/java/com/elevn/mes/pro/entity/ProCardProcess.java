package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序流转卡-工序过站记录 实体类
 * 对应表：pro_card_process
 *
 * 一张流转卡（pro_card）上有 N 行过站记录，与工艺路线的工序一一对应：
 *   第 1 道投料 → 报工产出 → 第 2 道投料 → …… → 末道产出 → 卡完工。
 *
 * 数量口径（与 pro_card 注释一致）：
 *   quantity_input  = 上一道的累计产出（首道 = 卡的流转数量）
 *   quantity_output = 本道累计产出（合格 + 不良）
 *   quantity_unqualified = 本道累计不良（只是记录，不影响推进）
 *
 * 维护入口（同 pro_card）：
 *   排产时随卡一起铺行（此时工作站已知、操作工未知 → user_id 存 0）
 *   报工时推进：写 output_time / quantity_output / 操作工，并给下一道写 input_time / quantity_input
 *
 */
@Data
public class ProCardProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流水ID */
    private Long recordId;

    /** 流转卡ID */
    private Long cardId;

    /** 流转卡编号（冗余） */
    private String cardCode;

    /** 序号（与工艺路线的工序顺序一致，从 1 开始） */
    private Integer seqNum;

    /** 工序ID */
    private Long processId;

    /** 工序编号（冗余） */
    private String processCode;

    /** 工序名称（冗余） */
    private String processName;

    /** 进入工序时间（上一道产出 / 首道开工时写入） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inputTime;

    /** 出工序时间（本道报工时写入，多次报工取最后一次） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outputTime;

    /** 投入数量（累计） */
    private BigDecimal quantityInput;

    /** 产出数量（累计 = 合格 + 不良） */
    private BigDecimal quantityOutput;

    /** 不合格品数量（累计） */
    private BigDecimal quantityUnqualified;

    /** 工作站ID（排产时从任务带来） */
    private Long workstationId;

    /** 工作站编号（冗余） */
    private String workstationCode;

    /** 工作站名称（冗余） */
    private String workstationName;

    /** 操作工ID（排产铺行时未知，存 0；报工推进时回填） */
    private Long userId;

    /** 操作工账号（冗余） */
    private String userName;

    /** 操作工昵称（冗余） */
    private String nickName;

    /** 过程检验单ID（预留，对接 C 线 IPQC 后回填） */
    private Long ipqcId;

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
