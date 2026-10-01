package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.service.SysCodingRuleService;
import com.elevn.mes.wm.entity.WmMaterialStock;
import com.elevn.mes.wm.entity.WmStockTaking;
import com.elevn.mes.wm.entity.WmStockTakingLine;
import com.elevn.mes.wm.entity.WmStockTakingPlan;
import com.elevn.mes.wm.entity.WmStockTakingScope;
import com.elevn.mes.wm.entity.WmTransaction;
import com.elevn.mes.wm.mapper.WmMaterialStockMapper;
import com.elevn.mes.wm.mapper.WmStockTakingLineMapper;
import com.elevn.mes.wm.mapper.WmStockTakingMapper;
import com.elevn.mes.wm.mapper.WmStockTakingPlanMapper;
import com.elevn.mes.wm.mapper.WmStockTakingScopeMapper;
import com.elevn.mes.wm.mapper.WmTransactionMapper;
import com.elevn.mes.wm.service.WmStockTakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 盘点 Service 实现
 *
 * 【核心口径】
 *   1. 账面数量是"生成那一刻"的快照，不实时刷新 —— 盘点比的是
 *      "生成时的账" vs "实际数出来的货"，中途的出入库属于业务正常流动，
 *      期末盘点的差异核对本来就是这么做的（期初 + 入 - 出 = 期末账）。
 *   2. 过账在一个事务里：盈亏改库存 + 每条差异行一条流水 + 状态推进。
 *      差异行写一半失败必须整单回滚，不留半截账。
 *   3. 盘亏的扣减复用 reduceOnhand 的"可用量守卫"：
 *      可用量不足以扣减时整单失败 —— 宁可不让过账，也不能把库存扣成负数。
 *
 */
@Service
public class WmStockTakingServiceImpl implements WmStockTakingService {

    @Autowired
    private WmStockTakingPlanMapper planMapper;

    @Autowired
    private WmStockTakingScopeMapper scopeMapper;

    @Autowired
    private WmStockTakingMapper takingMapper;

    @Autowired
    private WmStockTakingLineMapper lineMapper;

    @Autowired
    private WmMaterialStockMapper materialStockMapper;

    @Autowired
    private WmTransactionMapper transactionMapper;

    @Autowired
    private SysCodingRuleService codingRuleService;

    /** 盘点差异流水的类型（wm_transaction.transaction_type，不在 14 类单据里，是盘点专属） */
    private static final String TYPE_STOCK_TAKING = "STOCK_TAKING";

    private static final String ST_PREPARE = "PREPARE";
    private static final String ST_CONFIRMED = "CONFIRMED";
    private static final String ST_CANCELED = "CANCELED";

    private static final String SCOPE_WAREHOUSE = "WAREHOUSE";
    private static final String SCOPE_AREA = "AREA";
    private static final String SCOPE_ITEM_TYPE = "ITEM_TYPE";

    private static final String DEFAULT_OPERATOR = "admin";

    // ==================== 计划 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmStockTakingPlan createPlan(WmStockTakingPlan plan) {
        if (plan.getScopeList() == null || plan.getScopeList().isEmpty()) {
            throw new BusinessException("盘点计划至少要圈一个范围（仓库/库区/物料分类）");
        }
        plan.setPlanCode(codingRuleService.autoCode("WM_TAKING_PLAN"));
        plan.setStatus(ST_PREPARE);
        planMapper.insert(plan);
        for (WmStockTakingScope scope : plan.getScopeList()) {
            scope.setPlanId(plan.getPlanId());
            scopeMapper.insert(scope);
        }
        return plan;
    }

    @Override
    public PageInfo<WmStockTakingPlan> pagePlan(int pageNum, int pageSize, WmStockTakingPlan query) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(planMapper.selectByCondition(query));
    }

    @Override
    public WmStockTakingPlan getPlanById(Long planId) {
        WmStockTakingPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("盘点计划不存在或已被删除");
        }
        plan.setScopeList(scopeMapper.selectByPlanId(planId));
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(WmStockTakingPlan plan) {
        WmStockTakingPlan db = planMapper.selectById(plan.getPlanId());
        if (db == null) {
            throw new BusinessException("盘点计划不存在或已被删除");
        }
        if (planMapper.countTakingByPlanId(plan.getPlanId()) > 0) {
            throw new BusinessException("该计划已经生成过盘点单，不能再改范围");
        }
        if (plan.getScopeList() == null || plan.getScopeList().isEmpty()) {
            throw new BusinessException("盘点计划至少要圈一个范围");
        }
        int rows = planMapper.updateById(plan);
        if (rows == 0) {
            throw new BusinessException("计划保存失败：可能刚被别人处理过，请刷新后重试");
        }
        // 范围整批替换：先清空再插，保证"传什么就是什么"
        scopeMapper.deleteByPlanId(plan.getPlanId());
        for (WmStockTakingScope scope : plan.getScopeList()) {
            scope.setPlanId(plan.getPlanId());
            scopeMapper.insert(scope);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePlan(Long planId) {
        WmStockTakingPlan db = planMapper.selectById(planId);
        if (db == null) {
            throw new BusinessException("盘点计划不存在或已被删除");
        }
        if (planMapper.countTakingByPlanId(planId) > 0) {
            throw new BusinessException("该计划已经生成过盘点单，计划要留作盘点单的溯源，不能删");
        }
        scopeMapper.deleteByPlanId(planId);
        planMapper.deleteById(planId);
    }

    // ==================== 生成 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmStockTaking generate(Long planId, String operator) {
        // ---------- 1. 计划校验 + 状态抢占 ----------
        WmStockTakingPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("盘点计划不存在或已被删除");
        }
        // 抢状态：两个并发请求同时点"生成"只有一个进得来
        int grabbed = planMapper.updateStatus(planId, ST_PREPARE, ST_CONFIRMED);
        if (grabbed == 0) {
            throw new BusinessException("该计划已经生成过盘点单，不要重复生成");
        }

        // ---------- 2. 展开范围成库存行（并集去重，用 LinkedHashMap 保序） ----------
        List<WmStockTakingScope> scopes = scopeMapper.selectByPlanId(planId);
        if (scopes.isEmpty()) {
            throw new BusinessException("该计划没有圈定任何范围，无法生成盘点单");
        }
        Map<Long, WmMaterialStock> stockMap = new LinkedHashMap<>();
        for (WmStockTakingScope scope : scopes) {
            List<WmMaterialStock> rows;
            switch (scope.getScopeType()) {
                case SCOPE_WAREHOUSE:
                    rows = materialStockMapper.selectByScope(scope.getScopeValueId(), null, null);
                    break;
                case SCOPE_AREA:
                    rows = materialStockMapper.selectByScope(null, scope.getScopeValueId(), null);
                    break;
                case SCOPE_ITEM_TYPE:
                    rows = materialStockMapper.selectByScope(null, null, scope.getScopeValueId());
                    break;
                default:
                    throw new BusinessException("未知的盘点范围类型：" + scope.getScopeType());
            }
            for (WmMaterialStock row : rows) {
                stockMap.putIfAbsent(row.getMaterialStockId(), row);
            }
        }
        if (stockMap.isEmpty()) {
            // 范围里一件库存都没有：把计划状态退回去，不然计划就死在"已生成"上却没有单
            planMapper.updateStatus(planId, ST_CONFIRMED, ST_PREPARE);
            throw new BusinessException("盘点范围内没有任何现存库存，生成不了盘点单。"
                    + "（账上没货的行不参与盘点；如果确实需要盘'空库位'，应该在主数据里先挂上库存）");
        }

        // ---------- 3. 生成盘点单 + 明细行（账面数量 = 生成那一刻的现存） ----------
        WmStockTaking taking = new WmStockTaking();
        taking.setTakingCode(codingRuleService.autoCode("WM_STOCK_TAKING"));
        taking.setTakingName(plan.getPlanName() + "-盘点单");
        taking.setTakingDate(LocalDateTime.now());
        taking.setTakingType(plan.getTakingType());
        taking.setUserId(operator);
        taking.setUserName(operator);
        taking.setNickName(operator);
        taking.setBlindFlag(plan.getBlindFlag());
        taking.setFrozenFlag(plan.getFrozenFlag());
        taking.setPlanId(plan.getPlanId());
        taking.setPlanCode(plan.getPlanCode());
        taking.setPlanName(plan.getPlanName());
        taking.setStartTime(plan.getStartTime());
        taking.setEndTime(plan.getEndTime());
        takingMapper.insert(taking);

        for (WmMaterialStock stock : stockMap.values()) {
            WmStockTakingLine line = new WmStockTakingLine();
            line.setTakingId(taking.getTakingId());
            line.setMaterialStockId(stock.getMaterialStockId());
            line.setItemId(stock.getItemId());
            line.setItemCode(stock.getItemCode());
            line.setItemName(stock.getItemName());
            line.setSpecification(stock.getSpecification());
            line.setUnitOfMeasure(stock.getUnitOfMeasure());
            line.setUnitName(stock.getUnitName());
            line.setBatchId(stock.getBatchId());
            line.setBatchCode(stock.getBatchCode());
            // 账面数量：现存数量（不是可用量 —— 保留量也是账上的货，盘的时候得数出来）
            line.setQuantity(stock.getQuantityOnhand());
            line.setWarehouseId(stock.getWarehouseId());
            line.setWarehouseCode(stock.getWarehouseCode());
            line.setWarehouseName(stock.getWarehouseName());
            line.setLocationId(stock.getLocationId());
            line.setLocationCode(stock.getLocationCode());
            line.setLocationName(stock.getLocationName());
            line.setAreaId(stock.getAreaId());
            line.setAreaCode(stock.getAreaCode());
            line.setAreaName(stock.getAreaName());
            lineMapper.insert(line);
        }
        return taking;
    }

    // ==================== 盘点单 ====================

    @Override
    public PageInfo<WmStockTaking> page(int pageNum, int pageSize, WmStockTaking query) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(takingMapper.selectByCondition(query));
    }

    @Override
    public WmStockTaking getById(Long takingId) {
        WmStockTaking taking = takingMapper.selectById(takingId);
        if (taking == null) {
            throw new BusinessException("盘点单不存在或已被删除");
        }
        List<WmStockTakingLine> lines = lineMapper.selectByTakingId(takingId);
        // 盲盘：账面数量和差异都不给录入页面看（差异 = 账面 - 实盘，给一个等于给另一个）
        if ("Y".equals(taking.getBlindFlag())) {
            taking.setBlind(true);
            for (WmStockTakingLine line : lines) {
                line.setQuantity(null);
                line.setDiffQuantity(null);
            }
        } else {
            taking.setBlind(false);
        }
        taking.setLineList(lines);
        return taking;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTakingQuantities(Long takingId, List<WmStockTakingLine> lines) {
        WmStockTaking taking = takingMapper.selectById(takingId);
        if (taking == null) {
            throw new BusinessException("盘点单不存在或已被删除");
        }
        if (!ST_PREPARE.equals(taking.getStatus())) {
            throw new BusinessException("盘点单已过账，不能再改实盘数");
        }
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("没有要保存的实盘数据");
        }
        for (WmStockTakingLine input : lines) {
            WmStockTakingLine db = lineMapper.selectById(input.getLineId());
            if (db == null || !takingId.equals(db.getTakingId())) {
                throw new BusinessException("明细行（ID=" + input.getLineId() + "）不属于这张盘点单");
            }
            if (input.getTakingQuantity() == null
                    || input.getTakingQuantity().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("实盘数量不能为空也不能为负（物料 "
                        + db.getItemCode() + "）");
            }
            // 差异后端算，不信前端凑数
            BigDecimal diff = input.getTakingQuantity().subtract(
                    db.getQuantity() == null ? BigDecimal.ZERO : db.getQuantity());
            int rows = lineMapper.updateTaking(input.getLineId(), input.getTakingQuantity(), diff);
            if (rows == 0) {
                throw new BusinessException("明细行（ID=" + input.getLineId() + "）保存失败，"
                        + "可能刚被别人录入过，请刷新后重试");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void post(Long takingId) {
        // ---------- 1. 前置校验 ----------
        WmStockTaking taking = takingMapper.selectById(takingId);
        if (taking == null) {
            throw new BusinessException("盘点单不存在或已被删除");
        }
        int uncounted = lineMapper.countUncounted(takingId);
        if (uncounted > 0) {
            throw new BusinessException("还有 " + uncounted + " 行没录实盘数，过账前必须盘完"
                    + "（没盘到的行实盘数录 0）");
        }

        // ---------- 2. 抢状态，防止并发重复过账 ----------
        int grabbed = takingMapper.updateStatus(takingId, ST_PREPARE, ST_CONFIRMED);
        if (grabbed == 0) {
            throw new BusinessException("盘点单已经过账，不要重复操作");
        }

        // ---------- 3. 逐差异行：改库存 + 写流水（在同一事务里） ----------
        List<WmStockTakingLine> diffLines = lineMapper.selectDiffLines(takingId);
        for (WmStockTakingLine line : diffLines) {
            WmMaterialStock stock = materialStockMapper.selectById(line.getMaterialStockId());
            if (stock == null) {
                throw new BusinessException("库存行已被删除（物料 " + line.getItemCode()
                        + "，库位 " + line.getLocationCode() + "），无法过账，请联系管理员核对");
            }
            BigDecimal diff = line.getDiffQuantity();
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                // 盘盈：实际比账多，加回该库存行
                materialStockMapper.addOnhand(line.getMaterialStockId(), diff);
            } else {
                // 盘亏：实际比账少，扣减（reduceOnhand 自带可用量守卫，不够扣返回 0 行）
                int rows = materialStockMapper.reduceOnhand(
                        line.getMaterialStockId(), diff.abs());
                if (rows == 0) {
                    throw new BusinessException("盘亏扣不动：物料 " + line.getItemCode()
                            + " 在 " + stock.getWarehouseName() + "/" + line.getLocationCode()
                            + " 的现存/可用量已经不足（生成盘点单后有人出过库），"
                            + "请先处理相关单据或重新盘点");
                }
            }
            insertTransaction(taking, line, stock, diff);
        }
    }

    /**
     * 盘点差异流水：一条差异行一条流水，方向跟差异走（盈 +1 / 亏 -1），
     * 类型 STOCK_TAKING，来源单据指向盘点单本身
     */
    private void insertTransaction(WmStockTaking taking, WmStockTakingLine line,
                                   WmMaterialStock stock, BigDecimal diff) {
        WmTransaction trx = new WmTransaction();
        trx.setTransactionType(TYPE_STOCK_TAKING);
        trx.setItemId(stock.getItemId());
        trx.setItemCode(stock.getItemCode());
        trx.setItemName(stock.getItemName());
        trx.setSpecification(stock.getSpecification());
        trx.setUnitOfMeasure(stock.getUnitOfMeasure());
        trx.setUnitName(stock.getUnitName());
        trx.setBatchId(stock.getBatchId());
        trx.setBatchCode(stock.getBatchCode());
        trx.setWarehouseId(stock.getWarehouseId());
        trx.setWarehouseCode(stock.getWarehouseCode());
        trx.setWarehouseName(stock.getWarehouseName());
        trx.setLocationId(stock.getLocationId());
        trx.setLocationCode(stock.getLocationCode());
        trx.setLocationName(stock.getLocationName());
        trx.setAreaId(stock.getAreaId());
        trx.setAreaCode(stock.getAreaCode());
        trx.setAreaName(stock.getAreaName());
        trx.setSourceDocType("wm_stock_taking");
        trx.setSourceDocId(taking.getTakingId());
        trx.setSourceDocCode(taking.getTakingCode());
        trx.setSourceDocLineId(line.getLineId());
        trx.setMaterialStockId(line.getMaterialStockId());
        trx.setTransactionFlag(diff.compareTo(BigDecimal.ZERO) > 0 ? 1 : -1);
        trx.setTransactionQuantity(diff.abs());
        trx.setTransactionDate(LocalDateTime.now());
        trx.setCreateBy(DEFAULT_OPERATOR);
        trx.setUpdateBy(DEFAULT_OPERATOR);
        transactionMapper.insert(trx);
    }

    @Override
    public List<WmStockTakingLine> diffLines(Long takingId) {
        WmStockTaking taking = takingMapper.selectById(takingId);
        if (taking == null) {
            throw new BusinessException("盘点单不存在或已被删除");
        }
        return lineMapper.selectDiffLines(takingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long takingId) {
        WmStockTaking taking = takingMapper.selectById(takingId);
        if (taking == null) {
            throw new BusinessException("盘点单不存在或已被删除");
        }
        int rows = takingMapper.deleteById(takingId);
        if (rows == 0) {
            throw new BusinessException("已过账的盘点单不能删除（差异已经进账）");
        }
    }
}
