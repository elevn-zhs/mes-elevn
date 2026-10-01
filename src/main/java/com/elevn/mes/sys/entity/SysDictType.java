package com.elevn.mes.sys.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.sys.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型表 实体类
 * 对应表：sys_dict_type
 *
 * dictType 是字典的"英文名"，也是前后端引用的 key，
 * 例如 sys_user_sex 下挂着 男/女/未知 三个条目。
 *
 */
@Data
public class SysDictType implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字典主键 */
    @NotNull(groups = UpdateOption.class, message = "根据id修改操作中，dictId不能为空")
    private Long dictId;

    /** 字典名称 */
    @NotBlank(message = "字典名称不能为空")
    @Length(min = 3,max = 20,message = "字典名称长度在3~20个字符之间")
    private String dictName;

    /** 字典类型（英文key） */
    @NotBlank(message = "字典类型不能为空")
    @Length(min = 3,max = 50,message = "字典名称长度在3~50个字符之间")
    private String dictType;

    /** 显示顺序 */
    private Integer dictSort;

    /** 状态（0正常 1停用） */
    private String status;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

}
