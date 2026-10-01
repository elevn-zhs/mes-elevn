package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmStockTaking;
import com.elevn.mes.wm.entity.WmStockTakingLine;
import com.elevn.mes.wm.entity.WmStockTakingPlan;

import java.math.BigDecimal;
import java.util.List;

/**
 * 盘点 Service（计划 → 生成 → 录入 → 过账 → 差异）
 *
 * 【过账是本模块唯一的写库存动作，且必须在一个事务里】
 *   盘盈加 / 盘亏减 → 写 wm_transaction 流水（类型 STOCK_TAKING）→ 状态推进。
 *   差异行可能有几十条，写一半失败不留半截账。
 *
 */
public interface WmStockTakingService {

    // ---------- 计划 ----------

    /** 新增盘点计划（含范围行） */
    WmStockTakingPlan createPlan(WmStockTakingPlan plan);

    /** 分页查询计划 */
    PageInfo<WmStockTakingPlan> pagePlan(int pageNum, int pageSize, WmStockTakingPlan query);

    /** 计划详情（含范围） */
    WmStockTakingPlan getPlanById(Long planId);

    /** 编辑计划（仅 PREPARE；范围整批替换） */
    void updatePlan(WmStockTakingPlan plan);

    /** 删除计划（已生成盘点单的禁止删） */
    void deletePlan(Long planId);

    // ---------- 生成 ----------

    /** 按计划展开范围生成盘点单（带账面数量；返回生成的盘点单） */
    WmStockTaking generate(Long planId, String operator);

    // ---------- 盘点单 ----------

    /** 分页查询盘点单 */
    PageInfo<WmStockTaking> page(int pageNum, int pageSize, WmStockTaking query);

    /** 详情（含明细行；盲盘单剥掉账面数量） */
    WmStockTaking getById(Long takingId);

    /** 批量录入实盘数（diff 由后端算） */
    void saveTakingQuantities(Long takingId, List<WmStockTakingLine> lines);

    /** 过账：盈亏写库存 + 流水，状态 PREPARE → CONFIRMED */
    void post(Long takingId);

    /** 差异明细（diff != 0 的行） */
    List<WmStockTakingLine> diffLines(Long takingId);

    /** 删除盘点单（仅 PREPARE） */
    void delete(Long takingId);
}
