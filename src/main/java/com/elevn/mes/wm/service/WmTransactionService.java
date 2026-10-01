package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmTransaction;

import java.util.List;

/**
 * 库存事务（流水账）Service
 *
 * 【流水是"账"，库存是"余额"】
 *   库存表只放当前还剩多少；流水表每一次变动都留一条，只追加不修改。
 *   对账的逻辑就是：某个物料某段时间的流水加起来 == 库存余额的变化。
 *   所以本接口同样没有任何写方法 —— 流水只能由单据过账生成。
 *
 */
public interface WmTransactionService {

    /**
     * 分页查询流水（支持物料/批次/仓库/单据/时间区间筛选）
     */
    PageInfo<WmTransaction> page(int pageNum, int pageSize, WmTransaction transaction);

    /**
     * 按ID查询流水详情（含来源单据信息）
     */
    WmTransaction queryById(Long transactionId);

    /**
     * 按物料 + 批次查流水（批次追溯，时间正序）
     */
    List<WmTransaction> listByItemAndBatch(WmTransaction transaction);

    /**
     * 按事务类型统计进出数量
     */
    List<WmTransaction> statByType(WmTransaction transaction);
}
