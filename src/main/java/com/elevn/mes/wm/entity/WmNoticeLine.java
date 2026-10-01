package com.elevn.mes.wm.entity;

import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓储通知单行 实体类
 * 对应表：wm_notice_line
 *
 * 【一行 = 一种物料要多少】
 *   到货通知：这次送来哪些料、各多少
 *   发货通知：这次客户要提走哪些成品、各多少
 *   备料申请：这个工单需要领哪些料、各多少
 *
 * 【qc_flag 的意义】
 *   触发检验后，行上的 qc_flag 置 'Y'，表示"这一行已经报检"。
 *   检验单回来之后把 qc_id / qc_code / quantity_qualified 回填到这里，
 *   这样"这批料检了没有、检的结果如何"在通知单上就能看到。
 *   （目前 C 线的检验单接口还没就绪，这一段先留着 TODO）
 *
 */
@Data
public class WmNoticeLine implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 行ID */
    @NotNull(groups = UpdateOption.class, message = "行ID不能为空")
    private Long lineId;

    /** 通知单ID */
    @NotNull(groups = CreateOption.class, message = "通知单ID不能为空")
    private Long noticeId;

    /** 通知类型（冗余，便于按类型统计） */
    private String noticeType;

    /** 行号 */
    private Integer lineNo;

    /** 产品物料ID */
    @NotNull(groups = CreateOption.class, message = "物料不能为空")
    private Long itemId;

    /** 产品物料编码 */
    private String itemCode;

    /** 产品物料名称 */
    private String itemName;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 单位名称 */
    private String unitName;

    /** 通知数量 */
    @NotNull(groups = CreateOption.class, message = "数量不能为空")
    @DecimalMin(value = "0.000001", groups = CreateOption.class, message = "数量必须大于 0")
    private BigDecimal quantity;

    /** 合格数量（检验完成后回填） */
    private BigDecimal quantityQualified;

    /** 批次ID */
    private Long batchId;

    /** 批次号 */
    private String batchCode;

    /** 是否检验（Y是 N否） */
    private String qcFlag;

    /** 检验单ID（触发检验后由 C 线回填） */
    private Long qcId;

    /** 检验单编号 */
    private String qcCode;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    @Length(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
