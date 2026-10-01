package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 供应商表 实体类
 * 对应表：md_vendor
 *
 * 采购入库的收货方来源，vendor_code 会被采购订单、来料检验、入库单引用
 * 联系人保留两组（contact1 / contact2），够用且不引入子表
 *
 */
@Data
public class MdVendor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 供应商ID */
    @NotNull(groups = UpdateOption.class, message = "供应商ID不能为空")
    private Long vendorId;

    /** 供应商编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "供应商编码不能为空")
    @Length(max = 64, message = "供应商编码长度不能超过 64 个字符")
    private String vendorCode;

    /** 供应商名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "供应商名称不能为空")
    @Length(max = 255, message = "供应商名称长度不能超过 255 个字符")
    private String vendorName;

    /** 供应商简称 */
    @Length(max = 255, message = "供应商简称长度不能超过 255 个字符")
    private String vendorNick;

    /** 供应商英文名称 */
    @Length(max = 255, message = "供应商英文名称长度不能超过 255 个字符")
    private String vendorEn;

    /** 供应商简介 */
    @Length(max = 500, message = "供应商简介长度不能超过 500 个字符")
    private String vendorDes;

    /** 供应商LOGO地址 */
    @Length(max = 255, message = "供应商LOGO地址长度不能超过 255 个字符")
    private String vendorLogo;

    /** 供应商等级（如 A/B/C，或字典 sys_dict_data 中的供应商等级） */
    @Length(max = 64, message = "供应商等级长度不能超过 64 个字符")
    private String vendorLevel;

    /** 供应商评分（0~100，用于供应商考核） */
    @Min(value = 0, message = "供应商评分不能小于 0")
    @Max(value = 100, message = "供应商评分不能大于 100")
    private Integer vendorScore;

    /** 供应商地址 */
    @Length(max = 500, message = "供应商地址长度不能超过 500 个字符")
    private String address;

    /** 供应商官网地址 */
    @Length(max = 255, message = "供应商官网地址长度不能超过 255 个字符")
    private String website;

    /** 供应商邮箱地址 */
    @Email(message = "供应商邮箱格式不正确")
    @Length(max = 255, message = "供应商邮箱地址长度不能超过 255 个字符")
    private String email;

    /** 供应商电话 */
    @Length(max = 64, message = "供应商电话长度不能超过 64 个字符")
    private String tel;

    /** 联系人1 */
    @Length(max = 64, message = "联系人1长度不能超过 64 个字符")
    private String contact1;

    /** 联系人1-电话 */
    @Length(max = 64, message = "联系人1-电话长度不能超过 64 个字符")
    private String contact1Tel;

    /** 联系人1-邮箱 */
    @Email(message = "联系人1-邮箱格式不正确")
    @Length(max = 255, message = "联系人1-邮箱长度不能超过 255 个字符")
    private String contact1Email;

    /** 联系人2 */
    @Length(max = 64, message = "联系人2长度不能超过 64 个字符")
    private String contact2;

    /** 联系人2-电话 */
    @Length(max = 64, message = "联系人2-电话长度不能超过 64 个字符")
    private String contact2Tel;

    /** 联系人2-邮箱 */
    @Email(message = "联系人2-邮箱格式不正确")
    @Length(max = 255, message = "联系人2-邮箱长度不能超过 255 个字符")
    private String contact2Email;

    /** 统一社会信用代码 */
    @Length(max = 64, message = "统一社会信用代码长度不能超过 64 个字符")
    private String creditCode;

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

}
