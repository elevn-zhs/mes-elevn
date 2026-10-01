package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.pro.entity.ProTaskIssue;
import com.elevn.mes.pro.mapper.ProTaskIssueMapper;
import com.elevn.mes.sys.service.SysCodingRuleService;
import com.elevn.mes.wm.entity.WmBatch;
import com.elevn.mes.wm.entity.WmDoc;
import com.elevn.mes.wm.entity.WmDocDetail;
import com.elevn.mes.wm.entity.WmDocLine;
import com.elevn.mes.wm.entity.WmLocation;
import com.elevn.mes.wm.entity.WmMaterialStock;
import com.elevn.mes.wm.entity.WmTransaction;
import com.elevn.mes.wm.entity.WmWarehouse;
import com.elevn.mes.wm.mapper.WmBatchMapper;
import com.elevn.mes.wm.mapper.WmDocDetailMapper;
import com.elevn.mes.wm.mapper.WmDocLineMapper;
import com.elevn.mes.wm.mapper.WmDocMapper;
import com.elevn.mes.wm.mapper.WmLocationMapper;
import com.elevn.mes.wm.mapper.WmMaterialStockMapper;
import com.elevn.mes.wm.mapper.WmTransactionMapper;
import com.elevn.mes.wm.mapper.WmWarehouseMapper;
import com.elevn.mes.wm.service.WmDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 出入库单据 Service 实现
 *
 * ============================ 本模块最重要的一段代码在 execute() ============================
 *
 * 单据过账（execute）在一个事务里做四件事，缺一不可：
 *   ① 写 wm_doc_detail   —— 落位："这批货实际从/到哪个库位"
 *   ② 改 wm_material_stock —— 库存余额，唯一会动的表
 *   ③ 写 wm_transaction  —— 流水账，每一次变动留一条（调拨写配对两条）
 *   ④ 回写 pro_task_issue —— 生产领料单过账后，把投料记到生产任务上（A/B 两条线的接口）
 *
 * 为什么必须同一个事务？
 *   试想"库存已经扣了，但流水没写"：月底对账时，库存余额和流水求和差一个数，
 *   而且永远查不出这货是怎么少的。四件事同生共死，才能保证账实相符。
 *
 * ============================ 库存口径（最容易做错的地方） ============================
 *
 * wm_material_stock 上有 uk_stock 唯一键：
 *   同一「物料 + 批次 + 仓库 + 库区 + 库位 + 容器」只允许一行。
 *   所以入库不是无脑 insert，而是"按六件套找行 → 找到累加、找不到新建"。
 *   做错了同一批料会散成好几行，对账永远对不平。
 *   另外批次/库区/库位/容器用 0 与空串表示"无"，绝不给 NULL ——
 *   MySQL 唯一索引里 NULL 不参与比较，一旦有 NULL，这条约束就形同虚设。
 *
 * 出库走两条路：
 *   · 行里带了 material_stock_id  → 就按指定的那一行扣
 *   · 行里没带                    → 后端按 物料 + 批次 + 仓库/库位 自己找，
 *                                   按入库时间升序（先进先出），不够就跨库位拆分
 *   两条路最后都会落到 reduceOnhand，它的 where 里带了可用量判断，
 *   返回 0 行说明"余额不够或被人抢走了"，直接抛异常回滚 ——
 *   这是并发下真正堵死超卖的那一道，Service 里的校验只是提前给个好读的报错。
 *
 * ============================ 数量方向 ============================
 *   库存和流水里的数量一律记正数，方向由单据头的 io_flag 决定：
 *     I 入库 → 库存加、流水 transaction_flag = 1
 *     O 出库 → 库存减、流水 transaction_flag = -1
 *     T 调拨 → 源仓减一条(-1) + 目标仓加一条(1)，两条用 related_transaction_id 互指
 *
 */
@Service
public class WmDocServiceImpl implements WmDocService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 单据状态 */
    private static final String ST_PREPARE = "PREPARE";
    private static final String ST_CONFIRMED = "CONFIRMED";
    private static final String ST_CANCELED = "CANCELED";

    /** 出入库标志 */
    private static final String IO_IN = "I";
    private static final String IO_OUT = "O";
    private static final String IO_TRANSFER = "T";

    /** 单据类型常量 */
    private static final String TYPE_TRANSFER = "TRANSFER";
    private static final String TYPE_ISSUE = "ISSUE";
    private static final String TYPE_PRODUCT_PRODUCE = "PRODUCT_PRODUCE";
    private static final String TYPE_PRODUCT_RECPT = "PRODUCT_RECPT";

    /** 投料记录的来源表名（写进 pro_task_issue.source_doc_table） */
    private static final String SOURCE_DOC_TABLE_WM_DOC = "wm_doc";

    /**
     * 单据类型 → 出入库标志
     *
     * 【为什么写死在代码里，而不是让前端选 io_flag】
     *   "这张单是入库还是出库"是业务的定义，不是操作员的选项。
     *   让前端传，就等于允许"把采购入库单传成出库"，库存立刻算反。
     *   放在这里由 doc_type 推导，前端那个字段只是个展示。
     */
    private static final Map<String, String> DOC_TYPE_IO_FLAG = new LinkedHashMap<>();

    static {
        // 入库类 I（7 类）
        DOC_TYPE_IO_FLAG.put("ITEM_RECPT", IO_IN);
        DOC_TYPE_IO_FLAG.put("PRODUCT_RECPT", IO_IN);
        DOC_TYPE_IO_FLAG.put("PRODUCT_PRODUCE", IO_IN);
        DOC_TYPE_IO_FLAG.put("RT_ISSUE", IO_IN);
        DOC_TYPE_IO_FLAG.put("RT_SALES", IO_IN);
        DOC_TYPE_IO_FLAG.put("OUTSOURCE_RECPT", IO_IN);
        DOC_TYPE_IO_FLAG.put("MISC_RECPT", IO_IN);
        // 出库类 O（6 类）
        DOC_TYPE_IO_FLAG.put("RT_VENDOR", IO_OUT);
        DOC_TYPE_IO_FLAG.put("ISSUE", IO_OUT);
        DOC_TYPE_IO_FLAG.put("ITEM_CONSUME", IO_OUT);
        DOC_TYPE_IO_FLAG.put("PRODUCT_SALES", IO_OUT);
        DOC_TYPE_IO_FLAG.put("OUTSOURCE_ISSUE", IO_OUT);
        DOC_TYPE_IO_FLAG.put("MISC_ISSUE", IO_OUT);
        // 调拨 T（1 类）
        DOC_TYPE_IO_FLAG.put(TYPE_TRANSFER, IO_TRANSFER);
    }

    @Autowired
    private WmDocMapper wmDocMapper;

    @Autowired
    private WmDocLineMapper wmDocLineMapper;

    @Autowired
    private WmDocDetailMapper wmDocDetailMapper;

    @Autowired
    private WmMaterialStockMapper wmMaterialStockMapper;

    @Autowired
    private WmTransactionMapper wmTransactionMapper;

    @Autowired
    private WmWarehouseMapper wmWarehouseMapper;

    @Autowired
    private WmLocationMapper wmLocationMapper;

    @Autowired
    private WmBatchMapper wmBatchMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Autowired
    private ProTaskIssueMapper proTaskIssueMapper;

    @Autowired
    private SysCodingRuleService sysCodingRuleService;

    // ====================================================================
    // 一、查询
    // ====================================================================

    @Override
    public PageInfo<WmDoc> page(int pageNum, int pageSize, WmDoc wmDoc) {
        PageHelper.startPage(pageNum, pageSize);
        List<WmDoc> list = wmDocMapper.selectByCondition(wmDoc);
        return new PageInfo<>(list);
    }

    @Override
    public WmDoc queryById(Long docId) {
        WmDoc doc = wmDocMapper.selectById(docId);
        if (doc != null) {
            doc.setLineList(wmDocLineMapper.selectByDocId(docId));
            doc.setDetailList(wmDocDetailMapper.selectByDocId(docId));
        }
        return doc;
    }

    @Override
    public List<WmDocLine> queryLineList(Long docId) {
        return wmDocLineMapper.selectByDocId(docId);
    }

    @Override
    public List<WmDocDetail> queryDetailList(Long docId) {
        return wmDocDetailMapper.selectByDocId(docId);
    }

    // ====================================================================
    // 二、新增 / 修改 / 删除（都只在 PREPARE 阶段允许）
    // ====================================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(WmDoc wmDoc) {
        checkDocType(wmDoc.getDocType());
        // io_flag 一律由 doc_type 推导，覆盖前端传的任何值
        wmDoc.setIoFlag(DOC_TYPE_IO_FLAG.get(wmDoc.getDocType()));
        wmDoc.setStatus(ST_PREPARE);
        if (wmDoc.getBizDate() == null) {
            wmDoc.setBizDate(LocalDateTime.now());
        }
        if (wmDoc.getDeliveryFlag() == null) {
            wmDoc.setDeliveryFlag("N");
        }
        if (wmDoc.getConfirmFlag() == null) {
            wmDoc.setConfirmFlag("N");
        }
        fillDocLocationSnapshot(wmDoc);
        checkBizRules(wmDoc);
        checkLineList(wmDoc);

        if (isBlank(wmDoc.getDocName())) {
            wmDoc.setDocName(buildDefaultDocName(wmDoc.getDocType()));
        }

        wmDoc.setCreateBy(DEFAULT_OPERATOR);
        wmDoc.setUpdateBy(DEFAULT_OPERATOR);
        wmDoc.setCreateTime(LocalDateTime.now());
        wmDoc.setUpdateTime(LocalDateTime.now());
        // 编号按编码规则现取。取号是数据库层的原子自增（sys_coding_rules 行锁），
        // 不和"查最大号 +1"那条老路走，避免并发时两张单拿到同一个号。
        wmDoc.setDocCode(sysCodingRuleService.autoCode("WM_" + wmDoc.getDocType()));

        int rows = wmDocMapper.insert(wmDoc);
        if (rows == 1) {
            insertLines(wmDoc);
        }
        return rows;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int updateById(WmDoc wmDoc) {
        WmDoc db = checkExists(wmDoc.getDocId());
        checkStatusCanModify(db, "修改");

        // 单据类型不允许改：改了 io_flag 的方向就变了，已生成的编号也对不上
        if (!isBlank(wmDoc.getDocType()) && !wmDoc.getDocType().equals(db.getDocType())) {
            throw new BusinessException("单据类型不允许修改（当前为 " + db.getDocType()
                    + "）。如需更换请新建单据");
        }
        // 这三个字段由后端或业务规则决定，不从入参里取
        wmDoc.setDocType(null);
        wmDoc.setIoFlag(null);
        wmDoc.setDocCode(null);
        wmDoc.setStatus(null);

        // 校验要在"合并了库里已有值"的视图上做，否则前端只改了备注就会被误报"请选择仓库"
        WmDoc merged = mergeForCheck(db, wmDoc);
        fillDocLocationSnapshot(merged);
        checkBizRules(merged);

        wmDoc.setUpdateBy(DEFAULT_OPERATOR);
        wmDoc.setUpdateTime(LocalDateTime.now());
        int rows = wmDocMapper.updateById(wmDoc);

        // 行是"整批替换式"修改：先按单据ID逻辑删掉旧行，再整批插新的。
        // 这样保证幂等，也不会出现"改了 3 行、留下 2 行旧的"这种半新半旧的状态。
        if (wmDoc.getLineList() != null) {
            if (wmDoc.getLineList().isEmpty()) {
                throw new BusinessException("单据至少要有一行明细，不能全部删掉（如需作废请取消整单）");
            }
            wmDocLineMapper.deleteByDocId(wmDoc.getDocId());
            WmDoc forLine = new WmDoc();
            forLine.setDocId(db.getDocId());
            forLine.setDocType(db.getDocType());
            forLine.setIoFlag(db.getIoFlag());
            forLine.setWarehouseId(merged.getWarehouseId());
            forLine.setWarehouseCode(merged.getWarehouseCode());
            forLine.setWarehouseName(merged.getWarehouseName());
            forLine.setLocationId(merged.getLocationId());
            forLine.setLocationCode(merged.getLocationCode());
            forLine.setLocationName(merged.getLocationName());
            forLine.setAreaId(merged.getAreaId());
            forLine.setAreaCode(merged.getAreaCode());
            forLine.setAreaName(merged.getAreaName());
            forLine.setLineList(wmDoc.getLineList());
            insertLines(forLine);
        }
        return rows;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteById(Long docId) {
        WmDoc db = checkExists(docId);
        checkStatusCanModify(db, "删除");
        wmDocLineMapper.deleteByDocId(docId);
        return wmDocMapper.deleteById(docId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteBatch(Long[] docIds) {
        if (docIds == null || docIds.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        for (Long docId : docIds) {
            WmDoc db = checkExists(docId);
            checkStatusCanModify(db, "删除");
        }
        for (Long docId : docIds) {
            wmDocLineMapper.deleteByDocId(docId);
        }
        return wmDocMapper.deleteBatch(docIds);
    }

    // ====================================================================
    // 四、单据行的单行维护（只有【待过账】的单据能改行）
    // ====================================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int saveLine(WmDocLine wmDocLine) {
        WmDoc doc = checkExists(wmDocLine.getDocId());
        checkStatusCanModify(doc, "新增明细行");
        MdItem item = loadItem(wmDocLine.getItemId());
        checkLineQuantity(wmDocLine);
        fillLineItemSnapshot(wmDocLine, item);
        // 物料开了批次管理就必须给批次；方向不同，报错文案不同
        if (IO_IN.equals(doc.getIoFlag())) {
            checkLineBatchRule(wmDocLine, item);
        } else {
            checkBatchRequiredForOutbound(wmDocLine, item);
        }
        fillLineDefaults(wmDocLine, doc);
        if (wmDocLine.getBatchId() != null) {
            wmDocLine.setBatchCode(resolveBatchCode(wmDocLine));
        }
        if (wmDocLine.getQcFlag() == null) {
            wmDocLine.setQcFlag("N");
        }
        Integer maxLineNo = wmDocLineMapper.selectMaxLineNo(doc.getDocId());
        wmDocLine.setLineNo(maxLineNo == null ? 1 : maxLineNo + 1);
        wmDocLine.setDocType(doc.getDocType());
        wmDocLine.setCreateBy(DEFAULT_OPERATOR);
        wmDocLine.setUpdateBy(DEFAULT_OPERATOR);
        wmDocLine.setCreateTime(LocalDateTime.now());
        wmDocLine.setUpdateTime(LocalDateTime.now());
        return wmDocLineMapper.insert(wmDocLine);
    }

    @Override
    public PageInfo<WmDocLine> pageLine(int pageNum, int pageSize, Long docId) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(wmDocLineMapper.selectByDocId(docId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int updateLine(WmDocLine wmDocLine) {
        WmDocLine dbLine = checkLineExists(wmDocLine.getLineId());
        WmDoc doc = checkExists(dbLine.getDocId());
        checkStatusCanModify(doc, "修改明细行");
        // 行不允许搬到别的单子上：流水里记的是 source_doc_line_id，搬了来源就断了
        if (wmDocLine.getDocId() != null && !wmDocLine.getDocId().equals(dbLine.getDocId())) {
            throw new BusinessException("不能在单据之间搬动明细行。如需调整请删除后重新添加");
        }
        wmDocLine.setDocId(null);
        wmDocLine.setDocType(null);
        if (wmDocLine.getQuantity() != null) {
            checkLineQuantity(wmDocLine);
        }
        // 换了物料要重刷快照与批次（旧物料的批次不一定适用于新物料）
        if (wmDocLine.getItemId() != null && !wmDocLine.getItemId().equals(dbLine.getItemId())) {
            MdItem item = loadItem(wmDocLine.getItemId());
            fillLineItemSnapshot(wmDocLine, item);
            wmDocLineMapper.updateItemSnapshot(wmDocLine);
            wmDocLine.setBatchId(null);
        }
        if (wmDocLine.getBatchId() != null) {
            wmDocLine.setBatchCode(resolveBatchCode(wmDocLine));
        }
        wmDocLine.setUpdateBy(DEFAULT_OPERATOR);
        wmDocLine.setUpdateTime(LocalDateTime.now());
        return wmDocLineMapper.updateById(wmDocLine);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteLine(Long lineId) {
        WmDocLine dbLine = checkLineExists(lineId);
        WmDoc doc = checkExists(dbLine.getDocId());
        checkStatusCanModify(doc, "删除明细行");
        // 行全删光就没法过账了，留一行在手是底线
        if (wmDocLineMapper.countByDocId(dbLine.getDocId()) <= 1) {
            throw new BusinessException("单据至少要保留一行明细，不能全部删光（如需作废请取消整单）");
        }
        return wmDocLineMapper.deleteById(lineId);
    }

    // ====================================================================
    // 五、过账（本模块的心脏）
    // ====================================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int execute(Long docId) {
        WmDoc doc = checkExists(docId);
        if (!ST_PREPARE.equals(doc.getStatus())) {
            throw new BusinessException("单据[" + doc.getDocCode() + "]当前状态为【"
                    + statusText(doc.getStatus()) + "】，只有【待过账】的单据才能过账");
        }
        List<WmDocLine> lines = wmDocLineMapper.selectByDocId(docId);
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("单据[" + doc.getDocCode() + "]没有任何明细行，无法过账");
        }

        // —— 第 0 步：抢状态，这一步就是我们的"锁" ——
        // updateExecute 的 where 带 status = 'PREPARE'。
        // 两个人同时点过账：第一个改成功（返回 1），第二个返回 0 —— 直接拒绝，
        // 库存绝不会被扣两遍。放在最前面是为了尽早拿到行锁，让后来者立刻失败。
        WmDoc statusUpdate = new WmDoc();
        statusUpdate.setDocId(docId);
        statusUpdate.setUpdateBy(DEFAULT_OPERATOR);
        statusUpdate.setUpdateTime(LocalDateTime.now());
        if (wmDocMapper.updateExecute(statusUpdate) == 0) {
            throw new BusinessException("单据[" + doc.getDocCode()
                    + "]已经被处理过了（可能有人刚刚点了过账），请刷新后重试");
        }

        // 幂等：清掉可能残留的落位记录（本表是过程数据，删了重写没有副作用）
        wmDocDetailMapper.deleteByDocId(docId);

        // —— 第 1~3 步：按方向处理库存、落位、流水 ——
        if (IO_IN.equals(doc.getIoFlag())) {
            inbound(doc, lines);
        } else if (IO_OUT.equals(doc.getIoFlag())) {
            outbound(doc, lines);
        } else {
            transfer(doc, lines);
        }

        // —— 第 4 步：生产领料单回写投料（A/B 两条线的接口） ——
        if (TYPE_ISSUE.equals(doc.getDocType())) {
            writeTaskIssue(doc, lines);
        }
        return 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int cancel(Long docId, String reason) {
        WmDoc db = checkExists(docId);
        if (isBlank(reason)) {
            throw new BusinessException("请填写取消原因");
        }
        if (ST_CONFIRMED.equals(db.getStatus())) {
            throw new BusinessException("单据[" + db.getDocCode()
                    + "]已经过账、库存已变动，不能取消。如需反悔请开一张反方向的单据（红冲）");
        }
        if (!ST_PREPARE.equals(db.getStatus())) {
            throw new BusinessException("单据[" + db.getDocCode() + "]当前状态为【"
                    + statusText(db.getStatus()) + "】，不能取消");
        }
        WmDoc update = new WmDoc();
        update.setDocId(docId);
        update.setReason(reason);
        update.setUpdateBy(DEFAULT_OPERATOR);
        update.setUpdateTime(LocalDateTime.now());
        return wmDocMapper.updateCancel(update);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int finish(Long docId) {
        WmDoc db = checkExists(docId);
        if (!TYPE_TRANSFER.equals(db.getDocType())) {
            throw new BusinessException("只有调拨单需要做送达确认，其他单据过账即为完成");
        }
        if (!ST_CONFIRMED.equals(db.getStatus())) {
            throw new BusinessException("调拨单[" + db.getDocCode() + "]当前状态为【"
                    + statusText(db.getStatus()) + "】，只有【已过账】的调拨单能做送达确认");
        }
        WmDoc update = new WmDoc();
        update.setDocId(docId);
        update.setUpdateBy(DEFAULT_OPERATOR);
        update.setUpdateTime(LocalDateTime.now());
        return wmDocMapper.updateFinish(update);
    }

    // ====================================================================
    // 六、入库：按六件套找行 → 累加，找不到才新建
    // ====================================================================

    private void inbound(WmDoc doc, List<WmDocLine> lines) {
        for (WmDocLine line : lines) {
            MdItem item = loadItem(line.getItemId());
            refreshLineSnapshot(line, item);
            checkLineBatchRule(line, item);

            StockSlot slot = resolveSlot(line, doc);
            requireWarehouse(slot, line);
            String batchCode = resolveBatchCode(line);

            // ① 找行 / 建行 + 累加库存（返回库存记录ID）
            Long stockId = upsertStock(line, item, slot, batchCode, line.getQuantity());
            line.setMaterialStockId(stockId);
            wmDocLineMapper.updateMaterialStockId(line);

            // ② 落位明细
            WmDocDetail detail = buildDetail(doc, line, slot, line.getQuantity(), stockId);
            wmDocDetailMapper.insert(detail);

            // ③ 流水（入库 flag = 1）
            writeTransaction(doc, line, detail, 1, line.getQuantity(), stockId);
        }
    }

    // ====================================================================
    // 七、出库：定位库存行（指定 or 先进先出自动找），可用量校验后扣减
    // ====================================================================

    private void outbound(WmDoc doc, List<WmDocLine> lines) {
        for (WmDocLine line : lines) {
            MdItem item = loadItem(line.getItemId());
            refreshLineSnapshot(line, item);
            checkBatchRequiredForOutbound(line, item);

            StockSlot slot = resolveSlot(line, doc);
            requireWarehouse(slot, line);

            // ① 分配 + 扣减（按先进先出跨库位拆分，返回落位明细列表）
            List<WmDocDetail> details = allocateAndReduce(doc, line, slot);
            for (WmDocDetail detail : details) {
                // ② 落位明细
                wmDocDetailMapper.insert(detail);
                // ③ 流水（出库 flag = -1）
                writeTransaction(doc, line, detail, -1, detail.getQuantity(), detail.getMaterialStockId());
            }
        }
    }

    // ====================================================================
    // 八、调拨：源仓出一进、目标仓进一笔，流水配对两条
    // ====================================================================

    private void transfer(WmDoc doc, List<WmDocLine> lines) {
        StockSlot fromSlot = new StockSlot();
        fromSlot.warehouseId = doc.getWarehouseId();
        fromSlot.locationId = doc.getLocationId();
        fromSlot.areaId = doc.getAreaId();
        fillSlotNames(fromSlot);

        StockSlot toSlot = new StockSlot();
        toSlot.warehouseId = doc.getToWarehouseId();
        toSlot.locationId = doc.getToLocationId();
        toSlot.areaId = doc.getToAreaId();
        fillSlotNames(toSlot);

        if (toSlot.warehouseId == null) {
            throw new BusinessException("调拨单必须指定目标仓库");
        }

        // 配对关系：每条"源仓出"的流水 ID ↔ 对应的"目标仓进"的流水 ID
        List<Long[]> pairs = new ArrayList<>();

        for (WmDocLine line : lines) {
            MdItem item = loadItem(line.getItemId());
            refreshLineSnapshot(line, item);
            checkBatchRequiredForOutbound(line, item);
            requireWarehouse(fromSlot, line);

            // 源仓：分配 + 扣减，顺手拆好落位（用的是库存行真实所在库位，不是"建议库位"）
            List<WmDocDetail> outDetails = allocateAndReduce(doc, line, fromSlot);
            String batchCode = resolveBatchCode(line);

            for (WmDocDetail outDetail : outDetails) {
                wmDocDetailMapper.insert(outDetail);
                Long trxOutId = writeTransaction(doc, line, outDetail, -1,
                        outDetail.getQuantity(), outDetail.getMaterialStockId());

                // 目标仓：同样的六件套找行/建行 + 累加
                Long targetStockId = upsertStock(line, item, toSlot, batchCode, outDetail.getQuantity());
                WmDocDetail inDetail = buildDetail(doc, line, toSlot, outDetail.getQuantity(), targetStockId);
                wmDocDetailMapper.insert(inDetail);
                Long trxInId = writeTransaction(doc, line, inDetail, 1,
                        inDetail.getQuantity(), targetStockId);

                pairs.add(new Long[]{trxOutId, trxInId});
            }
        }

        // 两条流水互相回填配对ID（一条指向另一条，页面上点一下就能看到对应的那笔）
        for (Long[] pair : pairs) {
            wmTransactionMapper.updateRelatedTransactionId(pair[0], pair[1]);
            wmTransactionMapper.updateRelatedTransactionId(pair[1], pair[0]);
        }
    }

    // ====================================================================
    // 九、库存操作：找行/建行、扣减、分配
    // ====================================================================

    /**
     * 入库式的"找行 + 累加"：六件套命中就累加，没命中就新建一行
     *
     * @return 库存记录ID
     */
    private Long upsertStock(WmDocLine line, MdItem item, StockSlot slot,
                             String batchCode, BigDecimal quantity) {
        WmMaterialStock dimension = new WmMaterialStock();
        dimension.setItemId(line.getItemId());
        dimension.setBatchId(nvlLong(line.getBatchId()));
        dimension.setWarehouseId(slot.warehouseId);
        dimension.setLocationId(nvlLong(slot.locationId));
        dimension.setAreaId(nvlLong(slot.areaId));
        dimension.setPackageId(0L);

        WmMaterialStock exist = wmMaterialStockMapper.selectByDimension(dimension);
        if (exist != null) {
            wmMaterialStockMapper.addOnhand(exist.getMaterialStockId(), quantity);
            return exist.getMaterialStockId();
        }

        WmMaterialStock stock = new WmMaterialStock();
        stock.setItemTypeId(item.getItemTypeId());
        stock.setItemId(line.getItemId());
        stock.setItemCode(line.getItemCode());
        stock.setItemName(line.getItemName());
        stock.setSpecification(line.getSpecification());
        stock.setUnitOfMeasure(line.getUnitOfMeasure());
        stock.setUnitName(item.getUnitName());
        // 四个维度即使为"无"也要落 0 / 空串，不允许 NULL（NULL 会让 uk_stock 失效）
        stock.setBatchId(nvlLong(line.getBatchId()));
        stock.setBatchCode(batchCode == null ? "" : batchCode);
        stock.setWarehouseId(slot.warehouseId);
        stock.setWarehouseCode(nvlStr(slot.warehouseCode));
        stock.setWarehouseName(nvlStr(slot.warehouseName));
        stock.setLocationId(nvlLong(slot.locationId));
        stock.setLocationCode(nvlStr(slot.locationCode));
        stock.setLocationName(nvlStr(slot.locationName));
        stock.setAreaId(nvlLong(slot.areaId));
        stock.setAreaCode(nvlStr(slot.areaCode));
        stock.setAreaName(nvlStr(slot.areaName));
        stock.setPackageId(0L);
        stock.setPackageCode("");
        stock.setQuantityOnhand(quantity);
        stock.setQuantityReserved(BigDecimal.ZERO);
        stock.setRecptDate(LocalDateTime.now());
        stock.setFrozenFlag("N");
        // 生产日期 / 有效期跟着批次走
        WmBatch batch = line.getBatchId() == null ? null : wmBatchMapper.selectById(line.getBatchId());
        if (batch != null) {
            stock.setProductionDate(batch.getProduceDate());
            stock.setExpireDate(batch.getExpireDate());
        }
        stock.setCreateBy(DEFAULT_OPERATOR);
        stock.setUpdateBy(DEFAULT_OPERATOR);
        stock.setCreateTime(LocalDateTime.now());
        stock.setUpdateTime(LocalDateTime.now());
        wmMaterialStockMapper.insert(stock);
        return stock.getMaterialStockId();
    }

    /**
     * 出库式的"分配 + 扣减"
     *
     * 先定位候选库存行：
     *   · 行里指定了 material_stock_id → 就用它（并校验物料对得上）
     *   · 没指定 → 按 物料 + 批次 + 仓库/库位 查可出库的行，按入库时间升序（先进先出）
     * 然后按顺序把需求数量分掉了，不够就抛异常 —— 整个事务回滚，一行数据都不会动。
     *
     * @return 拆好的落位明细（还没入库），每条对应一个库位
     */
    private List<WmDocDetail> allocateAndReduce(WmDoc doc, WmDocLine line, StockSlot slot) {
        BigDecimal need = line.getQuantity();
        if (need == null || need.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("物料[" + line.getItemCode() + "]的数量必须大于 0");
        }

        List<WmMaterialStock> candidates;
        if (line.getMaterialStockId() != null) {
            WmMaterialStock one = wmMaterialStockMapper.selectById(line.getMaterialStockId());
            if (one == null) {
                throw new BusinessException("物料[" + line.getItemCode()
                        + "]指定的库存记录不存在或已被删除，请重新选择库位");
            }
            if (!one.getItemId().equals(line.getItemId())) {
                throw new BusinessException("指定的库存记录与行上的物料不是同一个，请重新选择库位");
            }
            candidates = Collections.singletonList(one);
        } else {
            WmMaterialStock query = new WmMaterialStock();
            query.setItemId(line.getItemId());
            query.setBatchId(line.getBatchId());
            query.setWarehouseId(slot.warehouseId);
            query.setLocationId(slot.locationId);
            query.setAreaId(slot.areaId);
            candidates = wmMaterialStockMapper.selectAvailableForOutbound(query);
        }

        BigDecimal remain = need;
        BigDecimal totalAvailable = BigDecimal.ZERO;
        List<WmDocDetail> details = new ArrayList<>();

        for (WmMaterialStock candidate : candidates) {
            if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal available = nvlDecimal(candidate.getQuantityAvailable());
            if (available.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            totalAvailable = totalAvailable.add(available);
            BigDecimal take = available.min(remain);

            // 数据库层再判一次可用量：返回 0 行 = 这一瞬间被别人出走了
            if (wmMaterialStockMapper.reduceOnhand(candidate.getMaterialStockId(), take) == 0) {
                throw new BusinessException("物料[" + line.getItemCode() + " " + line.getItemName()
                        + "]的可用库存刚刚被别人占用了，本次出库失败，请刷新后重试");
            }

            // 落位记录用的是"货实际在哪儿"（库存行所在库位），不是行上填的建议库位
            StockSlot actual = new StockSlot();
            actual.warehouseId = candidate.getWarehouseId();
            actual.warehouseCode = candidate.getWarehouseCode();
            actual.warehouseName = candidate.getWarehouseName();
            actual.locationId = candidate.getLocationId();
            actual.locationCode = candidate.getLocationCode();
            actual.locationName = candidate.getLocationName();
            actual.areaId = candidate.getAreaId();
            actual.areaCode = candidate.getAreaCode();
            actual.areaName = candidate.getAreaName();

            details.add(buildDetail(doc, line, actual, take, candidate.getMaterialStockId()));
            remain = remain.subtract(take);
        }

        if (remain.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("物料[" + line.getItemCode() + " " + line.getItemName()
                    + "]可用库存不足：本次需要 " + strip(need) + "，仓库里可分配的只有 "
                    + strip(totalAvailable) + "，还差 " + strip(remain)
                    + "。请先入库或调整本次数量");
        }
        return details;
    }

    // ====================================================================
    // 十、流水
    // ====================================================================

    /**
     * 写一条库存流水
     *
     * @param flag 1 入库 / -1 出库（数量永远正数，方向看这个）
     * @return 事务ID（调拨配对付调用）
     */
    private Long writeTransaction(WmDoc doc, WmDocLine line, WmDocDetail detail,
                                  int flag, BigDecimal quantity, Long stockId) {
        WmTransaction trx = new WmTransaction();
        trx.setTransactionType(doc.getDocType());
        trx.setItemId(line.getItemId());
        trx.setItemCode(detail.getItemCode());
        trx.setItemName(detail.getItemName());
        trx.setSpecification(detail.getSpecification());
        trx.setUnitOfMeasure(detail.getUnitOfMeasure());
        trx.setUnitName(detail.getUnitName());
        trx.setBatchId(detail.getBatchId());
        trx.setBatchCode(detail.getBatchCode());
        trx.setWarehouseId(detail.getWarehouseId());
        trx.setWarehouseCode(detail.getWarehouseCode());
        trx.setWarehouseName(detail.getWarehouseName());
        trx.setLocationId(detail.getLocationId());
        trx.setLocationCode(detail.getLocationCode());
        trx.setLocationName(detail.getLocationName());
        trx.setAreaId(detail.getAreaId());
        trx.setAreaCode(detail.getAreaCode());
        trx.setAreaName(detail.getAreaName());
        trx.setSourceDocType(SOURCE_DOC_TABLE_WM_DOC);
        trx.setSourceDocId(doc.getDocId());
        trx.setSourceDocCode(doc.getDocCode());
        trx.setSourceDocLineId(line.getLineId());
        trx.setMaterialStockId(stockId);
        trx.setTransactionFlag(flag);
        trx.setTransactionQuantity(quantity);
        trx.setTransactionDate(LocalDateTime.now());
        trx.setRecptDate(LocalDateTime.now());
        trx.setCreateBy(DEFAULT_OPERATOR);
        trx.setUpdateBy(DEFAULT_OPERATOR);
        trx.setCreateTime(LocalDateTime.now());
        trx.setUpdateTime(LocalDateTime.now());
        wmTransactionMapper.insert(trx);
        return trx.getTransactionId();
    }

    // ====================================================================
    // 十一、回写 A 线投料（生产领料单专属）
    // ====================================================================

    /**
     * 生产领料单过账成功后，把每一行领料翻译成一条投料记录写进 pro_task_issue
     *
     * 这是 A/B 两条线唯一的接口：
     *   仓库这边货真的出去了 → 生产那边必须看得到"这个任务领到了多少料"，
     *   否则任务执行层永远不知道自己有没有料。
     * 写成投料记录而不是只改个字段，是为了留证据：哪张领料单、哪一行、哪个批次。
     */
    private void writeTaskIssue(WmDoc doc, List<WmDocLine> lines) {
        // 兜底：同一张领料单不允许产生两批投料（过账本身已有 status 卡住，这里是双保险）
        if (proTaskIssueMapper.countBySourceDocId(SOURCE_DOC_TABLE_WM_DOC, doc.getDocId()) > 0) {
            return;
        }
        for (WmDocLine line : lines) {
            ProTaskIssue issue = new ProTaskIssue();
            issue.setTaskId(doc.getTaskId());
            issue.setWorkorderId(doc.getWorkorderId());
            issue.setWorkstationId(doc.getWorkstationId());
            issue.setSourceDocTable(SOURCE_DOC_TABLE_WM_DOC);
            issue.setSourceDocId(doc.getDocId());
            issue.setSourceDocType(doc.getDocType());
            issue.setSourceDocCode(doc.getDocCode());
            issue.setSourceLineId(line.getLineId());
            issue.setBatchCode(line.getBatchCode());
            issue.setItemId(line.getItemId());
            issue.setItemCode(line.getItemCode());
            issue.setItemName(line.getItemName());
            issue.setSpecification(line.getSpecification());
            issue.setUnitOfMeasure(line.getUnitOfMeasure());
            // 刚投料：可用 = 投料量，已用 = 0（恒等式 available + used = issued）
            issue.setQuantityIssued(line.getQuantity());
            issue.setQuantityAvailable(line.getQuantity());
            issue.setQuantityUsed(BigDecimal.ZERO);
            issue.setRemark("由生产领料单 " + doc.getDocCode() + " 过账自动生成");
            issue.setCreateBy(DEFAULT_OPERATOR);
            issue.setUpdateBy(DEFAULT_OPERATOR);
            issue.setCreateTime(LocalDateTime.now());
            issue.setUpdateTime(LocalDateTime.now());
            proTaskIssueMapper.insert(issue);
        }
    }

    // ====================================================================
    // 十二、校验（L3：后端二次校验 + 规则拒绝）
    // ====================================================================

    /**
     * 单据类型必须在 14 类里。前端传个乱七八糟的值进来，后面 io_flag 推导会得到 null，
     * 方向判定就全乱了，所以在最前面拦住。
     */
    private void checkDocType(String docType) {
        if (isBlank(docType)) {
            throw new BusinessException("单据类型不能为空");
        }
        if (!DOC_TYPE_IO_FLAG.containsKey(docType)) {
            throw new BusinessException("单据类型[" + docType + "]不是有效的出入库单据类型");
        }
    }

    /**
     * 跨字段业务规则：来源类型决定哪些字段必填
     */
    private void checkBizRules(WmDoc doc) {
        String type = doc.getDocType();
        // 1) 任何单据都要有仓库：出库要知道从哪出，入库要知道入到哪
        if (doc.getWarehouseId() == null) {
            throw new BusinessException("请选择仓库");
        }
        // 2) 调拨：必须有目标仓库，且与源仓库不同（原地调拨没有意义，多半是选错了）
        if (TYPE_TRANSFER.equals(type)) {
            if (doc.getToWarehouseId() == null) {
                throw new BusinessException("调拨单必须选择目标仓库");
            }
            if (doc.getToWarehouseId().equals(doc.getWarehouseId())) {
                throw new BusinessException("调拨单的源仓库与目标仓库不能相同");
            }
        }
        // 3) 生产领料：必须挂生产任务 —— 过账后要把投料记到任务上，没有任务就没地方记
        if (TYPE_ISSUE.equals(type) && doc.getTaskId() == null) {
            throw new BusinessException("生产领料单必须选择生产任务（过账后要把投料记到任务上）");
        }
        // 4) 生产入库 / 产品入库：必须挂生产工单，否则追溯不到成品是哪张单产出的
        if ((TYPE_PRODUCT_PRODUCE.equals(type) || TYPE_PRODUCT_RECPT.equals(type))
                && doc.getWorkorderId() == null) {
            throw new BusinessException("该单据必须选择生产工单，否则成品追溯不到来源");
        }
    }

    /** 单据头 + 行的整体校验（新增场景） */
    private void checkLineList(WmDoc doc) {
        List<WmDocLine> lines = doc.getLineList();
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("请至少添加一行明细");
        }
        for (int i = 0; i < lines.size(); i++) {
            WmDocLine line = lines.get(i);
            if (line.getItemId() == null) {
                throw new BusinessException("第 " + (i + 1) + " 行：请选择物料");
            }
            if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("第 " + (i + 1) + " 行：数量必须大于 0");
            }
        }
    }

    /**
     * 出库类必填批次：物料开了批次管理，出库却不说是哪个批次，
     * 那这货出的是哪一批就说不清了（先进先出也就无从谈起）。
     */
    private void checkBatchRequiredForOutbound(WmDocLine line, MdItem item) {
        if (!"Y".equals(item.getBatchFlag())) {
            return;
        }
        if (line.getBatchId() == null) {
            throw new BusinessException("物料[" + item.getItemCode() + " " + item.getItemName()
                    + "]启用了批次管理，出库必须指定批次");
        }
    }

    /** 入库类必填批次（与出库同理，口径保持一致） */
    private void checkLineBatchRule(WmDocLine line, MdItem item) {
        if (!"Y".equals(item.getBatchFlag())) {
            return;
        }
        if (line.getBatchId() == null) {
            throw new BusinessException("物料[" + item.getItemCode() + " " + item.getItemName()
                    + "]启用了批次管理，入库必须指定批次");
        }
    }

    private void requireWarehouse(StockSlot slot, WmDocLine line) {
        if (slot.warehouseId == null) {
            throw new BusinessException("物料[" + nvlStr(line.getItemCode())
                    + "]没有指定仓库，无法确定货物位置");
        }
    }

    private WmDoc checkExists(Long docId) {
        if (docId == null) {
            throw new BusinessException("单据ID不能为空");
        }
        WmDoc db = wmDocMapper.selectById(docId);
        if (db == null) {
            throw new BusinessException("单据不存在或已被删除");
        }
        return db;
    }

    private WmDocLine checkLineExists(Long lineId) {
        if (lineId == null) {
            throw new BusinessException("明细行ID不能为空");
        }
        WmDocLine db = wmDocLineMapper.selectById(lineId);
        if (db == null) {
            throw new BusinessException("明细行不存在或已被删除");
        }
        return db;
    }

    private void checkLineQuantity(WmDocLine line) {
        if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("数量必须大于 0");
        }
    }

    /** 只有待过账的单据能改/删 */
    private void checkStatusCanModify(WmDoc db, String action) {
        if (!ST_PREPARE.equals(db.getStatus())) {
            throw new BusinessException("单据[" + db.getDocCode() + "]当前状态为【"
                    + statusText(db.getStatus()) + "】，只有【待过账】的单据才能" + action);
        }
    }

    // ====================================================================
    // 十三、内部小工具
    // ====================================================================

    /** 供校验用的合并视图：库里已有值打底，入参的非空值覆盖上去 */
    private WmDoc mergeForCheck(WmDoc db, WmDoc param) {
        WmDoc merged = new WmDoc();
        merged.setDocId(db.getDocId());
        merged.setDocType(db.getDocType());
        merged.setIoFlag(db.getIoFlag());
        merged.setWarehouseId(param.getWarehouseId() != null ? param.getWarehouseId() : db.getWarehouseId());
        merged.setLocationId(param.getLocationId() != null ? param.getLocationId() : db.getLocationId());
        merged.setAreaId(param.getAreaId() != null ? param.getAreaId() : db.getAreaId());
        merged.setToWarehouseId(param.getToWarehouseId() != null ? param.getToWarehouseId() : db.getToWarehouseId());
        merged.setToLocationId(param.getToLocationId() != null ? param.getToLocationId() : db.getToLocationId());
        merged.setToAreaId(param.getToAreaId() != null ? param.getToAreaId() : db.getToAreaId());
        merged.setTaskId(param.getTaskId() != null ? param.getTaskId() : db.getTaskId());
        merged.setWorkorderId(param.getWorkorderId() != null ? param.getWorkorderId() : db.getWorkorderId());
        return merged;
    }

    /**
     * 把仓库/库区/库位的编码与名称补齐（按 ID 现查）
     *
     * 为什么不直接信前端传的编码名称？因为前端可能传了个过期快照，
     * 或者干脆伪造。现查一次，落库的快照就是对的。
     */
    private void fillDocLocationSnapshot(WmDoc doc) {
        if (doc.getWarehouseId() != null) {
            WmWarehouse warehouse = wmWarehouseMapper.selectById(doc.getWarehouseId());
            if (warehouse != null) {
                doc.setWarehouseCode(warehouse.getWarehouseCode());
                doc.setWarehouseName(warehouse.getWarehouseName());
            }
        }
        if (doc.getLocationId() != null) {
            WmLocation location = wmLocationMapper.selectById(doc.getLocationId());
            if (location != null) {
                doc.setLocationCode(location.getLocationCode());
                doc.setLocationName(location.getLocationName());
                if (doc.getAreaId() == null && location.getParentId() != null
                        && location.getParentId() != 0L) {
                    doc.setAreaId(location.getParentId());
                }
            }
        }
        if (doc.getAreaId() != null) {
            WmLocation area = wmLocationMapper.selectById(doc.getAreaId());
            if (area != null) {
                doc.setAreaCode(area.getLocationCode());
                doc.setAreaName(area.getLocationName());
            }
        }
        // 目标仓库（调拨单）
        if (doc.getToWarehouseId() != null) {
            WmWarehouse warehouse = wmWarehouseMapper.selectById(doc.getToWarehouseId());
            if (warehouse != null) {
                doc.setToWarehouseCode(warehouse.getWarehouseCode());
                doc.setToWarehouseName(warehouse.getWarehouseName());
            }
        }
        if (doc.getToLocationId() != null) {
            WmLocation location = wmLocationMapper.selectById(doc.getToLocationId());
            if (location != null) {
                doc.setToLocationCode(location.getLocationCode());
                doc.setToLocationName(location.getLocationName());
                if (doc.getToAreaId() == null && location.getParentId() != null
                        && location.getParentId() != 0L) {
                    doc.setToAreaId(location.getParentId());
                }
            }
        }
        if (doc.getToAreaId() != null) {
            WmLocation area = wmLocationMapper.selectById(doc.getToAreaId());
            if (area != null) {
                doc.setToAreaCode(area.getLocationCode());
                doc.setToAreaName(area.getLocationName());
            }
        }
    }

    /** 行上的"建议库位"优先，没填就回落到单据头 */
    private StockSlot resolveSlot(WmDocLine line, WmDoc doc) {
        StockSlot slot = new StockSlot();
        slot.warehouseId = line.getWarehouseId() != null ? line.getWarehouseId() : doc.getWarehouseId();
        slot.locationId = line.getLocationId() != null ? line.getLocationId() : doc.getLocationId();
        slot.areaId = line.getAreaId() != null ? line.getAreaId() : doc.getAreaId();
        fillSlotNames(slot);
        return slot;
    }

    private void fillSlotNames(StockSlot slot) {
        if (slot.warehouseId != null) {
            WmWarehouse warehouse = wmWarehouseMapper.selectById(slot.warehouseId);
            if (warehouse != null) {
                slot.warehouseCode = warehouse.getWarehouseCode();
                slot.warehouseName = warehouse.getWarehouseName();
            }
        }
        if (slot.locationId != null) {
            WmLocation location = wmLocationMapper.selectById(slot.locationId);
            if (location != null) {
                slot.locationCode = location.getLocationCode();
                slot.locationName = location.getLocationName();
                if (slot.areaId == null && location.getParentId() != null && location.getParentId() != 0L) {
                    slot.areaId = location.getParentId();
                }
            }
        }
        if (slot.areaId != null) {
            WmLocation area = wmLocationMapper.selectById(slot.areaId);
            if (area != null) {
                slot.areaCode = area.getLocationCode();
                slot.areaName = area.getLocationName();
            }
        }
    }

    /** 行上的库位没填时，用单据头的兜底（保证行上永远能看出"默认从哪出/入到哪"） */
    private void fillLineDefaults(WmDocLine line, WmDoc doc) {
        if (line.getWarehouseId() == null) {
            line.setWarehouseId(doc.getWarehouseId());
            line.setWarehouseCode(doc.getWarehouseCode());
            line.setWarehouseName(doc.getWarehouseName());
        }
        if (line.getLocationId() == null) {
            line.setLocationId(doc.getLocationId());
            line.setLocationCode(doc.getLocationCode());
            line.setLocationName(doc.getLocationName());
        }
        if (line.getAreaId() == null) {
            line.setAreaId(doc.getAreaId());
            line.setAreaCode(doc.getAreaCode());
            line.setAreaName(doc.getAreaName());
        }
    }

    /** 整批插入行 */
    private void insertLines(WmDoc doc) {
        List<WmDocLine> lines = doc.getLineList();
        if (lines == null || lines.isEmpty()) {
            return;
        }
        int lineNo = 0;
        for (WmDocLine line : lines) {
            MdItem item = loadItem(line.getItemId());
            fillLineItemSnapshot(line, item);
            line.setDocId(doc.getDocId());
            line.setDocType(doc.getDocType());
            line.setLineNo(++lineNo);
            fillLineDefaults(line, doc);
            if (line.getQcFlag() == null) {
                line.setQcFlag("N");
            }
            if (line.getBatchId() != null) {
                line.setBatchCode(resolveBatchCode(line));
            }
            line.setCreateBy(DEFAULT_OPERATOR);
            line.setUpdateBy(DEFAULT_OPERATOR);
            line.setCreateTime(LocalDateTime.now());
            line.setUpdateTime(LocalDateTime.now());
            wmDocLineMapper.insert(line);
        }
    }

    private MdItem loadItem(Long itemId) {
        if (itemId == null) {
            throw new BusinessException("物料不能为空");
        }
        MdItem item = mdItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物料不存在或已被删除（物料ID：" + itemId + "）");
        }
        if (!"Y".equals(item.getEnableFlag())) {
            throw new BusinessException("物料[" + item.getItemCode() + " " + item.getItemName()
                    + "]已停用，不能用于出入库");
        }
        return item;
    }

    /** 物料快照一律现查回填，不信前端传的 */
    private void fillLineItemSnapshot(WmDocLine line, MdItem item) {
        line.setItemCode(item.getItemCode());
        line.setItemName(item.getItemName());
        line.setSpecification(item.getSpecification());
        line.setUnitOfMeasure(item.getUnitOfMeasure());
        line.setUnitName(item.getUnitName());
    }

    /**
     * 过账时刷新行上的物料快照并落库
     *
     * 单据可能是三天前建的，这三天里物料改了名字或规格 ——
     * 落位明细和流水都从行上取快照，所以过账这一刻必须刷成最新的，
     * 否则流水里记的还是旧名字，事后追溯会被误导。
     */
    private void refreshLineSnapshot(WmDocLine line, MdItem item) {
        fillLineItemSnapshot(line, item);
        wmDocLineMapper.updateItemSnapshot(line);
    }

    /** 批次号一律从 wm_batch 现查，保证与批次档案一致 */
    private String resolveBatchCode(WmDocLine line) {
        if (line.getBatchId() == null) {
            return line.getBatchCode();
        }
        WmBatch batch = wmBatchMapper.selectById(line.getBatchId());
        if (batch == null) {
            throw new BusinessException("行上的批次不存在或已被删除（批次ID：" + line.getBatchId() + "）");
        }
        line.setBatchCode(batch.getBatchCode());
        if (line.getProduceDate() == null) {
            line.setProduceDate(batch.getProduceDate());
        }
        if (line.getExpireDate() == null) {
            line.setExpireDate(batch.getExpireDate());
        }
        return batch.getBatchCode();
    }

    /** 组装落位明细（数量为正数，方向由单据头的 io_flag 决定） */
    private WmDocDetail buildDetail(WmDoc doc, WmDocLine line, StockSlot slot,
                                    BigDecimal quantity, Long stockId) {
        WmDocDetail detail = new WmDocDetail();
        detail.setDocId(doc.getDocId());
        detail.setDocType(doc.getDocType());
        detail.setLineId(line.getLineId());
        detail.setMaterialStockId(stockId);
        detail.setItemId(line.getItemId());
        detail.setItemCode(line.getItemCode());
        detail.setItemName(line.getItemName());
        detail.setSpecification(line.getSpecification());
        detail.setUnitOfMeasure(line.getUnitOfMeasure());
        detail.setUnitName(line.getUnitName());
        detail.setQuantity(quantity);
        detail.setBatchId(line.getBatchId());
        detail.setBatchCode(line.getBatchCode());
        detail.setWarehouseId(slot.warehouseId);
        detail.setWarehouseCode(slot.warehouseCode);
        detail.setWarehouseName(slot.warehouseName);
        detail.setLocationId(slot.locationId);
        detail.setLocationCode(slot.locationCode);
        detail.setLocationName(slot.locationName);
        detail.setAreaId(slot.areaId);
        detail.setAreaCode(slot.areaCode);
        detail.setAreaName(slot.areaName);
        detail.setCreateBy(DEFAULT_OPERATOR);
        detail.setUpdateBy(DEFAULT_OPERATOR);
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());
        return detail;
    }

    private String buildDefaultDocName(String docType) {
        return "WM-" + docType + "-" + LocalDateTime.now().toLocalDate();
    }

    private String statusText(String status) {
        if (ST_PREPARE.equals(status)) return "待过账";
        if (ST_CONFIRMED.equals(status)) return "已过账";
        if (ST_CANCELED.equals(status)) return "已取消";
        if ("FINISHED".equals(status)) return "已完成";
        return status;
    }

    private boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }

    private String nvlStr(String str) {
        return str == null ? "" : str;
    }

    private Long nvlLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal nvlDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** 去掉小数末尾多余的 0，报错信息里好看一点（100.000000 → 100） */
    private String strip(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * 落位（一个仓库 + 库区 + 库位的组合）
     * 抽出来是因为入库、出库、调拨三处都要用，参数少传几个不容易错。
     */
    private static class StockSlot {
        private Long warehouseId;
        private String warehouseCode;
        private String warehouseName;
        private Long locationId;
        private String locationCode;
        private String locationName;
        private Long areaId;
        private String areaCode;
        private String areaName;
    }
}
