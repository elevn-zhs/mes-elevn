package com.elevn.mes.sys.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表 实体类
 * 对应表：sys_log
 *
 * log_type：1=操作日志  2=登录日志  3=异常日志  4=接口调用日志
 * 一张表承载四类日志，靠 logType 区分，避免为登录再开一张几乎一样的表。
 *
 */
@Data
public class SysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志主键 */
    private Long logId;

    /** 日志类型（1操作 2登录 3异常 4接口） */
    private String logType;

    /** 操作模块/日志标题 */
    private String title;

    /** 业务类型（0其他 1新增 2修改 3删除 4导出 5导入 6授权 7强退） */
    private String businessType;

    /** 操作人员账号 */
    private String operName;

    /** 操作人员姓名 */
    private String operNick;

    /** 所属部门（快照，部门改名不影响历史） */
    private String deptName;

    /** 请求方法（类.方法()） */
    private String method;

    /** HTTP 请求方式 */
    private String requestMethod;

    /** 请求地址 */
    private String operUrl;

    /** 操作IP */
    private String operIp;

    /** 操作地点 */
    private String operLocation;

    /** 请求参数 */
    private String operParam;

    /** 返回结果 */
    private String jsonResult;

    /** 操作状态（0正常 1异常） */
    private String status;

    /** 异常信息 */
    private String errorMsg;

    /** 操作类别（1后台用户 2手机端 3其他） */
    private String operatorType;

    /** User-Agent */
    private String userAgent;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 操作时间 */
    private LocalDateTime operTime;

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
