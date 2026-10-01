package com.elevn.mes.wm.entity;

import com.elevn.mes.wm.options.CreateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 条码 实体类
 * 对应表：wm_barcode
 *
 * 【一行 = 一个真实存在的码】
 *   条码是"规则 + 业务对象"生成的实例：类型 + 内容 + 指向谁（biz_*）。
 *   解析（扫码查库）就靠 barcode_content 精确匹配这一行 ——
 *   所以内容必须唯一（内容一样的码贴到两个东西上，扫码就分不清了）。
 *
 */
@Data
public class WmBarcode implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 条码ID */
    private Long barcodeId;

    /** 码制 CODE128 / QR_CODE */
    private String barcodeFormat;

    /** 条码类型 ITEM / BATCH / PACKAGE */
    @NotBlank(groups = CreateOption.class, message = "条码类型不能为空")
    @Pattern(regexp = "^(ITEM|BATCH|PACKAGE)$", message = "条码类型只能是 ITEM / BATCH / PACKAGE")
    private String barcodeType;

    /** 条码内容（唯一；扫码解析的主键） */
    @NotBlank(groups = CreateOption.class, message = "条码内容不能为空")
    private String barcodeContent;

    /** 业务对象ID（物料ID / 批次ID / 装箱ID，跟 barcode_type 走） */
    @NotNull(groups = CreateOption.class, message = "业务对象不能为空")
    private Long bizId;

    /** 业务对象编码 */
    private String bizCode;

    /** 业务对象名称 */
    private String bizName;

    /** 条码图片地址（相对 uploadPath 的子路径） */
    private String barcodeUrl;

    /** 是否启用 */
    private String enableFlag;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    private String remark;

    // ==================== 以下为非表字段 ====================

    /** 查询用：条码内容模糊 */
    private String contentKeyword;
}
