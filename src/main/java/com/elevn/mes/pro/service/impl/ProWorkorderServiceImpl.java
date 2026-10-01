package com.elevn.mes.pro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.entity.MdProductBom;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.md.mapper.MdProductBomMapper;
import com.elevn.mes.pro.entity.ProRouteProduct;
import com.elevn.mes.pro.entity.ProRouteProductBom;
import com.elevn.mes.pro.entity.ProWorkorder;
import com.elevn.mes.pro.entity.ProWorkorderBom;
import com.elevn.mes.pro.mapper.ProRouteProductBomMapper;
import com.elevn.mes.pro.mapper.ProRouteProductMapper;
import com.elevn.mes.pro.mapper.ProWorkorderBomMapper;
import com.elevn.mes.pro.mapper.ProWorkorderMapper;
import com.elevn.mes.pro.service.ProWorkorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生产工单 Service 实现
 *
 * L3 业务校验（这是工单模块的分水岭，不是"能存进去"就算完）：
 *
 * 一、录入时的交叉校验 —— 三个维度互相约束
 *   1. 来源类型 ORDER 客户订单 → 订单编号必填 + 客户必选
 *      来源类型 STORE 库存备货 → 订单编号与客户都必须为空（防止脏数据）
 *   2. 工单类型 OUTSOURCE/PURCHASE 外协外购 → 供应商必选
 *      工单类型 SELF 自产 → 供应商必须为空
 *   3. 产品必须存在且启用，冗余字段（编码/名称/规格/单位）由后端回填，不信前端传的
 *
 * 二、状态机校验 —— 状态不对什么都不让干
 *   修改/删除：只有 PREPARE 待下达可以
 *   下达：只有 PREPARE 可以，下达后业务字段冻结
 *   完工：只有 CONFIRMED 可以
 *   取消：PREPARE / CONFIRMED 可以，已完工已取消不能再取消
 *
 * 三、下达时的完整性校验
 *   自产工单（SELF）必须已经在产品制程（pro_route_product）里挂过工艺路线，
 *   否则排产时拆不出工序任务 —— 这个错必须在下达时就拦住，不能拖到排产。
 *
 * 四、BOM 展开
 *   用料来源**优先制程BOM**（pro_route_product_bom），没挂制程的工单才退回产品BOM（md_product_bom）。
 *   预计使用量 = 单位用量合计 × 工单生产数量（用 BigDecimal 算，避免浮点误差）
 *
 *   为什么必须优先制程BOM：报工倒冲消耗取的就是制程BOM，工单用料账若还是产品BOM，
 *   两边不同源 —— 制程上补录的工艺辅料（如冷轧钢板、铝型材）在工单账里就是 0，
 *   比对页会出现「应耗 0 / 实耗 216」的假超耗。同源才能比对。
 *
 */
@Service
public class ProWorkorderServiceImpl implements ProWorkorderService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 状态常量：待下达 */
    private static final String STATUS_PREPARE = "PREPARE";
    /** 状态常量：已下达 */
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    /** 状态常量：已完工 */
    private static final String STATUS_FINISHED = "FINISHED";
    /** 状态常量：已取消 */
    private static final String STATUS_CANCELED = "CANCELED";

    /** 来源类型：客户订单 */
    private static final String SOURCE_ORDER = "ORDER";
    /** 来源类型：库存备货 */
    private static final String SOURCE_STORE = "STORE";

    /** 工单类型：自产 */
    private static final String TYPE_SELF = "SELF";

    @Autowired
    private ProWorkorderMapper proWorkorderMapper;

    @Autowired
    private ProWorkorderBomMapper proWorkorderBomMapper;

    @Autowired
    private ProRouteProductMapper proRouteProductMapper;

    @Autowired
    private MdProductBomMapper mdProductBomMapper;

    @Autowired
    private ProRouteProductBomMapper proRouteProductBomMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Override
    public PageInfo<ProWorkorder> page(int pageNum, int pageSize, ProWorkorder proWorkorder) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProWorkorder> list = proWorkorderMapper.selectByCondition(proWorkorder);
        return new PageInfo<>(list);
    }

    @Override
    public ProWorkorder queryById(Long id) {
        // BOM 明细由前端 Tab 单独拉取（queryBomList），这里只回主表
        return proWorkorderMapper.selectById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(ProWorkorder proWorkorder) {
        checkCodeUnique(proWorkorder);
        checkCrossField(proWorkorder);
        fillProductSnapshot(proWorkorder);

        // 状态一律由后端定，不信前端传的值
        proWorkorder.setStatus(STATUS_PREPARE);
        // 累计数量从 0 起，防止前端伪造一个"已生产 100 台"
        proWorkorder.setQuantityProduced(BigDecimal.ZERO);
        proWorkorder.setQuantityScheduled(BigDecimal.ZERO);
        proWorkorder.setQuantityChanged(BigDecimal.ZERO);
        // 父工单相关：本模块暂不拆单，顶层工单固定 0 / '0'
        if (proWorkorder.getParentId() == null) {
            proWorkorder.setParentId(0L);
        }
        if (proWorkorder.getAncestors() == null || "".equals(proWorkorder.getAncestors())) {
            proWorkorder.setAncestors("0");
        }
        proWorkorder.setCreateBy(DEFAULT_OPERATOR);
        proWorkorder.setUpdateBy(DEFAULT_OPERATOR);
        proWorkorder.setCreateTime(LocalDateTime.now());
        proWorkorder.setUpdateTime(LocalDateTime.now());
        return proWorkorderMapper.insert(proWorkorder);
    }

    @Override
    public int updateById(ProWorkorder proWorkorder) {
        ProWorkorder db = proWorkorderMapper.selectById(proWorkorder.getWorkorderId());
        if (db == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        // 状态机：只有待下达能改
        if (!STATUS_PREPARE.equals(db.getStatus())) {
            throw new BusinessException("工单[" + db.getWorkorderCode() + "]当前状态为"
                    + statusName(db.getStatus()) + "，只有待下达的工单可以修改");
        }
        if (proWorkorder.getWorkorderCode() != null && !"".equals(proWorkorder.getWorkorderCode())) {
            checkCodeUnique(proWorkorder);
        }
        checkCrossField(proWorkorder);
        fillProductSnapshot(proWorkorder);

        proWorkorder.setUpdateBy(DEFAULT_OPERATOR);
        proWorkorder.setUpdateTime(LocalDateTime.now());
        return proWorkorderMapper.updateById(proWorkorder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteById(Long id) {
        checkDeletable(id);
        // BOM 展开行跟着工单一起清掉，避免留下孤儿行
        proWorkorderBomMapper.deleteByWorkorderId(id);
        return proWorkorderMapper.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        for (Long id : ids) {
            checkDeletable(id);
        }
        int rows = proWorkorderMapper.deleteBatch(ids);
        for (Long id : ids) {
            proWorkorderBomMapper.deleteByWorkorderId(id);
        }
        return rows;
    }

    @Override
    public ProWorkorder queryByWorkorderCode(String workorderCode) {
        return proWorkorderMapper.selectByWorkorderCode(workorderCode);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int confirm(Long workorderId) {
        ProWorkorder db = proWorkorderMapper.selectById(workorderId);
        if (db == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        if (!STATUS_PREPARE.equals(db.getStatus())) {
            throw new BusinessException("工单[" + db.getWorkorderCode() + "]当前状态为"
                    + statusName(db.getStatus()) + "，只有待下达的工单可以下达");
        }
        // 自产工单必须已挂工艺路线，否则排产时拆不出工序任务
        if (TYPE_SELF.equals(db.getWorkorderType())) {
            List<ProRouteProduct> routes = proRouteProductMapper.selectByItemId(db.getProductId());
            if (routes == null || routes.isEmpty()) {
                throw new BusinessException("自产工单的产品[" + db.getProductName()
                        + "]还没有配置产品制程（工艺路线），无法下达。"
                        + "请先到 生产管理-产品制程 里为该产品挂一条工艺路线");
            }
        }
        // 改状态 + 展开BOM
        proWorkorderMapper.updateStatus(workorderId, STATUS_CONFIRMED, null, null, DEFAULT_OPERATOR);
        return expandBom(workorderId);
    }

    @Override
    public int finish(Long workorderId) {
        ProWorkorder db = proWorkorderMapper.selectById(workorderId);
        if (db == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        if (!STATUS_CONFIRMED.equals(db.getStatus())) {
            throw new BusinessException("工单[" + db.getWorkorderCode() + "]当前状态为"
                    + statusName(db.getStatus()) + "，只有已下达的工单可以完工");
        }
        return proWorkorderMapper.updateStatus(workorderId, STATUS_FINISHED,
                LocalDateTime.now(), null, DEFAULT_OPERATOR);
    }

    @Override
    public int cancel(Long workorderId) {
        ProWorkorder db = proWorkorderMapper.selectById(workorderId);
        if (db == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        if (STATUS_FINISHED.equals(db.getStatus())) {
            throw new BusinessException("工单[" + db.getWorkorderCode() + "]已完工，不能取消");
        }
        if (STATUS_CANCELED.equals(db.getStatus())) {
            throw new BusinessException("工单[" + db.getWorkorderCode() + "]已经是取消状态，无需重复操作");
        }
        return proWorkorderMapper.updateStatus(workorderId, STATUS_CANCELED,
                null, LocalDateTime.now(), DEFAULT_OPERATOR);
    }

    @Override
    public List<ProWorkorderBom> queryBomList(Long workorderId) {
        return proWorkorderBomMapper.selectByWorkorderId(workorderId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int rebuildBom(Long workorderId) {
        ProWorkorder db = proWorkorderMapper.selectById(workorderId);
        if (db == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        // 已完工/已取消的单子重算用料没有意义，反而会打乱历史
        if (STATUS_FINISHED.equals(db.getStatus()) || STATUS_CANCELED.equals(db.getStatus())) {
            throw new BusinessException("工单[" + db.getWorkorderCode() + "]当前状态为"
                    + statusName(db.getStatus()) + "，不能重算用料");
        }
        return expandBom(workorderId);
    }

    // ============================================================
    // 私有方法
    // ============================================================

    /**
     * 按下拉选择的产品，回填产品快照字段
     *
     * 冗余字段一律从 md_item 现查后回填，不信前端传过来的编码/名称/规格。
     * 前端传错一个产品名称，就会导致工单和物料主数据对不上、报表统计串行。
     */
    private void fillProductSnapshot(ProWorkorder proWorkorder) {
        if (proWorkorder.getProductId() == null) {
            throw new BusinessException("请选择生产的产品");
        }
        MdItem item = mdItemMapper.selectById(proWorkorder.getProductId());
        if (item == null) {
            throw new BusinessException("所选产品不存在或已被删除");
        }
        if (!"Y".equals(item.getEnableFlag())) {
            throw new BusinessException("产品[" + item.getItemName() + "]已停用，不能开工单");
        }
        proWorkorder.setProductCode(item.getItemCode());
        proWorkorder.setProductName(item.getItemName());
        proWorkorder.setProductSpc(item.getSpecification());
        proWorkorder.setUnitOfMeasure(item.getUnitOfMeasure());
    }

    /**
     * 三个维度的交叉校验（L3 核心）
     *
     * 注意：修改工单时前端可能只传部分字段（比如只改备注），
     * 所以这里对 null 的字段要"回填数据库里已有的值"再校验，
     * 否则会出现"只改备注却报来源类型为空"这种莫名其妙的错。
     */
    private void checkCrossField(ProWorkorder proWorkorder) {
        // 修改场景：把数据库里的值补齐到未传的字段上
        if (proWorkorder.getWorkorderId() != null) {
            ProWorkorder db = proWorkorderMapper.selectById(proWorkorder.getWorkorderId());
            if (db != null) {
                if (proWorkorder.getOrderSource() == null) {
                    proWorkorder.setOrderSource(db.getOrderSource());
                }
                if (proWorkorder.getWorkorderType() == null) {
                    proWorkorder.setWorkorderType(db.getWorkorderType());
                }
                if (proWorkorder.getSourceCode() == null) {
                    proWorkorder.setSourceCode(db.getSourceCode());
                }
                if (proWorkorder.getClientId() == null) {
                    proWorkorder.setClientId(db.getClientId());
                    proWorkorder.setClientCode(db.getClientCode());
                    proWorkorder.setClientName(db.getClientName());
                }
                if (proWorkorder.getVendorId() == null) {
                    proWorkorder.setVendorId(db.getVendorId());
                    proWorkorder.setVendorCode(db.getVendorCode());
                    proWorkorder.setVendorName(db.getVendorName());
                }
            }
        }

        String source = proWorkorder.getOrderSource();
        String type = proWorkorder.getWorkorderType();

        // ---- 规则一：来源类型决定订单编号与客户 ----
        if (SOURCE_ORDER.equals(source)) {
            if (isBlank(proWorkorder.getSourceCode())) {
                throw new BusinessException("来源类型为「客户订单」时，订单编号不能为空");
            }
            if (proWorkorder.getClientId() == null) {
                throw new BusinessException("来源类型为「客户订单」时，必须选择客户");
            }
        } else if (SOURCE_STORE.equals(source)) {
            // 备货单挂订单号或客户，会造成"到底有没有客户"的口径混乱，直接拒绝
            if (!isBlank(proWorkorder.getSourceCode())) {
                throw new BusinessException("来源类型为「库存备货」时，不需要填订单编号");
            }
            if (proWorkorder.getClientId() != null) {
                throw new BusinessException("来源类型为「库存备货」时，不需要选择客户");
            }
        }

        // ---- 规则二：工单类型决定供应商 ----
        if (TYPE_SELF.equals(type)) {
            if (proWorkorder.getVendorId() != null) {
                throw new BusinessException("工单类型为「自产」时，不需要选择供应商");
            }
        } else if ("OUTSOURCE".equals(type) || "PURCHASE".equals(type)) {
            if (proWorkorder.getVendorId() == null) {
                throw new BusinessException("工单类型为「"
                        + ("OUTSOURCE".equals(type) ? "外协" : "外购") + "」时，必须选择供应商");
            }
        }
    }

    /**
     * 校验编码是否重复
     */
    private void checkCodeUnique(ProWorkorder proWorkorder) {
        if (isBlank(proWorkorder.getWorkorderCode())) {
            throw new BusinessException("工单编码不能为空");
        }
        ProWorkorder db = proWorkorderMapper.selectByWorkorderCode(proWorkorder.getWorkorderCode());
        if (db == null) {
            return;
        }
        if (proWorkorder.getWorkorderId() != null
                && proWorkorder.getWorkorderId().equals(db.getWorkorderId())) {
            return;
        }
        throw new BusinessException("工单编码已存在：" + proWorkorder.getWorkorderCode());
    }

    /**
     * 删除前校验：只有待下达能删
     */
    private void checkDeletable(Long workorderId) {
        ProWorkorder db = proWorkorderMapper.selectById(workorderId);
        if (db == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }
        if (!STATUS_PREPARE.equals(db.getStatus())) {
            throw new BusinessException("工单[" + db.getWorkorderCode() + "]当前状态为"
                    + statusName(db.getStatus()) + "，只有待下达的工单可以删除。"
                    + "已下达的工单请走「取消」");
        }
    }

    /**
     * 展开工单用料（写入 pro_workorder_bom）
     *
     * 整批替换式：先按工单ID物理清空旧行，再重新展开一遍。
     * 这样重复下达/重算得到的结果都一样（幂等），也不会残留脏行。
     *
     * 用料来源分两档（同源优先、降级兜底）：
     *   1. 产品已挂制程 → 用**制程BOM**（pro_route_product_bom）。
     *      制程BOM = 产品BOM 按工序分流 + 工艺上补录的辅料，是"这批料在这条线上
     *      实际会被吃掉多少"的真相，与报工倒冲**同一个源**，两边才能比对。
     *   2. 产品没挂制程（外协/外购工单）→ 退回**产品BOM**（md_product_bom），
     *      只展开成品构成，没有工序维度可言。
     *
     * 同一物料在多道工序出现时**用量相加**：比如螺丝装配用 24 件、接线再用 4 件，
     * 工单层面要的是"这批一共要备多少料"，汇总成一行 28 件；
     * 倒冲时两道工序也各扣一次，两边口径正好对得上。
     *
     * @param workorderId 工单ID
     * @return 展开出的行数
     */
    private int expandBom(Long workorderId) {
        ProWorkorder db = proWorkorderMapper.selectById(workorderId);
        if (db == null) {
            throw new BusinessException("生产工单不存在或已被删除");
        }

        // ---- 1. 定用料来源：工单的产品有没有挂制程 ----
        Long routeId = null;
        List<ProRouteProduct> routes = proRouteProductMapper.selectByItemId(db.getProductId());
        if (routes != null && !routes.isEmpty()) {
            // A100 这类产品可能挂多条制程，取第一条 —— 与排产 loadRouteProduct() 同一口径，
            // 否则会出现"排产按路线A拆工序、用料按路线B展开"的错配
            routeId = routes.get(0).getRouteId();
        }

        // 汇总容器：key = 物料ID，同一物料多道工序的用量累加
        // LinkedHashMap 保序，展开结果的顺序跟制程BOM 查看页一致（按工序序号、物料编码）
        Map<Long, ProWorkorderBom> merged = new LinkedHashMap<>();
        String sourceTag;

        if (routeId != null) {
            // ---- 2.1 制程BOM 展开 ----
            sourceTag = "下达自动展开(制程BOM)";
            List<ProRouteProductBom> boms =
                    proRouteProductBomMapper.selectByProductAndRoute(db.getProductId(), routeId);
            if (boms == null || boms.isEmpty()) {
                throw new BusinessException("产品[" + db.getProductName()
                        + "]的制程下还没有维护用料，无法展开工单用料。"
                        + "请先到 生产管理-产品制程 的用料明细里维护");
            }
            LocalDateTime now = LocalDateTime.now();
            for (ProRouteProductBom bom : boms) {
                ProWorkorderBom row = merged.get(bom.getItemId());
                if (row == null) {
                    row = new ProWorkorderBom();
                    row.setWorkorderId(workorderId);
                    row.setItemId(bom.getItemId());
                    row.setItemCode(bom.getItemCode());
                    row.setItemName(bom.getItemName());
                    row.setItemSpc(bom.getSpecification());
                    row.setUnitOfMeasure(bom.getUnitOfMeasure());
                    // 制程BOM 表上没有单位名称与物料/产品标识，回查主数据补齐
                    MdItem subItem = mdItemMapper.selectById(bom.getItemId());
                    if (subItem != null) {
                        row.setUnitName(subItem.getUnitName());
                        row.setItemOrProduct(subItem.getItemOrProduct());
                    }
                    row.setQuantity(BigDecimal.ZERO);
                    row.setCreateTime(now);
                    merged.put(bom.getItemId(), row);
                }
                row.setQuantity(row.getQuantity().add(nvl(bom.getQuantity())));
            }
        } else {
            // ---- 2.2 产品BOM 展开（外协/外购等没挂制程的工单） ----
            sourceTag = "下达自动展开(产品BOM)";
            List<MdProductBom> boms = mdProductBomMapper.selectByItemId(db.getProductId());
            if (boms == null || boms.isEmpty()) {
                throw new BusinessException("产品[" + db.getProductName()
                        + "]还没有维护 BOM，无法展开工单用料。请先到 主数据-产品BOM 里维护");
            }
            for (MdProductBom bom : boms) {
                ProWorkorderBom row = new ProWorkorderBom();
                row.setWorkorderId(workorderId);
                row.setItemId(bom.getBomItemId());
                row.setItemCode(bom.getBomItemCode());
                row.setItemName(bom.getBomItemName());
                row.setItemSpc(bom.getBomItemSpec());
                row.setUnitOfMeasure(bom.getUnitOfMeasure());
                row.setItemOrProduct(bom.getItemOrProduct());
                // 单位名称不在 md_product_bom 里，从 md_item 按单位编码反查一次补上
                MdItem subItem = mdItemMapper.selectById(bom.getBomItemId());
                if (subItem != null) {
                    row.setUnitName(subItem.getUnitName());
                }
                row.setQuantity(nvl(bom.getQuantity()));
                merged.put(bom.getBomItemId(), row);
            }
        }

        // ---- 3. 预计使用量 = 单位用量合计 × 工单生产数量 ----
        LocalDateTime now = LocalDateTime.now();
        List<ProWorkorderBom> rows = new ArrayList<>(merged.values());
        for (ProWorkorderBom row : rows) {
            row.setQuantity(row.getQuantity().multiply(nvl(db.getQuantity())));
            row.setRemark(sourceTag);
            row.setCreateBy(DEFAULT_OPERATOR);
            row.setUpdateBy(DEFAULT_OPERATOR);
            row.setCreateTime(now);
            row.setUpdateTime(now);
        }

        // 整批替换：先清空再插
        proWorkorderBomMapper.deleteByWorkorderId(workorderId);
        if (rows.isEmpty()) {
            return 0;
        }
        proWorkorderBomMapper.insertBatch(rows);
        return rows.size();
    }

    /**
     * BigDecimal 空值兜底
     * 数据库里 quantity 有默认值不会为 null，但前端传入的 DTO 可能是 null，
     * 直接参与乘法会 NPE，统一在这里转 0。
     */
    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 状态编码转中文名（拼错误提示用）
     */
    private String statusName(String status) {
        if (STATUS_PREPARE.equals(status)) {
            return "待下达";
        }
        if (STATUS_CONFIRMED.equals(status)) {
            return "已下达";
        }
        if (STATUS_FINISHED.equals(status)) {
            return "已完工";
        }
        if (STATUS_CANCELED.equals(status)) {
            return "已取消";
        }
        return status;
    }

    private boolean isBlank(String s) {
        return s == null || "".equals(s.trim());
    }
}
