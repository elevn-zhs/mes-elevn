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
 * 工序 实体类
 * 对应表：pro_process
 *
 * 工序是工艺管理的基础字典：先有工序，才能组成工艺路线（pro_route_process），
 * 才能被工作站挂接（md_workstation.process_id）。
 * 一个工序下面可以挂多条「工序内容」（pro_process_content），内容才是教学生产时怎么干活的。
 *
 */
@Data
public class ProProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工序ID */
    @NotNull(groups = UpdateOption.class, message = "工序ID不能为空")
    private Long processId;

    /** 工序编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "工序编码不能为空")
    @Length(max = 64, message = "工序编码长度不能超过 64 个字符")
    private String processCode;

    /** 工序名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "工序名称不能为空")
    @Length(max = 255, message = "工序名称长度不能超过 255 个字符")
    private String processName;

    /** 工艺要求 / 注意事项 */
    @Length(max = 1000, message = "工艺要求长度不能超过 1000 个字符")
    private String attention;

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

    /** 工序内容列表（非表字段，详情页一次性带出，@TableField(exist=false) 的手写 MyBatis 等价：resultMap 不映射即可） */
    private java.util.List<ProProcessContent> contentList;
}
