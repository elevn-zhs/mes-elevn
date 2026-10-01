package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmDoc;
import com.elevn.mes.wm.entity.WmDocDetail;
import com.elevn.mes.wm.entity.WmDocLine;

import java.util.List;

/**
 * 出入库单据 Service
 *
 * 【整个仓储模块只有这一条路能改变库存】
 *   仓库设置 / 库区库位 / 批次都只是"档案"，
 *   真正让 wm_material_stock 变动的，只有本接口的 execute（过账）。
 *   所以这里的方法划分也是围绕状态机来的：
 *
 *     待过账 PREPARE ──execute()──▶ 已过账 CONFIRMED ──finish()──▶ 已完成 FINISHED
 *          │
 *          └──cancel()──▶ 已取消 CANCELED
 *
 *   · PREPARE 阶段随便改（改表头、加行删行、整单删除都行），因为还没动库存
 *   · 一旦 CONFIRMED，改单接口就会拒绝 —— 库存动过就回不了头，
 *     想反悔只能开一张反方向的单子（红冲），这样账上永远看得到"发生过什么"
 *   · CANCELED 只能从 PREPARE 来，已过账的单子不能"取消"
 *
 */
public interface WmDocService {

    /**
     * 分页查询单据列表（必须带 docType，14 类单据混在一张表里）
     */
    PageInfo<WmDoc> page(int pageNum, int pageSize, WmDoc wmDoc);

    /**
     * 查询单据详情（一次带出行列表 lineList 与落位明细 detailList）
     */
    WmDoc queryById(Long docId);

    /**
     * 查询单据的行列表
     */
    List<WmDocLine> queryLineList(Long docId);

    /**
     * 查询单据的落位明细（只有过账后才有数据）
     */
    List<WmDocDetail> queryDetailList(Long docId);

    /**
     * 新增单据（含行），编号由编码规则生成
     */
    int save(WmDoc wmDoc);

    /**
     * 修改单据（含行，整批替换）。只有 PREPARE 状态能改。
     */
    int updateById(WmDoc wmDoc);

    /**
     * 删除单据（逻辑删除，同时删掉行）。只有 PREPARE 状态能删。
     */
    int deleteById(Long docId);

    /**
     * 批量删除单据。只有 PREPARE 状态能删。
     */
    int deleteBatch(Long[] docIds);

    /**
     * 【本模块的心脏】过账
     *
     * 在一个事务里做四件事：
     *   ① 写 wm_doc_detail 落位
     *   ② 按 io_flag 改 wm_material_stock 库存
     *   ③ 生成 wm_transaction 流水（调拨配对两条）
     *   ④ 生产领料单回写 pro_task_issue 投料
     * 四件事同生共死 —— 少做一件，账就对不上。
     *
     * @param docId 单据ID
     * @return 影响行数（1 = 成功）
     */
    int execute(Long docId);

    /**
     * 取消单据（PREPARE → CANCELED）。已过账的单子不能取消。
     *
     * @param docId 单据ID
     * @param reason 取消原因（必填，留痕用）
     */
    int cancel(Long docId, String reason);

    /**
     * 调拨送达确认（CONFIRMED → FINISHED）。只有调拨单能调它。
     *
     * 调拨是唯一有"两次动作"的单据：过账时货离开源仓，送达确认后整单才算完，
     * 所以它的终态不是 CONFIRMED 而是 FINISHED。
     */
    int finish(Long docId);

    // ====================================================================
    // 单据行的单行维护（表头已保存、正在往单子上加料/改料时用）
    //
    // 说明：新增/编辑整单时用的是"整批替换式"（见 save / updateById），
    // 单据在【待过账】阶段也允许一行一行地改，所以另开这一组单行接口。
    // 两者都能改行，但都遵守同一条规矩：只有【待过账】的单据能改。
    // ====================================================================

    /**
     * 单行新增（自动接着编行号）
     */
    int saveLine(WmDocLine wmDocLine);

    /**
     * 按单据ID分页查询行
     */
    PageInfo<WmDocLine> pageLine(int pageNum, int pageSize, Long docId);

    /**
     * 单行修改
     */
    int updateLine(WmDocLine wmDocLine);

    /**
     * 单行删除（逻辑删除）
     */
    int deleteLine(Long lineId);
}
