package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户表 实体类
 * 对应表：md_client
 *
 * 销售出库的收货方，client_code 会被销售订单、发货单、出库单引用
 * client_type 默认 ENTERPRISE（企业客户），其他取值由字典维护，如 INDIVIDUAL / GOVERNMENT
 *
 */
@Data
public class MdClient implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 客户ID */
    @NotNull(groups = UpdateOption.class, message = "客户ID不能为空")
    private Long clientId;

    /** 客户编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "客户编码不能为空")
    @Length(max = 64, message = "客户编码长度不能超过 64 个字符")
    private String clientCode;

    /** 客户名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "客户名称不能为空")
    @Length(max = 255, message = "客户名称长度不能超过 255 个字符")
    private String clientName;

    /** 客户简称 */
    @Length(max = 255, message = "客户简称长度不能超过 255 个字符")
    private String clientNick;

    /** 客户英文名称 */
    @Length(max = 255, message = "客户英文名称长度不能超过 255 个字符")
    private String clientEn;

    /** 客户简介 */
    @Length(max = 500, message = "客户简介长度不能超过 500 个字符")
    private String clientDes;

    /** 客户LOGO地址 */
    @Length(max = 255, message = "客户LOGO地址长度不能超过 255 个字符")
    private String clientLogo;

    /** 客户类型（ENTERPRISE=企业 INDIVIDUAL=个人，默认 ENTERPRISE） */
    @Length(max = 64, message = "客户类型长度不能超过 64 个字符")
    private String clientType;

    /** 客户地址 */
    @Length(max = 500, message = "客户地址长度不能超过 500 个字符")
    private String address;

    /** 客户官网地址 */
    @Length(max = 255, message = "客户官网地址长度不能超过 255 个字符")
    private String website;

    /** 客户邮箱地址 */
    @Email(message = "客户邮箱格式不正确")
    @Length(max = 255, message = "客户邮箱地址长度不能超过 255 个字符")
    private String email;

    /** 客户电话 */
    @Length(max = 64, message = "客户电话长度不能超过 64 个字符")
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
