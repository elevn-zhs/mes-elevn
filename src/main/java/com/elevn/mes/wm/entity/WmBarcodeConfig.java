package com.elevn.mes.wm.entity;

import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 条码规则配置 实体类
 * 对应表：wm_barcode_config
 *
 * 【一条规则管一种码】
 *   barcode_type 决定这张码贴在哪类东西上（物料/批次/箱），
 *   content_format 是内容模板：{itemCode} {batchCode} {packageCode} 是占位符，
 *   生成时后端把占位符换成真实值。例：模板 IT-{itemCode}，物料 A100 → 条码 IT-FG-CTRL-A100。
 *   模板是"规则的灵魂"：改模板不用改代码，这是配置化最省的地方。
 *
 */
@Data
public class WmBarcodeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long configId;

    /** 码制（如 CODE128 / QR_CODE） */
    @NotBlank(groups = CreateOption.class, message = "码制不能为空")
    @Pattern(regexp = "^(CODE128|QR_CODE)$", message = "码制只能是 CODE128 / QR_CODE")
    private String barcodeFormat;

    /** 条码类型 ITEM物料码 / BATCH批次码 / PACKAGE箱码（每类只允许一条启用规则） */
    @NotBlank(groups = CreateOption.class, message = "条码类型不能为空")
    @Pattern(regexp = "^(ITEM|BATCH|PACKAGE)$", message = "条码类型只能是 ITEM / BATCH / PACKAGE")
    private String barcodeType;

    /** 内容模板，支持占位符 {itemCode} {batchCode} {packageCode} */
    @NotBlank(groups = CreateOption.class, message = "内容模板不能为空")
    private String contentFormat;

    /** 内容示例（给录入的人看的，不参与逻辑） */
    private String contentExample;

    /** 是否自动生成（Y=业务单据保存后自动生成对应条码；本版本先手动批量生成） */
    private String autoGenFlag;

    /** 默认打印模板名 */
    private String defaultTemplate;

    /** 是否启用 */
    private String enableFlag;

    /** 删除标志（0存在 1删除） */
    private String delFlag;

    /** 备注 */
    private String remark;
}
