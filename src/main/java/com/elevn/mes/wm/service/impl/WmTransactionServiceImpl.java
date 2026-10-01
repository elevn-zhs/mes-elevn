package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.wm.entity.WmTransaction;
import com.elevn.mes.wm.mapper.WmTransactionMapper;
import com.elevn.mes.wm.service.WmTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存事务 Service 实现（全只读）
 *
 */
@Service
public class WmTransactionServiceImpl implements WmTransactionService {

    @Autowired
    private WmTransactionMapper wmTransactionMapper;

    @Override
    public PageInfo<WmTransaction> page(int pageNum, int pageSize, WmTransaction transaction) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(wmTransactionMapper.selectByCondition(transaction));
    }

    @Override
    public WmTransaction queryById(Long transactionId) {
        if (transactionId == null) {
            throw new BusinessException("事务ID不能为空");
        }
        WmTransaction transaction = wmTransactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw new BusinessException("库存流水不存在或已被删除");
        }
        return transaction;
    }

    @Override
    public List<WmTransaction> listByItemAndBatch(WmTransaction transaction) {
        if (transaction == null || transaction.getItemId() == null) {
            throw new BusinessException("请先选择物料，再进行批次追溯");
        }
        return wmTransactionMapper.selectByItemAndBatch(transaction);
    }

    @Override
    public List<WmTransaction> statByType(WmTransaction transaction) {
        return wmTransactionMapper.statByType(transaction);
    }
}
