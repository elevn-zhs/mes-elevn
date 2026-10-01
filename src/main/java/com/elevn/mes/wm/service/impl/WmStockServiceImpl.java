package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.wm.entity.WmMaterialStock;
import com.elevn.mes.wm.mapper.WmMaterialStockMapper;
import com.elevn.mes.wm.service.WmStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存查询 Service 实现（全只读）
 *
 */
@Service
public class WmStockServiceImpl implements WmStockService {

    @Autowired
    private WmMaterialStockMapper wmMaterialStockMapper;

    @Override
    public PageInfo<WmMaterialStock> query(int pageNum, int pageSize, WmMaterialStock stock) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(wmMaterialStockMapper.selectByCondition(stock));
    }

    @Override
    public WmMaterialStock queryById(Long materialStockId) {
        if (materialStockId == null) {
            throw new BusinessException("库存记录ID不能为空");
        }
        WmMaterialStock stock = wmMaterialStockMapper.selectById(materialStockId);
        if (stock == null) {
            throw new BusinessException("库存记录不存在或已被删除");
        }
        return stock;
    }

    @Override
    public List<WmMaterialStock> summaryByItem(WmMaterialStock stock) {
        return wmMaterialStockMapper.selectSummaryByItem(stock);
    }

    @Override
    public List<WmMaterialStock> summaryByWarehouse(WmMaterialStock stock) {
        return wmMaterialStockMapper.selectSummaryByWarehouse(stock);
    }

    @Override
    public List<WmMaterialStock> warningList() {
        return wmMaterialStockMapper.selectWarningList();
    }

    @Override
    public List<WmMaterialStock> summaryByWarehouseAndItem(Long warehouseId, Long itemId) {
        WmMaterialStock stock = new WmMaterialStock();
        stock.setWarehouseId(warehouseId);
        stock.setItemId(itemId);
        return wmMaterialStockMapper.selectSummaryByWarehouse(stock);
    }
}
