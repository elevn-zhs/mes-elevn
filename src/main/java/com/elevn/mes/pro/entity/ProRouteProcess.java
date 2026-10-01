package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工艺路线工序明细 实体类
 * 对应表：pro_route_process
 *
 * 路线工序明细 = 路线里的第 N 步是什么工序。
 * 保存走「整批替换」：前端把明细表编辑完一次性提交，后端校验通过后
 * 逻辑删除旧明细、插入新明细，保证排序和 next_process 永远是完整一致的链。
 *
 * 冗余字段说明（process_code / process_name / next_process_code / next_process_name）：
 * 一律由后端从工序表查出来回填，不信任前端传值 —— 免得前端改了名、这里留着旧名。
 *
 */
@Data
public class ProRouteProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long recordId;

    /** 路线ID（关联 pro_route.route_id） */
    private Long routeId;

    /** 工序ID（关联 pro_process.process_id） */
    private Long processId;

    /** 工序编码（冗余，后端回填） */
    @Length(max = 64, message = "工序编码长度不能超过 64 个字符")
    private String processCode;

    /** 工序名称（冗余，后端回填） */
    @Length(max = 255, message = "工序名称长度不能超过 255 个字符")
    private String processName;

    /** 顺序号（从 1 开始连续） */
    private Integer orderNum;

    /** 下一道工序ID（末工序为 0；关联本路线内的其他工序） */
    private Long nextProcessId;

    /** 下一道工序编码（冗余，后端回填） */
    @Length(max = 64, message = "下一道工序编码长度不能超过 64 个字符")
    private String nextProcessCode;

    /** 下一道工序名称（冗余，后端回填） */
    @Length(max = 255, message = "下一道工序名称长度不能超过 255 个字符")
    private String nextProcessName;

    /** 工序衔接方式（如：正常流转 / 返工回流，字典配置） */
    @Length(max = 64, message = "衔接方式长度不能超过 64 个字符")
    private String linkType;

    /** 默认准备时间（分钟） */
    private Integer defaultPreTime;

    /** 默认等待时间（分钟） */
    private Integer defaultSufTime;

    /**
     * 单件工时（分钟）—— 排产算时长用
     *
     * 为什么放在这里而不是 pro_process：
     *   同一种工序在不同路线上的工时可能不同（设备不同、批量不同），
     *   工时是"工序在本条路线上的属性"，放关联表语义才准。
     * 与 capacity_per_hour 互为倒数：capacity = 60 / unitTime。
     */
    private BigDecimal unitTime;

    /** 小时产能（件/小时）—— 与 unitTime 互为倒数，冗余存一份方便直接读 */
    private BigDecimal capacityPerHour;

    /** 甘特图颜色（#RRGGBB） */
    @Length(max = 7, message = "颜色值长度只能为 7 个字符（#RRGGBB）")
    private String colorCode;

    /** 是否关键工序（KEY 普通 / KEY_PROCESS 关键，字典配置） */
    @Length(max = 64, message = "关键工序标识长度不能超过 64 个字符")
    private String keyFlag;

    /** 是否检验工序（Y是 N否，决定报工后是否推质检） */
    @Length(max = 1, message = "是否检验工序只能为 1 个字符")
    private String isCheck;

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
