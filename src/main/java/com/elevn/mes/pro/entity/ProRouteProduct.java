package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品制程 实体类
 * 对应表：pro_route_product
 *
 * 产品制程 = 把工艺路线挂到具体产品上，并声明"生产这么多数量、正常要用多长时间"。
 * 制程物料BOM（pro_route_product_bom）按工序决定每道工序的用料比例。
 * 工单下达时就是根据这张表找到产品该走的路线。
 *
 * quantity 是 int（生产数量），production_time 是 decimal（生产用时）。
 *
 */
@Data
public class ProRouteProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @NotNull(groups = UpdateOption.class, message = "记录ID不能为空")
    private Long recordId;

    /** 路线ID（关联 pro_route.route_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "路线ID不能为空")
    private Long routeId;

    /** 产品ID（关联 md_item.item_id，必须是产品） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "产品ID不能为空")
    private Long itemId;

    /** 产品编码（冗余，后端回填） */
    @Length(max = 64, message = "产品编码长度不能超过 64 个字符")
    private String itemCode;

    /** 产品名称（冗余，后端回填） */
    @Length(max = 255, message = "产品名称长度不能超过 255 个字符")
    private String itemName;

    /** 规格（冗余，后端回填） */
    @Length(max = 500, message = "规格长度不能超过 500 个字符")
    private String specification;

    /** 单位（冗余，后端回填） */
    @Length(max = 64, message = "单位长度不能超过 64 个字符")
    private String unitOfMeasure;

    /** 生产数量 */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "生产数量不能为空")
    private Integer quantity;

    /** 生产用时 */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "生产用时不能为空")
    private BigDecimal productionTime;

    /** 用时单位（H小时/M分钟/D天，字典配置） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "用时单位不能为空")
    @Length(max = 64, message = "用时单位长度不能超过 64 个字符")
    private String timeUnitType;

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
