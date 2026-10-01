package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物料批次属性配置表 实体类
 * 对应表：md_item_batch_config
 *
 * 一个物料一条配置，用来决定"这个物料的批次号要记录哪些属性"
 * 14 个 *_flag 字段全部是 Y/N：Y 表示该属性参与批次管理，生成批次时必须采集
 * 表结构上是 14 列，实际业务里也可以按"属性编码 + 值"纵向存，这里按原表横排保留
 *
 */
@Data
public class MdItemBatchConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批次配置ID */
    @NotNull(groups = UpdateOption.class, message = "批次配置ID不能为空")
    private Long configId;

    /** 产品物料ID（关联 md_item.item_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料ID不能为空")
    private Long itemId;

    /** 生产日期（Y采集 N不采集） */
    @Length(max = 1, message = "生产日期标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "生产日期标志只能是 Y 或 N")
    private String produceDateFlag;

    /** 有效期（Y采集 N不采集） */
    @Length(max = 1, message = "有效期标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "有效期标志只能是 Y 或 N")
    private String expireDateFlag;

    /** 入库日期（Y采集 N不采集） */
    @Length(max = 1, message = "入库日期标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "入库日期标志只能是 Y 或 N")
    private String recptDateFlag;

    /** 供应商（Y采集 N不采集） */
    @Length(max = 1, message = "供应商标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "供应商标志只能是 Y 或 N")
    private String vendorFlag;

    /** 客户（Y采集 N不采集） */
    @Length(max = 1, message = "客户标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "客户标志只能是 Y 或 N")
    private String clientFlag;

    /** 销售订单编号（Y采集 N不采集） */
    @Length(max = 1, message = "销售订单编号标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "销售订单编号标志只能是 Y 或 N")
    private String coCodeFlag;

    /** 采购订单编号（Y采集 N不采集） */
    @Length(max = 1, message = "采购订单编号标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "采购订单编号标志只能是 Y 或 N")
    private String poCodeFlag;

    /** 生产工单（Y采集 N不采集） */
    @Length(max = 1, message = "生产工单标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "生产工单标志只能是 Y 或 N")
    private String workorderFlag;

    /** 生产任务（Y采集 N不采集） */
    @Length(max = 1, message = "生产任务标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "生产任务标志只能是 Y 或 N")
    private String taskFlag;

    /** 工作站（Y采集 N不采集） */
    @Length(max = 1, message = "工作站标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "工作站标志只能是 Y 或 N")
    private String workstationFlag;

    /** 工具（Y采集 N不采集） */
    @Length(max = 1, message = "工具标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "工具标志只能是 Y 或 N")
    private String toolFlag;

    /** 模具（Y采集 N不采集） */
    @Length(max = 1, message = "模具标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "模具标志只能是 Y 或 N")
    private String moldFlag;

    /** 生产批号（Y采集 N不采集） */
    @Length(max = 1, message = "生产批号标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "生产批号标志只能是 Y 或 N")
    private String lotNumberFlag;

    /** 质量状态（Y采集 N不采集） */
    @Length(max = 1, message = "质量状态标志长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "质量状态标志只能是 Y 或 N")
    private String qualityStatusFlag;

    /** 生效状态（Y生效 N失效） */
    @Length(max = 1, message = "生效状态长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "生效状态只能是 Y 或 N")
    private String enableFlag;

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
