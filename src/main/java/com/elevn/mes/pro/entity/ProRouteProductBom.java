package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 制程物料BOM 实体类
 * 对应表：pro_route_product_bom
 *
 * 制程BOM行 = 某产品的某道工序要消耗哪些物料、各消耗多少（quantity 是单套比例）。
 * 通过 (product_id, route_id, process_id) 软关联：产品走哪条路线、路线里哪道工序。
 * 领料和投料都按这里的比例展开。
 *
 * quantity 是 decimal(18,6) —— 用料比例允许小数（如 0.0025 千克），实体必须用 BigDecimal。
 *
 */
@Data
public class ProRouteProductBom implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 路线ID（关联 pro_route.route_id） */
    private Long routeId;

    /** 工序ID（关联 pro_process.process_id，必须是该路线内的工序） */
    private Long processId;

    /** 产品ID（关联 md_item.item_id） */
    private Long productId;

    /** 物料ID（关联 md_item.item_id，被消耗的物料） */
    private Long itemId;

    /** 物料编码（冗余，后端回填） */
    @Length(max = 64, message = "物料编码长度不能超过 64 个字符")
    private String itemCode;

    /** 物料名称（冗余，后端回填） */
    @Length(max = 255, message = "物料名称长度不能超过 255 个字符")
    private String itemName;

    /** 规格（冗余，后端回填） */
    @Length(max = 500, message = "规格长度不能超过 500 个字符")
    private String specification;

    /** 单位（冗余，后端回填） */
    @Length(max = 64, message = "单位长度不能超过 64 个字符")
    private String unitOfMeasure;

    /** 单套用量 */
    private BigDecimal quantity;

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
}
