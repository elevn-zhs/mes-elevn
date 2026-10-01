package com.elevn.mes.md.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 人力资源表 实体类
 * 对应表：md_workstation_worker
 *
 * 描述"这个工位定编需要什么岗位、几个人"，是排班和工时核算的依据
 * 岗位主数据在 sys_post，这里只冗余 code / name
 * 注意：这是"岗位编制"，不是"具体哪个人"；谁上岗看排班表（tm 模块）
 *
 */
@Data
public class MdWorkstationWorker implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @NotNull(groups = UpdateOption.class, message = "记录ID不能为空")
    private Long recordId;

    /** 工作站ID（关联 md_workstation.workstation_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工作站ID不能为空")
    private Long workstationId;

    /** 岗位ID（关联 sys_post.post_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "岗位ID不能为空")
    private Long postId;

    /** 岗位编码（冗余字段） */
    @Length(max = 64, message = "岗位编码长度不能超过 64 个字符")
    private String postCode;

    /** 岗位名称（冗余字段） */
    @Length(max = 255, message = "岗位名称长度不能超过 255 个字符")
    private String postName;

    /** 数量（该岗位定编人数，至少 1） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于 1")
    private Integer quantity;

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
