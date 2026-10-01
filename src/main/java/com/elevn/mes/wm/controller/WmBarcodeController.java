package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmBarcode;
import com.elevn.mes.wm.entity.WmBarcodeConfig;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import com.elevn.mes.wm.service.WmBarcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 条码 Controller
 *
 * 【打印怎么实现】
 *   任务表允许简化为"图片批量下载"。我们的做法更顺：前端把条码图片
 *   （barcode_url，走图片服务器 9898）排进打印布局，浏览器 Ctrl+P 出标签纸。
 *   后端不参与打印 —— 打印是"展示"问题，不是"业务"问题。
 *
 */
@RestController
@RequestMapping("/api/wm/barcode")
public class WmBarcodeController {

    @Autowired
    private WmBarcodeService barcodeService;

    // ==================== 规则配置 ====================

    @GetMapping("/config/page")
    public Result<PageInfo<WmBarcodeConfig>> pageConfig(WmBarcodeConfig query,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(barcodeService.pageConfig(pageNum, pageSize, query));
    }

    @GetMapping("/config/{configId}")
    public Result<WmBarcodeConfig> getConfigById(@PathVariable Long configId) {
        return Result.success(barcodeService.getConfigById(configId));
    }

    /** 新增规则（同一类型只能有一条启用中的规则） */
    @PostMapping("/config")
    public Result<WmBarcodeConfig> createConfig(
            @Validated(CreateOption.class) @RequestBody WmBarcodeConfig config) {
        return Result.success(barcodeService.createConfig(config));
    }

    @PutMapping("/config")
    public Result<Void> updateConfig(
            @Validated(UpdateOption.class) @RequestBody WmBarcodeConfig config) {
        barcodeService.updateConfig(config);
        return Result.success();
    }

    @DeleteMapping("/config/{configId}")
    public Result<Void> deleteConfig(@PathVariable Long configId) {
        barcodeService.deleteConfig(configId);
        return Result.success();
    }

    // ==================== 生成 ====================

    /**
     * 给一个业务对象生成条码
     * @param barcodeType ITEM / BATCH / PACKAGE
     * @param bizId 物料ID / 批次ID / 装箱ID
     */
    @PostMapping("/generate/{barcodeType}/{bizId}")
    public Result<WmBarcode> generate(@PathVariable String barcodeType,
                                      @PathVariable Long bizId) {
        return Result.success(barcodeService.generate(barcodeType, bizId));
    }

    /** 批量生成（已生成过的自动跳过） */
    @PostMapping("/generate/{barcodeType}")
    public Result<List<WmBarcode>> generateBatch(@PathVariable String barcodeType,
                                                 @RequestBody List<Long> bizIds) {
        return Result.success(barcodeService.generateBatch(barcodeType, bizIds));
    }

    // ==================== 查询 / 解析 ====================

    @GetMapping("/page")
    public Result<PageInfo<WmBarcode>> page(WmBarcode query,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(barcodeService.page(pageNum, pageSize, query));
    }

    @GetMapping("/{barcodeId}")
    public Result<WmBarcode> getById(@PathVariable Long barcodeId) {
        return Result.success(barcodeService.getById(barcodeId));
    }

    /**
     * 扫码解析：?content=扫码枪读到的内容
     * 前端按返回的 barcode_type 跳物料 / 批次 / 装箱详情
     */
    @GetMapping("/parse")
    public Result<WmBarcode> parse(@RequestParam("content") String content) {
        return Result.success(barcodeService.parse(content));
    }
}
