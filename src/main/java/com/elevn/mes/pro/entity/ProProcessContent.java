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
import java.time.LocalDateTime;

/**
 * 工序内容 实体类
 * 对应表：pro_process_content
 *
 * 工序内容 = 某个工序下的具体作业步骤，按 order_num 排序展示。
 * 一个工序（pro_process）可以挂多条内容，是典型的一对多主子表。
 * content_text 是干什么的，device 是用什么辅助设备，material 是用什么辅助材料，
 * doc_url 可以挂一张说明图或文档链接。
 *
 */
@Data
public class ProProcessContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 内容ID */
    @NotNull(groups = UpdateOption.class, message = "内容ID不能为空")
    private Long contentId;

    /** 工序ID（关联 pro_process.process_id） */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "工序ID不能为空")
    private Long processId;

    /** 顺序号 */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "顺序号不能为空")
    private Integer orderNum;

    /** 内容说明 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "内容说明不能为空")
    @Length(max = 500, message = "内容说明长度不能超过 500 个字符")
    private String contentText;

    /** 辅助设备 */
    @Length(max = 255, message = "辅助设备长度不能超过 255 个字符")
    private String device;

    /** 辅助材料 */
    @Length(max = 255, message = "辅助材料长度不能超过 255 个字符")
    private String material;

    /** 说明图/文档地址 */
    @Length(max = 255, message = "文档地址长度不能超过 255 个字符")
    private String docUrl;

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
