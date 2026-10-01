package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.wm.entity.WmBarcode;
import com.elevn.mes.wm.entity.WmBarcodeConfig;
import com.elevn.mes.wm.entity.WmBatch;
import com.elevn.mes.wm.entity.WmPackage;
import com.elevn.mes.wm.mapper.WmBarcodeConfigMapper;
import com.elevn.mes.wm.mapper.WmBarcodeMapper;
import com.elevn.mes.wm.mapper.WmBatchMapper;
import com.elevn.mes.wm.mapper.WmPackageMapper;
import com.elevn.mes.wm.service.WmBarcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 条码 Service 实现
 *
 * 【生成条码的三步】
 *   1. 找规则：按类型取"启用中"的那条规则（一类一规则，配置页已保证）
 *   2. 拼内容：把模板里的 {itemCode} 等占位符换成业务对象的真实值；
 *      内容必须在 wm_barcode 里唯一 —— 码贴出去就代表一个东西，重号就乱套
 *   3. 画图：zxing 把内容编码成黑白点阵（BitMatrix），自己转 BufferedImage 存 PNG。
 *      存到 uploadPath/barcode/ 下，图片服务器(9898)直接就能按 URL 访问
 *
 * 【为什么解析是"查表"而不是"解格式"】
 *   扫码枪读回来就是一串字符。理论上可以按前缀反推类型，
 *   但模板是用户配置的，前缀会变；拿内容去 wm_barcode 精确查一行，
 *   查到就知道类型和指向 —— 规则怎么改都不影响解析，这才叫配置驱动。
 *
 */
@Service
public class WmBarcodeServiceImpl implements WmBarcodeService {

    @Autowired
    private WmBarcodeConfigMapper configMapper;

    @Autowired
    private WmBarcodeMapper barcodeMapper;

    @Autowired
    private MdItemMapper itemMapper;

    @Autowired
    private WmBatchMapper batchMapper;

    @Autowired
    private WmPackageMapper packageMapper;

    /** 上传根目录（application.yaml 的 uploadPath） */
    @Value("${uploadPath}")
    private String uploadPath;

    /** 图片访问基础地址（application.yaml 的 imageServerBaseUrl） */
    @Value("${imageServerBaseUrl}")
    private String imageServerBaseUrl;

    private static final String TYPE_ITEM = "ITEM";
    private static final String TYPE_BATCH = "BATCH";
    private static final String TYPE_PACKAGE = "PACKAGE";

    /** 条码图片存的上传子目录 */
    private static final String IMAGE_SUB_DIR = "barcode";

    // ==================== 规则配置 ====================

    @Override
    public PageInfo<WmBarcodeConfig> pageConfig(int pageNum, int pageSize, WmBarcodeConfig query) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(configMapper.selectByCondition(query));
    }

    @Override
    public WmBarcodeConfig getConfigById(Long configId) {
        WmBarcodeConfig config = configMapper.selectById(configId);
        if (config == null) {
            throw new BusinessException("条码规则不存在或已被删除");
        }
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmBarcodeConfig createConfig(WmBarcodeConfig config) {
        checkTemplate(config.getContentFormat());
        if ("Y".equals(config.getEnableFlag())
                && configMapper.countEnabledByType(config.getBarcodeType(), null) > 0) {
            throw new BusinessException("该条码类型已有一条启用中的规则，一个类型只能启用一条"
                    + "（先把旧的停用再启用新的）");
        }
        configMapper.insert(config);
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(WmBarcodeConfig config) {
        WmBarcodeConfig db = configMapper.selectById(config.getConfigId());
        if (db == null) {
            throw new BusinessException("条码规则不存在或已被删除");
        }
        checkTemplate(config.getContentFormat());
        if ("Y".equals(config.getEnableFlag())
                && configMapper.countEnabledByType(config.getBarcodeType(), config.getConfigId()) > 0) {
            throw new BusinessException("该条码类型已有一条启用中的规则，一个类型只能启用一条");
        }
        configMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long configId) {
        if (configMapper.selectById(configId) == null) {
            throw new BusinessException("条码规则不存在或已被删除");
        }
        configMapper.deleteById(configId);
    }

    /** 模板合法性：必须有占位符，且占位符必须在支持清单里（防止拼出一堆没替换的花括号） */
    private void checkTemplate(String contentFormat) {
        if (contentFormat == null || !contentFormat.contains("{")) {
            throw new BusinessException("内容模板必须包含占位符，如 IT-{itemCode}");
        }
        String cleaned = contentFormat
                .replace("{itemCode}", "").replace("{batchCode}", "").replace("{packageCode}", "");
        if (cleaned.contains("{") || cleaned.contains("}")) {
            throw new BusinessException("内容模板里有不支持的占位符，只支持 {itemCode} / {batchCode} / {packageCode}");
        }
    }

    // ==================== 生成 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmBarcode generate(String barcodeType, Long bizId) {
        // ---------- 1. 找启用中的规则 ----------
        WmBarcodeConfig config = configMapper.selectEnabledByType(barcodeType);
        if (config == null) {
            throw new BusinessException("条码类型 " + barcodeType + " 没有启用中的规则，请先到条码规则页配置");
        }

        // ---------- 2. 取业务对象 + 按模板拼内容 ----------
        Map<String, String> tokens = loadBizTokens(barcodeType, bizId);
        String content = config.getContentFormat()
                .replace("{itemCode}", nvl(tokens.get("itemCode")))
                .replace("{batchCode}", nvl(tokens.get("batchCode")))
                .replace("{packageCode}", nvl(tokens.get("packageCode")));
        if (content.contains("{") || content.trim().isEmpty()) {
            throw new BusinessException("条码内容拼不出来：模板需要的值在业务对象上不存在"
                    + "（内容模板 " + config.getContentFormat() + "）");
        }
        if (barcodeMapper.countByContent(content, null) > 0) {
            throw new BusinessException("条码内容「" + content + "」已经生成过，一个内容只对应一个码");
        }

        // ---------- 3. 画图存文件 + 落库 ----------
        String fileName = sanitize(content) + ".png";
        try {
            renderAndSave(content, config.getBarcodeFormat(), new File(uploadPath + IMAGE_SUB_DIR, fileName));
        } catch (Exception e) {
            throw new BusinessException("条码图片生成失败：" + e.getMessage());
        }

        WmBarcode barcode = new WmBarcode();
        barcode.setBarcodeFormat(config.getBarcodeFormat());
        barcode.setBarcodeType(barcodeType);
        barcode.setBarcodeContent(content);
        barcode.setBizId(bizId);
        barcode.setBizCode(bizCodeOf(barcodeType, tokens));
        barcode.setBizName(nvl(tokens.get("bizName")));
        barcode.setBarcodeUrl(imageServerBaseUrl + IMAGE_SUB_DIR + "/" + fileName);
        barcode.setRemark("按规则「" + config.getContentFormat() + "」生成");
        barcodeMapper.insert(barcode);
        return barcode;
    }

    @Override
    public List<WmBarcode> generateBatch(String barcodeType, List<Long> bizIds) {
        List<WmBarcode> created = new ArrayList<>();
        for (Long bizId : bizIds) {
            try {
                created.add(generate(barcodeType, bizId));
            } catch (BusinessException e) {
                // 已生成过的跳过（批量场景幂等），其余错误照抛
                if (e.getMessage() != null && e.getMessage().contains("已经生成过")) {
                    continue;
                }
                throw e;
            }
        }
        if (created.isEmpty()) {
            throw new BusinessException("没有生成任何新条码（所选对象都已生成过）");
        }
        return created;
    }

    /** 取业务对象并备好模板占位符的值 */
    private Map<String, String> loadBizTokens(String barcodeType, Long bizId) {
        Map<String, String> tokens = new HashMap<>();
        switch (barcodeType) {
            case TYPE_ITEM: {
                MdItem item = itemMapper.selectById(bizId);
                if (item == null) {
                    throw new BusinessException("物料不存在，ID=" + bizId);
                }
                tokens.put("itemCode", item.getItemCode());
                tokens.put("bizName", item.getItemName());
                break;
            }
            case TYPE_BATCH: {
                WmBatch batch = batchMapper.selectById(bizId);
                if (batch == null) {
                    throw new BusinessException("批次不存在，ID=" + bizId);
                }
                tokens.put("itemCode", batch.getItemCode());
                tokens.put("batchCode", batch.getBatchCode());
                tokens.put("bizName", batch.getItemName());
                break;
            }
            case TYPE_PACKAGE: {
                WmPackage pkg = packageMapper.selectById(bizId);
                if (pkg == null) {
                    throw new BusinessException("装箱单不存在，ID=" + bizId);
                }
                tokens.put("packageCode", pkg.getPackageCode());
                tokens.put("bizName", pkg.getPackageCode());
                break;
            }
            default:
                throw new BusinessException("未知的条码类型：" + barcodeType);
        }
        return tokens;
    }

    private String bizCodeOf(String barcodeType, Map<String, String> tokens) {
        switch (barcodeType) {
            case TYPE_ITEM:    return nvl(tokens.get("itemCode"));
            case TYPE_BATCH:   return nvl(tokens.get("batchCode"));
            case TYPE_PACKAGE: return nvl(tokens.get("packageCode"));
            default:           return "";
        }
    }

    // ==================== 查询 / 解析 ====================

    @Override
    public PageInfo<WmBarcode> page(int pageNum, int pageSize, WmBarcode query) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(barcodeMapper.selectByCondition(query));
    }

    @Override
    public WmBarcode getById(Long barcodeId) {
        WmBarcode barcode = barcodeMapper.selectById(barcodeId);
        if (barcode == null) {
            throw new BusinessException("条码不存在或已被删除");
        }
        return barcode;
    }

    @Override
    public WmBarcode parse(String barcodeContent) {
        if (barcodeContent == null || barcodeContent.trim().isEmpty()) {
            throw new BusinessException("条码内容不能为空");
        }
        WmBarcode barcode = barcodeMapper.selectByContent(barcodeContent.trim());
        if (barcode == null) {
            throw new BusinessException("扫码内容「" + barcodeContent.trim()
                    + "」在系统里没有登记，可能还没生成条码，或者不是本系统的码");
        }
        return barcode;
    }

    // ==================== 图片渲染 ====================

    /**
     * zxing 编码 + 自己转 PNG：
     * MultiFormatWriter 把内容编码成 BitMatrix（黑白点阵），
     * 点阵逐像素填到 BufferedImage 上（黑=1 白=0），ImageIO 写盘。
     */
    private void renderAndSave(String content, String format, File target) throws Exception {
        int width = 300;
        int height = "QR_CODE".equals(format) ? 300 : 100;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        if ("QR_CODE".equals(format)) {
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        }
        BarcodeFormat zxingFormat = "QR_CODE".equals(format)
                ? BarcodeFormat.QR_CODE : BarcodeFormat.CODE_128;
        BitMatrix matrix = new MultiFormatWriter()
                .encode(content, zxingFormat, width, height, hints);

        BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        // 目录不存在就建（首次生成时 uploadPath/barcode 还没有）
        if (!target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        ImageIO.write(image, "png", target);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    /** 文件名只留安全字符（条码内容来自用户配置的模板，别把路径特殊字符带进文件系统） */
    private String sanitize(String content) {
        return content.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }
}
