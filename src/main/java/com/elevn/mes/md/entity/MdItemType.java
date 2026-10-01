package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 物料产品分类表 实体类
 * 对应表：md_item_type
 *
 * 树形结构：parent_type_id = 0 表示顶级分类
 * ancestors 存"所有层级父节点"（如 0,200,205），用于一次性查询整棵子树，不用递归
 *
 */
@Data
public class MdItemType implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 产品物料类型ID */
    @NotNull(groups = UpdateOption.class, message = "物料类型ID不能为空")
    @JsonPropertyDescription("分类的Id")
    private Long itemTypeId;

    /** 产品物料类型编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "物料类型编码不能为空")
    @Length(max = 64, message = "物料类型编码长度不能超过 64 个字符")
    private String itemTypeCode;

    /** 产品物料类型名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "物料类型名称不能为空")
    @Length(max = 255, message = "物料类型名称长度不能超过 255 个字符")
    private String itemTypeName;

    /** 父类型ID（0=顶级，前端可不传，后端按 0 处理） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "父类型ID不能为空")
    private Long parentTypeId;

    /** 所有层级父节点（如 0,200,205）。建议由后端根据 parentTypeId 自动拼接，前端不必传 */
    @Length(max = 255, message = "祖级路径长度不能超过 255 个字符")
    private String ancestors;

    /** 产品物料标识（ITEM=物料 PRODUCT=产品，与 md_item 保持一致，分类不能混挂） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "产品物料标识不能为空")
    @Length(max = 20, message = "产品物料标识长度不能超过 20 个字符")
    private String itemOrProduct;

    /** 排列顺序 */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "排列顺序不能为空")
    private Integer orderNum;

    /** 是否启用（Y是 N否） */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "是否启用不能为空")
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

    /** 标记当前分类是否有子分类 */
    private Boolean hasChildren;

    /** 子分类列表（非数据库字段，用于前端渲染分类树） */
    private List<MdItemType> children = new ArrayList<>();

}
