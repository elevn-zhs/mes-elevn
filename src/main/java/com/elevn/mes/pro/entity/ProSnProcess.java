package com.elevn.mes.pro.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SN 过站记录 实体类
 * 对应表：pro_sn_process（A 线生产执行域的表）
 *
 * 【这张表是 A 线写的，B 线只读】
 *   产品每过一道工序，A 线报工时在这里落一条"哪台机器、哪道工序、
 *   什么时候进什么时候出、多少合格多少不良"。
 *   B 线的 SN 追溯页把它按 seq_num 排出来，就是单件的"出生档案"。
 *   当前 A 线还没有接 SN 过站，表是空的 —— 接口先做好，两边数据一通就能看。
 *
 */
@Data
public class ProSnProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** SN ID */
    private Long snId;

    /** SN 码 */
    private String snCode;

    /** 序号（第几道，从 1 开始，追溯按它排序） */
    private Integer seqNum;

    /** 工序ID */
    private Long processId;

    /** 工序编码 */
    private String processCode;

    /** 工序名称 */
    private String processName;

    /** 进站时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inputTime;

    /** 出站时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outputTime;

    /** 进站数量 */
    private BigDecimal quantityInput;

    /** 出站数量 */
    private BigDecimal quantityOutput;

    /** 不合格数量 */
    private BigDecimal quantityUnqualified;

    /** 工作站ID */
    private Long workstationId;

    /** 工作站编码 */
    private String workstationCode;

    /** 工作站名称 */
    private String workstationName;

    /** IPQC 检验单ID（软引用 C 线） */
    private Long ipqcId;

    /** 操作工用户名 */
    private String userId;

    /** 操作工名称 */
    private String userName;

    /** 操作工昵称 */
    private String nickName;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    private String remark;
}
