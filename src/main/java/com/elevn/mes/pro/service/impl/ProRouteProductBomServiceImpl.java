package com.elevn.mes.pro.service.impl;

import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.pro.entity.ProRouteProcess;
import com.elevn.mes.pro.entity.ProRouteProductBom;
import com.elevn.mes.pro.mapper.ProRouteProcessMapper;
import com.elevn.mes.pro.mapper.ProRouteProductBomMapper;
import com.elevn.mes.pro.service.ProRouteProductBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 制程物料BOM Service 实现
 *
 * 保存同样走「整批替换」：BOM 行挂在 (product_id, route_id) 维度上，
 * 一次编辑就是一张完整的工序用料表，整表提交、整表校验、旧删新插。
 *
 */
@Service
public class ProRouteProductBomServiceImpl implements ProRouteProductBomService {

    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private ProRouteProductBomMapper proRouteProductBomMapper;

    @Autowired
    private ProRouteProcessMapper proRouteProcessMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Override
    public List<ProRouteProductBom> selectByProductAndRoute(Long productId, Long routeId) {
        return proRouteProductBomMapper.selectByProductAndRoute(productId, routeId);
    }

    @Override
    @Transactional
    public int batchSave(Long productId, Long routeId, List<ProRouteProductBom> bomList) {
        if (productId == null || routeId == null) {
            throw new BusinessException("产品ID和路线ID不能为空");
        }

        // ===== 校验 1：工序必须属于该路线 =====
        List<ProRouteProcess> routeProcesses = proRouteProcessMapper.selectByRouteId(routeId);
        Set<Long> routeProcessIds = new HashSet<>();
        for (ProRouteProcess rp : routeProcesses) {
            routeProcessIds.add(rp.getProcessId());
        }

        // ===== 校验 2/3/4：物料存在且不是产品本身、用量大于 0、同工序同物料不重复，顺便回填冗余字段 =====
        Set<String> seen = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        for (ProRouteProductBom bom : bomList) {
            if (bom.getProcessId() == null) {
                throw new BusinessException("存在未选择工序的用料行");
            }
            if (!routeProcessIds.contains(bom.getProcessId())) {
                throw new BusinessException("用料行选择的工序不属于该工艺路线");
            }
            if (bom.getItemId() == null) {
                throw new BusinessException("存在未选择物料的用料行");
            }
            if (productId.equals(bom.getItemId())) {
                throw new BusinessException("用料不能选产品本身");
            }
            MdItem item = mdItemMapper.selectById(bom.getItemId());
            if (item == null) {
                throw new BusinessException("用料物料不存在或已被删除");
            }
            if (bom.getQuantity() == null || bom.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("物料[" + item.getItemCode() + "]的单套用量必须大于 0");
            }
            if (!seen.add(bom.getProcessId() + ":" + bom.getItemId())) {
                throw new BusinessException("同一工序下物料[" + item.getItemCode() + "]重复出现");
            }
            // 回填冗余字段
            bom.setUnitOfMeasure(item.getUnitOfMeasure());
            bom.setRouteId(routeId);
            bom.setProductId(productId);
            bom.setItemCode(item.getItemCode());
            bom.setItemName(item.getItemName());
            bom.setSpecification(item.getSpecification());
            bom.setUnitOfMeasure(item.getUnitName());
            bom.setCreateBy(DEFAULT_OPERATOR);
            bom.setCreateTime(now);
        }

        // ===== 整批替换：旧的全删、新的全插 =====
        proRouteProductBomMapper.deleteByProductAndRoute(productId, routeId);
        if (bomList.isEmpty()) {
            return 0;
        }
        return proRouteProductBomMapper.insertBatch(bomList);
    }
}
