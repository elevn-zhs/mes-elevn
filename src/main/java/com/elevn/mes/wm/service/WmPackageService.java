package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmPackage;
import com.elevn.mes.wm.entity.WmPackageLine;

import java.util.List;

/**
 * 装箱单 Service
 *
 * 【装箱的 L3 业务规则】
 *   1. 箱套箱：子箱的祖链(ancestors)由后端按父箱维护，编辑时不允许换父箱
 *   2. 明细行必须从库存行选：快照由后端从 wm_material_stock 现查回填，不信前端
 *   3. 装箱数量累计不得超过该库存行的现存数量（防"账上 10 个装了 12 个"）
 *   4. 状态机：PREPARE(装箱中) → PACKED(已完成)；完成后明细锁定
 *   5. 有子箱或有明细行的箱禁止删除；删除顶层箱连子树一起删（先校验再删）
 *
 */
public interface WmPackageService {

    /** 分页查询装箱单 */
    PageInfo<WmPackage> page(int pageNum, int pageSize, WmPackage query);

    /** 查详情（表头 + 明细行 + 子箱） */
    WmPackage getById(Long packageId);

    /** 新增箱（支持箱套箱：传 parentId 挂到父箱下） */
    WmPackage create(WmPackage wmPackage);

    /** 编辑箱信息（编号/父级/状态不可改，仅 PREPARE 可编辑） */
    void update(WmPackage wmPackage);

    /** 完成装箱（PREPARE → PACKED，明细锁定） */
    void finish(Long packageId);

    /** 删除箱（校验通过后连子树 + 明细一起删） */
    void delete(Long packageId);

    /** 加明细行（从库存行选，快照后端回填） */
    WmPackageLine addLine(WmPackageLine line);

    /** 改明细行（只允许改数量与备注） */
    void updateLine(WmPackageLine line);

    /** 删明细行（仅 PREPARE 可删） */
    void deleteLine(Long lineId);
}
