package com.elevn.mes.wm.entity;

import com.elevn.mes.wm.options.CreateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 盘点范围 实体类
 * 对应表：wm_stock_taking_scope
 *
 * 【一行 = 圈了一块地方或一类料】
 *   scope_type 决定 scope_value_* 里存的是什么：
 *   WAREHOUSE -> 仓库三件套 / AREA -> 库区三件套 / ITEM_TYPE -> 物料分类三件套。
 *   生成盘点单时把每个范围翻译成一批库存行，多个范围取并集去重。
 *
 */
@Data
public class WmStockTakingScope implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 范围ID */
    private Long scopeId;

    /** 盘点计划ID */
    @NotNull(groups = CreateOption.class, message = "盘点计划ID不能为空")
    private Long planId;

    /** 范围类型 WAREHOUSE / AREA / ITEM_TYPE */
    @NotBlank(groups = CreateOption.class, message = "范围类型不能为空")
    @Pattern(regexp = "^(WAREHOUSE|AREA|ITEM_TYPE)$", message = "范围类型只能是 WAREHOUSE / AREA / ITEM_TYPE")
    private String scopeType;

    /** 范围对象ID */
    @NotNull(groups = CreateOption.class, message = "范围对象不能为空")
    private Long scopeValueId;

    /** 范围对象编码 */
    private String scopeValueCode;

    /** 范围对象名称 */
    private String scopeValueName;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    private String remark;
}
