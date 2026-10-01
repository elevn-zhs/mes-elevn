package com.elevn.mes.pro.service.impl;

import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.pro.entity.ProRoute;
import com.elevn.mes.pro.entity.ProRouteProduct;
import com.elevn.mes.pro.mapper.ProRouteProductBomMapper;
import com.elevn.mes.pro.mapper.ProRouteProductMapper;
import com.elevn.mes.pro.mapper.ProRouteMapper;
import com.elevn.mes.pro.service.ProRouteProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品制程 Service 实现
 *
 * L3 业务校验：
 *   1. 挂接的路线必须存在且启用
 *   2. 挂接的必须是「产品」（md_item.item_or_product = PRODUCT）—— 制程是给产品用的
 *   3. 同一产品不能重复挂同一条路线
 *   4. 生产数量必须大于 0
 *   5. 冗余字段（编码/名称/规格/单位）一律后端从 md_item 回填，不信任前端
 *
 */
@Service
public class ProRouteProductServiceImpl implements ProRouteProductService {

    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private ProRouteProductMapper proRouteProductMapper;

    @Autowired
    private ProRouteMapper proRouteMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Autowired
    private ProRouteProductBomMapper proRouteProductBomMapper;

    @Override
    public List<ProRouteProduct> selectByItemId(Long itemId) {
        return proRouteProductMapper.selectByItemId(itemId);
    }

    @Override
    @Transactional
    public int save(ProRouteProduct proRouteProduct) {
        MdItem item = checkRouteAndItem(proRouteProduct.getRouteId(), proRouteProduct.getItemId());
        checkDuplicate(proRouteProduct.getItemId(), proRouteProduct.getRouteId(), null);
        checkQuantity(proRouteProduct);
        fillItemFields(proRouteProduct, item);
        proRouteProduct.setUnitOfMeasure(item.getUnitOfMeasure());
        proRouteProduct.setCreateBy(DEFAULT_OPERATOR);
        proRouteProduct.setUpdateBy(DEFAULT_OPERATOR);
        proRouteProduct.setCreateTime(LocalDateTime.now());
        proRouteProduct.setUpdateTime(LocalDateTime.now());
        return proRouteProductMapper.insert(proRouteProduct);
    }

    @Override
    public int updateById(ProRouteProduct proRouteProduct) {
        ProRouteProduct db = proRouteProductMapper.selectById(proRouteProduct.getRecordId());
        if (db == null) {
            throw new BusinessException("产品制程不存在或已被删除");
        }
        // 路线和产品不允许在修改里换：换了等于"解挂一条、新挂一条"，
        // 挂接关系上的制程BOM全要作废，语义完全不同。前端也不提供这个入口。
        proRouteProduct.setRouteId(null);
        proRouteProduct.setItemId(null);
        checkQuantity(proRouteProduct);
        proRouteProduct.setUpdateBy(DEFAULT_OPERATOR);
        proRouteProduct.setUpdateTime(LocalDateTime.now());
        return proRouteProductMapper.updateById(proRouteProduct);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        ProRouteProduct db = proRouteProductMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("产品制程不存在或已被删除");
        }
        // 制程BOM是按 (product_id, route_id) 软关联的，制程解挂时 BOM 一起清，否则是孤儿数据
        proRouteProductBomMapper.deleteByProductAndRoute(db.getItemId(), db.getRouteId());
        return proRouteProductMapper.deleteById(id);
    }

    /**
     * 校验路线与产品（L3），并返回产品档案供回填冗余字段
     */
    private MdItem checkRouteAndItem(Long routeId, Long itemId) {
        ProRoute route = proRouteMapper.selectById(routeId);
        if (route == null) {
            throw new BusinessException("工艺路线不存在或已被删除");
        }
        if (!"Y".equals(route.getEnableFlag())) {
            throw new BusinessException("工艺路线[" + route.getRouteCode() + "]已停用，不能挂接");
        }
        MdItem item = mdItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("产品不存在或已被删除");
        }
        if (!"PRODUCT".equals(item.getItemOrProduct())) {
            throw new BusinessException("[" + item.getItemCode() + "]是物料不是产品，产品制程只能挂接到产品上");
        }
        return item;
    }

    /**
     * 同产品同路线查重（L3）
     */
    private void checkDuplicate(Long itemId, Long routeId, Long excludeRecordId) {
        int count = proRouteProductMapper.countDuplicate(itemId, routeId, excludeRecordId);
        if (count > 0) {
            throw new BusinessException("该产品已挂接此工艺路线，不能重复挂接");
        }
    }

    /**
     * 生产数量必须大于 0
     */
    private void checkQuantity(ProRouteProduct proRouteProduct) {
        if (proRouteProduct.getQuantity() != null && proRouteProduct.getQuantity() <= 0) {
            throw new BusinessException("生产数量必须大于 0");
        }
        if (proRouteProduct.getProductionTime() != null
                && proRouteProduct.getProductionTime().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("生产用时必须大于 0");
        }
    }

    /**
     * 从产品档案回填冗余字段
     */
    private void fillItemFields(ProRouteProduct proRouteProduct, MdItem item) {
        proRouteProduct.setItemCode(item.getItemCode());
        proRouteProduct.setItemName(item.getItemName());
        proRouteProduct.setSpecification(item.getSpecification());
        proRouteProduct.setUnitOfMeasure(item.getUnitName());
    }
}
