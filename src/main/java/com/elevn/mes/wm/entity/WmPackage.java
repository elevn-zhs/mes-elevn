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
 * 装箱单 实体类
 * 对应表：wm_package
 *
 * 【装箱单是一棵树 —— 箱套箱】
 *   大箱里套小箱（一个托盘箱装 4 个产品箱），靠 parent_id + ancestors 表达：
 *   - parent_id：直接父箱的 ID，顶层箱为 0
 *   - ancestors：从根到直接父箱的完整 ID 链，格式 "0,1001,1003,"
 *     （注意结尾带逗号，配合 like '0,1001,%' 或 find_in_set 查整棵子树）
 *   新增子箱时 ancestors = 父箱.ancestors + 父箱ID + ","，
 *   这和 md_dept / wm_location 的自关联树是同一套套路。
 *
 * 【装箱不动库存】
 *   箱子里装的还是库位上那些货，只是给它们套了一层"包装"的层级，
 *   所以本表任何操作都不碰 wm_material_stock。真正扣库存的是出库单过账。
 *   明细行上冗余的仓库/库位/批次信息，是从库存行拷贝的"快照"，
 *   记录"装箱那一刻货在哪"，后来货被调拨走了快照也不跟着变。
 *
 * 【箱子编号生成后不允许改】
 *   package_code 会写进条码（barcode_content）、贴在箱子上对外流转，
 *   改了码，贴出去的标签就成了废纸。
 *
 */
@Data
public class WmPackage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 装箱单ID */
    @NotNull(groups = UpdateOption.class, message = "装箱单ID不能为空")
    private Long packageId;

    /** 父箱ID（顶层箱为 0） */
    private Long parentId;

    /**
     * 祖链：从根到直接父箱的 ID 链，格式 "0,1001,1003,"（结尾带逗号）
     * 由后端按父箱维护，前端不传、传了也忽略
     */
    private String ancestors;

    /**
     * 装箱单编号
     * 由后端按「ZX + 日期 + 序列」生成，前端不用传，也不允许改
     */
    private String packageCode;

    /** 条码ID（条码模块生成后回填，S3 条码接口未调前为空） */
    private Long barcodeId;

    /** 条码内容（与 package_code 一致，冗余一份方便扫码反查） */
    private String barcodeContent;

    /** 条码图片地址 */
    private String barcodeUrl;

    /** 装箱日期 */
    @NotNull(groups = CreateOption.class, message = "装箱日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime packageDate;

    /** 销售订单编号 */
    @Length(max = 64, message = "销售订单编号长度不能超过 64 个字符")
    private String soCode;

    /** 发票编号 */
    @Length(max = 255, message = "发票编号长度不能超过 255 个字符")
    private String invoiceCode;

    /** 客户ID */
    private Long clientId;

    /** 客户编码 */
    private String clientCode;

    /** 客户名称 */
    private String clientName;

    /** 客户简称 */
    private String clientNick;

    /** 箱长 */
    private BigDecimal packageLength;

    /** 箱宽 */
    private BigDecimal packageWidth;

    /** 箱高 */
    private BigDecimal packageHeight;

    /** 尺寸单位 */
    private String sizeUnit;

    /** 净重 */
    private BigDecimal netWeight;

    /** 毛重 */
    private BigDecimal grossWeight;

    /** 重量单位 */
    private String weightUnit;

    /** 检查员用户名 */
    private String inspector;

    /** 检查员名称 */
    private String inspectorName;

    /** 状态 PREPARE装箱中 / PACKED 已完成 */
    @Pattern(regexp = "^(PREPARE|PACKED)$", message = "状态只能是 PREPARE / PACKED")
    private String status;

    /** 是否生效 */
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

    // ==================== 以下为非表字段 ====================

    /** 装箱日期区间查询 - 起（仅查询条件用） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime packageDateStart;

    /** 装箱日期区间查询 - 止（仅查询条件用） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime packageDateEnd;

    /** 父箱编号（join 出来给前端显示层级） */
    private String parentCode;

    /** 状态中文名（字典翻译，列表页显示用） */
    private String statusName;

    /** 箱内明细行（详情接口返回） */
    private List<WmPackageLine> lineList;

    /** 子箱列表（详情接口返回，顶层箱才可能有） */
    private List<WmPackage> children;

    /** 箱内物料种类数（列表页小统计） */
    private Integer itemCount;

    /** 箱内总数量（列表页小统计） */
    private BigDecimal totalQuantity;
}
