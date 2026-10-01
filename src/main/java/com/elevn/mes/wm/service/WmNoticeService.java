package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmNotice;
import com.elevn.mes.wm.entity.WmNoticeLine;

import java.util.List;

/**
 * 仓储通知单 Service
 *
 * 【通知单不动库存，它的三个作用是】
 *   ① 提前告诉仓库做准备（到货 / 发货预告、备料申请）
 *   ② 触发质量检验：到货通知 → C 线 IQC，发货通知 → C 线 OQC
 *   ③ 作为下游单据的来源：备料申请 → 生产领料单
 *
 * 【状态流转很简单，只有一个动作】
 *   待处理 PREPARE ──triggerQc()──▶ 已触发检验 CONFIRMED
 *      │
 *      └──（已触发检验后就不能改也不能删了）
 *   为什么只有一个动作？因为通知单没有"过账"这一步 ——
 *   真正动库存的是后面的出入库单据。
 *
 */
public interface WmNoticeService {

    /**
     * 分页查询通知单（必须带 noticeType，3 类混在一起没法看）
     */
    PageInfo<WmNotice> page(int pageNum, int pageSize, WmNotice wmNotice);

    /**
     * 查询通知单详情（含明细行 lineList）
     */
    WmNotice queryById(Long noticeId);

    /**
     * 查询通知单的行列表
     */
    List<WmNoticeLine> queryLineList(Long noticeId);

    /**
     * 新增通知单（含行），编号按编码规则生成
     */
    int save(WmNotice wmNotice);

    /**
     * 修改通知单（含行，整批替换）。只有【待处理】状态能改。
     */
    int updateById(WmNotice wmNotice);

    /**
     * 删除通知单（逻辑删除 + 删掉行）。只有【待处理】且没报过检的能删。
     */
    int deleteById(Long noticeId);

    /**
     * 批量删除通知单
     */
    int deleteBatch(Long[] noticeIds);

    /**
     * 触发质量检验
     *
     * 到货通知（ARRIVAL）→ IQC 来料检验
     * 发货通知（SALES）  → OQC 出货检验
     * 备料申请（MATERIAL_REQUEST）不触发检验，它触发的是备料/领料
     *
     * 【C 线接口还没就绪】目前只把行上的 qc_flag 置 'Y' 并打一条 TODO 日志，
     * 等 C 线提供检验单接口后再改成真实调用，并把 qcId / qcCode 回填到行上。
     *
     * @return 本次触发的检验类型（IQC / OQC）
     */
    String triggerQc(Long noticeId);
}
