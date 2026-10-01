package com.elevn.mes.sys.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据表 实体类
 * 对应表：sys_dict_data
 *
 * dictValue 存实际入库的值（如 '0'），dictLabel 存页面展示的值（如 '正常'）
 * listClass 用于前端回显样式：primary / success / info / warning / danger
 *
 */
@Data
public class SysDictData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字典条目主键 */
    private Long dictCode;

    /** 所属字典类型 */
    private String dictType;

    /** 字典标签（显示值） */
    private String dictLabel;

    /** 字典键值（存储值） */
    private String dictValue;

    /** 显示顺序 */
    private Integer dictSort;

    /** 是否默认（Y是 N否） */
    private String isDefault;

    /** 回显样式（primary/success/info/warning/danger） */
    private String listClass;

    /** 自定义样式类 */
    private String cssClass;

    /** 状态（0正常 1停用） */
    private String status;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

}
