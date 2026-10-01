package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProSnProcess;
import com.elevn.mes.wm.entity.WmSn;

import java.util.List;

/**
 * SN Service（生成 → 绑定 → 追溯）
 *
 */
public interface WmSnService {

    /** 分页查询 SN */
    PageInfo<WmSn> page(int pageNum, int pageSize, WmSn query);

    /** SN 详情 */
    WmSn getById(Long snId);

    /**
     * 批量生成 SN：按产品 + 批次（+可选工单）生成 count 个，
     * 物料快照现查 md_item 回填，SN 码按规则 WM_SN 取号
     */
    List<WmSn> generate(String batchCode, Long itemId, Long workorderId,
                        Integer count, String operator);

    /** 重新绑定批次/工单（仅"在库"可改） */
    void updateBind(WmSn sn);

    /** 改状态（IN_STOCK / SHIPPED / FROZEN） */
    void updateStatus(Long snId, String status);

    /** 删除 SN（仅"在库"） */
    void delete(Long snId);

    /** 单件全流程追溯：pro_sn_process 按 seq_num 正序（A 线写入，这里只读） */
    List<ProSnProcess> trace(Long snId);
}
