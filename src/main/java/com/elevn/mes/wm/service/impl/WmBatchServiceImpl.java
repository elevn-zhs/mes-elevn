package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.entity.MdItemBatchConfig;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.md.service.MdItemBatchConfigService;
import com.elevn.mes.wm.entity.WmBatch;
import com.elevn.mes.wm.entity.WmMaterialStock;
import com.elevn.mes.wm.mapper.WmBatchMapper;
import com.elevn.mes.wm.service.WmBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 批次 Service 实现
 *
 * 【本模块最有价值的一条 L3 校验：批次属性按物料配置决定必填】
 *   批次表字段一大堆（生产日期/有效期/供应商/客户/工单/模具...），
 *   但不是每个物料都要填全。到底填哪些，由 E 线的 md_item_batch_config 说了算：
 *   配置里哪个开关是 'Y'，生成批次时哪个字段就必须有值。
 *   校验放这里而不是前端 —— 前端能绕过，后端不能。
 *
 * 其余 L3 校验：
 *   1. 物料必须开启批次管理（md_item.batch_flag = 'Y'）才允许建批次
 *   2. 批次编号 / 所属物料不可改（编号和物料ID都冗余在下游表里）
 *   3. 删除前查库存 —— 还有在库数量的批次删了，货就查不到来源了
 *
 */
@Service
public class WmBatchServiceImpl implements WmBatchService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 批次编号前缀：PC = 批次 */
    private static final String BATCH_CODE_PREFIX = "PC";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 质量状态默认值，与字典 wm_batch_quality_status 的默认项保持一致 */
    private static final String QUALITY_STATUS_PENDING = "PENDING";

    @Autowired
    private WmBatchMapper wmBatchMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Autowired
    private MdItemBatchConfigService mdItemBatchConfigService;

    @Override
    public PageInfo<WmBatch> page(int pageNum, int pageSize, WmBatch wmBatch) {
        PageHelper.startPage(pageNum, pageSize);
        List<WmBatch> list = wmBatchMapper.selectByCondition(wmBatch);
        return new PageInfo<>(list);
    }

    @Override
    public WmBatch queryById(Long id) {
        WmBatch wmBatch = wmBatchMapper.selectById(id);
        if (wmBatch != null) {
            // 详情页多带一块：这批货分布在哪些仓库库位，各多少
            List<WmMaterialStock> stockList = wmBatchMapper.selectStockByBatchId(id);
            wmBatch.setStockList(stockList);
            BigDecimal total = BigDecimal.ZERO;
            for (WmMaterialStock stock : stockList) {
                if (stock.getQuantityOnhand() != null) {
                    total = total.add(stock.getQuantityOnhand());
                }
            }
            wmBatch.setTotalQuantity(total);
        }
        return wmBatch;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(WmBatch wmBatch) {
        MdItem item = checkItemBatchEnabled(wmBatch.getItemId());
        fillItemSnapshot(wmBatch, item);

        if (isBlank(wmBatch.getQualityStatus())) {
            wmBatch.setQualityStatus(QUALITY_STATUS_PENDING);
        }
        // 按物料的批次配置校验属性必填（核心 L3）
        checkBatchAttrByConfig(wmBatch, item);

        wmBatch.setCreateBy(DEFAULT_OPERATOR);
        wmBatch.setUpdateBy(DEFAULT_OPERATOR);
        wmBatch.setCreateTime(LocalDateTime.now());
        wmBatch.setUpdateTime(LocalDateTime.now());

        // 编号里要带主键，所以先占个位把行插进去，拿到主键后再拼真编号回填
        wmBatch.setBatchCode("TMP-" + System.nanoTime());
        int rows = wmBatchMapper.insert(wmBatch);
        if (rows == 1) {
            wmBatch.setBatchCode(buildBatchCode(wmBatch.getBatchId()));
            wmBatchMapper.updateBatchCode(wmBatch);
        }
        return rows;
    }

    @Override
    public int updateById(WmBatch wmBatch) {
        WmBatch db = checkExists(wmBatch.getBatchId());

        // 批次编号不可改：它冗余在库存、单据行、流水里，改了要批量刷，漏一处账就错
        if (wmBatch.getBatchCode() != null && !wmBatch.getBatchCode().equals(db.getBatchCode())) {
            throw new BusinessException("批次编号不允许修改（当前为 " + db.getBatchCode()
                    + "）。如需更换请新建批次");
        }
        // 所属物料不可改：换了物料，这批的历史库存与消耗记录就全解释不通了
        if (wmBatch.getItemId() != null && !wmBatch.getItemId().equals(db.getItemId())) {
            throw new BusinessException("批次所属物料不允许修改。如需调整请新建批次");
        }

        // 编辑时前端是全量提交，但万一漏传了某个属性字段，校验会误判成"没填"。
        // 所以先用库里的值把没传的属性补上，再做必填校验。
        WmBatch merged = mergeBatchAttr(wmBatch, db);
        MdItem item = mdItemMapper.selectById(db.getItemId());
        if (item != null) {
            checkBatchAttrByConfig(merged, item);
        }

        wmBatch.setUpdateBy(DEFAULT_OPERATOR);
        wmBatch.setUpdateTime(LocalDateTime.now());
        return wmBatchMapper.updateById(wmBatch);
    }

    @Override
    public int deleteById(Long id) {
        checkCanDelete(id);
        return wmBatchMapper.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        for (Long id : ids) {
            checkCanDelete(id);
        }
        return wmBatchMapper.deleteBatch(ids);
    }

    // ==================== 以下为 L3 业务校验 ====================

    /**
     * 校验物料存在、且开启了批次管理
     *
     * 物料本身不让管批次（md_item.batch_flag = 'N'），却硬要给它建批次，
     * 后面入库时批次字段用不上，数据就是垃圾 —— 所以在入口就拦住。
     */
    private MdItem checkItemBatchEnabled(Long itemId) {
        if (itemId == null) {
            throw new BusinessException("物料不能为空");
        }
        MdItem item = mdItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物料不存在或已被删除");
        }
        if (!"Y".equals(item.getBatchFlag())) {
            throw new BusinessException("物料[" + item.getItemCode() + " " + item.getItemName()
                    + "]没有开启批次管理，不需要建批次。请先到 主数据-物料管理 里把批次管理开关打开");
        }
        return item;
    }

    /**
     * 回填物料快照（编码 / 名称 / 规格 / 单位）
     * 不信前端传的，现查一次 md_item —— 物料改名或前端伪造时，批次上显示的还是对的信息
     */
    private void fillItemSnapshot(WmBatch wmBatch, MdItem item) {
        wmBatch.setItemCode(item.getItemCode());
        wmBatch.setItemName(item.getItemName());
        wmBatch.setSpecification(item.getSpecification());
        wmBatch.setUnitOfMeasure(item.getUnitOfMeasure());
    }

    /**
     * 按物料的批次属性配置做必填校验（本模块的核心 L3）
     *
     * 配置来自 E 线：md_item_batch_config 里 14 个开关，'Y' 表示这个物料
     * 的批次号必须携带该属性。比如原料类配了 vendor_flag='Y'，
     * 那么建批次时不填供应商就过不去 —— 因为没有供应商就追溯不了来源。
     *
     * 配置不存在或未启用时不做限制（相当于该物料没特殊要求）。
     */
    private void checkBatchAttrByConfig(WmBatch wmBatch, MdItem item) {
        MdItemBatchConfig config = mdItemBatchConfigService.queryByItemId(wmBatch.getItemId());
        if (config == null || !"Y".equals(config.getEnableFlag())) {
            return;
        }
        List<String> missing = new ArrayList<>();
        if ("Y".equals(config.getProduceDateFlag()) && wmBatch.getProduceDate() == null) {
            missing.add("生产日期");
        }
        if ("Y".equals(config.getExpireDateFlag()) && wmBatch.getExpireDate() == null) {
            missing.add("有效期");
        }
        if ("Y".equals(config.getRecptDateFlag()) && wmBatch.getRecptDate() == null) {
            missing.add("入库日期");
        }
        if ("Y".equals(config.getVendorFlag()) && wmBatch.getVendorId() == null) {
            missing.add("供应商");
        }
        if ("Y".equals(config.getClientFlag()) && wmBatch.getClientId() == null) {
            missing.add("客户");
        }
        if ("Y".equals(config.getCoCodeFlag()) && isBlank(wmBatch.getSoCode())) {
            missing.add("销售订单编号");
        }
        if ("Y".equals(config.getPoCodeFlag()) && isBlank(wmBatch.getPoCode())) {
            missing.add("采购订单编号");
        }
        if ("Y".equals(config.getWorkorderFlag()) && wmBatch.getWorkorderId() == null) {
            missing.add("生产工单");
        }
        if ("Y".equals(config.getTaskFlag()) && wmBatch.getTaskId() == null) {
            missing.add("生产任务");
        }
        if ("Y".equals(config.getWorkstationFlag()) && wmBatch.getWorkstationId() == null) {
            missing.add("工作站");
        }
        if ("Y".equals(config.getToolFlag()) && wmBatch.getToolId() == null) {
            missing.add("工具");
        }
        if ("Y".equals(config.getMoldFlag()) && wmBatch.getMoldId() == null) {
            missing.add("模具");
        }
        if ("Y".equals(config.getLotNumberFlag()) && isBlank(wmBatch.getLotNumber())) {
            missing.add("生产批号");
        }
        if ("Y".equals(config.getQualityStatusFlag()) && isBlank(wmBatch.getQualityStatus())) {
            missing.add("质量状态");
        }
        if (!missing.isEmpty()) {
            throw new BusinessException("物料[" + item.getItemCode() + " " + item.getItemName()
                    + "]的批次属性配置要求以下字段必填：" + String.join("、", missing));
        }
    }

    /**
     * 用库里的值补齐入参中没传的属性字段，专供必填校验使用
     * （不补齐的话，前端只改个备注就会被误报"生产日期不能为空"）
     */
    private WmBatch mergeBatchAttr(WmBatch param, WmBatch db) {
        if (param.getProduceDate() == null) param.setProduceDate(db.getProduceDate());
        if (param.getExpireDate() == null) param.setExpireDate(db.getExpireDate());
        if (param.getRecptDate() == null) param.setRecptDate(db.getRecptDate());
        if (param.getVendorId() == null) param.setVendorId(db.getVendorId());
        if (param.getClientId() == null) param.setClientId(db.getClientId());
        if (isBlank(param.getSoCode())) param.setSoCode(db.getSoCode());
        if (isBlank(param.getPoCode())) param.setPoCode(db.getPoCode());
        if (param.getWorkorderId() == null) param.setWorkorderId(db.getWorkorderId());
        if (param.getTaskId() == null) param.setTaskId(db.getTaskId());
        if (param.getWorkstationId() == null) param.setWorkstationId(db.getWorkstationId());
        if (param.getToolId() == null) param.setToolId(db.getToolId());
        if (param.getMoldId() == null) param.setMoldId(db.getMoldId());
        if (isBlank(param.getLotNumber())) param.setLotNumber(db.getLotNumber());
        if (isBlank(param.getQualityStatus())) param.setQualityStatus(db.getQualityStatus());
        return param;
    }

    /**
     * 删除前的引用检查（L3）
     */
    private void checkCanDelete(Long batchId) {
        WmBatch db = checkExists(batchId);
        int stockCount = wmBatchMapper.countStockByBatchId(batchId);
        if (stockCount > 0) {
            throw new BusinessException("批次[" + db.getBatchCode() + "]下还有 " + stockCount
                    + " 条库存，不能删除。请先把这批货出库或调拨走");
        }
    }

    /**
     * 校验批次是否存在
     */
    private WmBatch checkExists(Long batchId) {
        if (batchId == null) {
            throw new BusinessException("批次ID不能为空");
        }
        WmBatch db = wmBatchMapper.selectById(batchId);
        if (db == null) {
            throw new BusinessException("批次不存在或已被删除");
        }
        return db;
    }

    /**
     * 生成批次编号：PC + yyyyMMdd + 6 位主键补零
     * 例：PC20260923 001005
     *
     * 带上主键是为了天然唯一，不用再去查一次"今天最大序号是多少"，
     * 也就没有并发取号撞车的问题。
     */
    private String buildBatchCode(Long batchId) {
        return BATCH_CODE_PREFIX
                + LocalDate.now().format(DATE_FORMATTER)
                + String.format("%06d", batchId == null ? 0L : batchId);
    }

    private boolean isBlank(String str) {
        return str == null || "".equals(str);
    }
}
