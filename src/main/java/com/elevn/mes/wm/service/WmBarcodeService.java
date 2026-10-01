package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmBarcode;
import com.elevn.mes.wm.entity.WmBarcodeConfig;

import java.util.List;

/**
 * 条码 Service（规则配置 → 生成 → 解析）
 *
 */
public interface WmBarcodeService {

    // ---------- 规则配置 ----------

    PageInfo<WmBarcodeConfig> pageConfig(int pageNum, int pageSize, WmBarcodeConfig query);

    WmBarcodeConfig getConfigById(Long configId);

    /** 新增规则（同一类型只允许一条启用中的规则，防止生成时不知道听谁的） */
    WmBarcodeConfig createConfig(WmBarcodeConfig config);

    void updateConfig(WmBarcodeConfig config);

    void deleteConfig(Long configId);

    // ---------- 生成 ----------

    /**
     * 按启用规则给一个业务对象生成条码（内容唯一，重复生成会被拦下）
     * @param barcodeType ITEM / BATCH / PACKAGE
     * @param bizId 物料ID / 批次ID / 装箱ID
     */
    WmBarcode generate(String barcodeType, Long bizId);

    /** 批量生成（逐个调 generate，返回成功清单；已存在的内容跳过并计入 skipped） */
    List<WmBarcode> generateBatch(String barcodeType, List<Long> bizIds);

    // ---------- 查询 / 解析 ----------

    PageInfo<WmBarcode> page(int pageNum, int pageSize, WmBarcode query);

    WmBarcode getById(Long barcodeId);

    /**
     * 扫码解析：拿扫码枪读到的内容精确查库，
     * 命中返回条码行（前端按 barcode_type 跳物料/批次/装箱详情）
     */
    WmBarcode parse(String barcodeContent);
}
