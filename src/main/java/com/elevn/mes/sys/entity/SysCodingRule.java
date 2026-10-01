package com.elevn.mes.sys.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysCodingRule {
    private Long ruleId;
    private String ruleTitle;
    private String ruleCode;
    private String ruleDesc;
    private String prefix;
    private Integer dateStr;
    private Long serialNumber;
    private Integer numberLength;
    private LocalDateTime createTime;
    private String createBy;
    private LocalDateTime updateTime;
    private String updateBy;
    private Integer status;
    private Integer delFlag;
}
