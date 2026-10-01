package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工艺路线 实体类
 * 对应表：pro_route
 *
 * 工艺路线 = 一串按顺序执行的工序。路线本身只有编码/名称等主信息，
 * 真正的工序顺序在子表 pro_route_process 里。
 * 产品制程（pro_route_product）把路线挂到具体产品上，工单下达时按它拆工序任务。
 *
 */
@Data
public class ProRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 路线ID */
    @NotNull(groups = UpdateOption.class, message = "路线ID不能为空")
    private Long routeId;

    /** 路线编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "路线编码不能为空")
    @Length(max = 64, message = "路线编码长度不能超过 64 个字符")
    private String routeCode;

    /** 路线名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "路线名称不能为空")
    @Length(max = 255, message = "路线名称长度不能超过 255 个字符")
    private String routeName;

    /** 路线描述 */
    @Length(max = 500, message = "路线描述长度不能超过 500 个字符")
    private String routeDesc;

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
