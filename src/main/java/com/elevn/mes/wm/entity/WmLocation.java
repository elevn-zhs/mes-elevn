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
import java.util.List;

/**
 * 库区 / 库位 实体类
 * 对应表：wm_location
 *
 * 【一张表装两级，靠 location_type 区分】
 *   AREA     库区 —— 父级，parent_id = 0，挂在仓库下，本身不放货
 *   LOCATION 库位 —— 子级，parent_id 指向所属库区，货真正放在这里
 *
 * 【易错点提醒】
 *   旧版数据库里 wm_storage_location 是"库区"、wm_storage_area 是"库位"，
 *   命名跟直觉正好相反，迁移时极易搞混。本版已纠正为 AREA(大) / LOCATION(小)。
 *
 * 【编码唯一性口径】
 *   唯一键是 (location_type, location_code) —— 库区和库位的编码各自独立，
 *   一个叫 A01 的库区和一个叫 A01 的库位可以共存。
 *
 */
@Data
public class WmLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 库区/库位ID */
    @NotNull(groups = UpdateOption.class, message = "库区库位ID不能为空")
    private Long locationId;

    /** 父级ID（库区为 0；库位为所属库区的 locationId） */
    private Long parentId;

    /**
     * 类型 AREA-库区 LOCATION-库位
     * 只在新增时必填 —— 修改时不允许改（Service 会拦），库区改成库位整棵树就乱了
     */
    @NotBlank(groups = CreateOption.class, message = "类型不能为空")
    @Pattern(regexp = "^(AREA|LOCATION)$", message = "类型只能是 AREA(库区) 或 LOCATION(库位)")
    private String locationType;

    /** 库区/库位编码 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "库区库位编码不能为空")
    @Length(max = 64, message = "编码长度不能超过 64 个字符")
    private String locationCode;

    /** 库区/库位名称 */
    @NotBlank(groups = {CreateOption.class, UpdateOption.class}, message = "库区库位名称不能为空")
    @Length(max = 255, message = "名称长度不能超过 255 个字符")
    private String locationName;

    /** 所属仓库ID */
    @NotNull(groups = {CreateOption.class, UpdateOption.class}, message = "所属仓库不能为空")
    private Long warehouseId;

    /** 所属仓库编码（冗余，Service 现查回填） */
    private String warehouseCode;

    /** 所属仓库名称（冗余，Service 现查回填） */
    private String warehouseName;

    /** 面积（平方米） */
    private BigDecimal area;

    /** 最大载重量 */
    private BigDecimal maxLoa;

    /** 位置X坐标 */
    private Integer positionX;

    /** 位置Y坐标 */
    private Integer positionY;

    /** 位置Z坐标 */
    private Integer positionZ;

    /** 是否开启库位管理（库区可用，Y是 N否） */
    @Pattern(regexp = "^[YN]$", message = "是否开启库位管理只能是 Y 或 N")
    private String areaFlag;

    /** 是否启用（Y是 N否） */
    @Pattern(regexp = "^[YN]$", message = "是否启用只能是 Y 或 N")
    private String enableFlag;

    /** 是否冻结（冻结的库位不允许入库，Y是 N否） */
    @Pattern(regexp = "^[YN]$", message = "是否冻结只能是 Y 或 N")
    private String frozenFlag;

    /** 是否允许产品混放（Y是 N否） */
    @Pattern(regexp = "^[YN]$", message = "是否允许产品混放只能是 Y 或 N")
    private String productMixing;

    /** 是否允许批次混放（Y是 N否） */
    @Pattern(regexp = "^[YN]$", message = "是否允许批次混放只能是 Y 或 N")
    private String batchMixing;

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

    // ==================== 以下为非表字段 ====================

    /** 子节点（库区下面挂的库位），树形展示用 */
    private List<WmLocation> children;

    /** 该库位当前存放的物料与批次（仅详情接口返回） */
    private List<WmMaterialStock> stockList;
}
