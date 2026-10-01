package com.elevn.mes.wm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓库 实体类
 * 对应表：wm_warehouse
 *
 * 仓库是仓储模块的根节点：仓库下面挂库区（wm_location.location_type='AREA'），
 * 库区下面再挂库位（LOCATION），货最终放在库位上。
 *
 * 【为什么编码不可改】
 * 仓库编码会冗余写进 wm_location、wm_material_stock、wm_doc 等一堆表里，
 * 让用户改编码就得批量刷这些冗余列，改漏一处账就对不上。
 * 所以编码一旦建好就锁死，要换编码就新建仓库。
 *
 */
@Data
public class WmWarehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 仓库ID */
    @NotNull(groups = UpdateOption.class, message = "仓库ID不能为空")
    private Long warehouseId;

    /**
     * 仓库编码
     * 只在新增时必填 —— 修改时不允许改（Service 会拦），所以不标 UpdateOption
     */
    @NotBlank(groups = CreateOption.class, message = "仓库编码不能为空")
    @Length(max = 64, message = "仓库编码长度不能超过 64 个字符")
    private String warehouseCode;

    /** 仓库名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "仓库名称不能为空")
    @Length(max = 255, message = "仓库名称长度不能超过 255 个字符")
    private String warehouseName;

    /** 位置（文字描述，如"一号厂房东侧"） */
    @Length(max = 500, message = "位置长度不能超过 500 个字符")
    private String location;

    /** 面积（平方米） */
    private BigDecimal area;

    /** 仓管员用户ID */
    private Long userId;

    /** 仓管员用户名 */
    @Length(max = 64, message = "仓管员用户名长度不能超过 64 个字符")
    private String userName;

    /** 仓管名称（显示用的中文名） */
    @Length(max = 64, message = "仓管名称长度不能超过 64 个字符")
    private String charge;

    /** 主管用户ID */
    private Long managerId;

    /** 主管用户名 */
    @Length(max = 64, message = "主管用户名长度不能超过 64 个字符")
    private String managerName;

    /** 主管名称（显示用的中文名） */
    @Length(max = 64, message = "主管名称长度不能超过 64 个字符")
    private String managerNick;

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

    // ==================== 以下为非表字段（详情页统计用） ====================

    /** 库区数量（wm_location 里 area 类型且属本仓库的行数） */
    private Integer areaCount;

    /** 库位数量（LOCATION 类型且属本仓库的行数） */
    private Integer locationCount;

    /** 库存总量（本仓库所有物料在库数量合计） */
    private BigDecimal stockQuantity;

    /** 库存物料种数 */
    private Integer stockItemCount;
}
