package com.elevn.mes.pro.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工序用料需求 展示对象（非表实体）
 *
 * 回答一个车间最常问的问题：**这道工序该用哪些料、要用多少、已经用了多少、还差多少**。
 *
 * 数据来自三处：
 *   单位用量  unitQty     <- pro_route_product_bom.quantity（做 1 件要用多少）
 *   应耗      planQty     <- 任务数量 × 单位用量
 *   已耗      consumedQty <- pro_trans_consume 按 (task_id, item_id) 汇总
 *
 * 这个对象是"投入产出比对"的载体：
 *   diffQty > 0 → 应耗大于已耗，料还没投够，不能完工；
 *   diffQty < 0 → 实际消耗超出应耗，说明有损耗（不良、报废、试机），要预警。
 *
 */
@Data
public class ProMaterialRequire implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 物料ID */
    private Long itemId;

    /** 物料编码 */
    private String itemCode;

    /** 物料名称 */
    private String itemName;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 单位名称 */
    private String unitName;

    /** 单位用量：做 1 件产品，这道工序要摊多少该物料 */
    private BigDecimal unitQty;

    /** 应耗数量 = 任务排产数量 × 单位用量 */
    private BigDecimal planQty;

    /** 已消耗数量 = 该工序该物料的累计消耗 */
    private BigDecimal consumedQty;

    /** 差额 = 应耗 - 已耗（负数表示超耗 / 有损耗） */
    private BigDecimal diffQty;

    /** 是否超耗（已耗 > 应耗），前端据此标红 */
    private Boolean overFlag;
}
