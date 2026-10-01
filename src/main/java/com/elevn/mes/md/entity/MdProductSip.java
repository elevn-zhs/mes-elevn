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
 * 产品SIP表 实体类
 * 对应表：md_product_sip
 *
 * SIP = Standard Inspection Procedure（标准检验指导书）
 * 与 SOP 结构完全一致，区别在语义：SOP 指导"怎么干"，SIP 指导"怎么检"
 * 质检员在 qc 模块的检验单里按此录入检验项与判定结果
 *
 */
@Data
public class MdProductSip implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @NotNull(groups = UpdateOption.class, message = "SIP记录ID不能为空")
    private Long sipId;

    /** 物料产品ID（关联 md_item.item_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "物料产品ID不能为空")
    private Long itemId;

    /** 排列顺序（同一产品同一工序内的检验项序号，从 1 开始） */
    private Integer orderNum;

    /** 对应的工序ID（关联 pro_process） */
    private Long processId;

    /** 工序编号（冗余字段） */
    @Length(max = 64, message = "工序编号长度不能超过 64 个字符")
    private String processCode;

    /** 工序名称（冗余字段） */
    @Length(max = 255, message = "工序名称长度不能超过 255 个字符")
    private String processName;

    /** SIP标题 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "SIP标题不能为空")
    @Length(max = 255, message = "SIP标题长度不能超过 255 个字符")
    private String sipTitle;

    /** SIP详细描述（检验方法、判定标准） */
    @Length(max = 500, message = "SIP详细描述长度不能超过 500 个字符")
    private String sipDescription;

    /** 图片地址（检验示意图，多个用逗号分隔） */
    @Length(max = 255, message = "图片地址长度不能超过 255 个字符")
    private String sipUrl;

    /** 是否启用（Y是 N否）—— 检验标准换版本时旧版置 N，保留记录用于追溯，不做物理删除 */
    @Length(max = 1, message = "是否启用长度只能为 1 个字符")
    @Pattern(regexp = "^[YN]$", message = "是否启用只能是 Y 或 N")
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
