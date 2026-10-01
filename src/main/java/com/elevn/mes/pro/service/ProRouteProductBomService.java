package com.elevn.mes.pro.service;

import com.elevn.mes.pro.entity.ProRouteProductBom;

import java.util.List;

/**
 * 制程物料BOM Service 接口
 *
 */
public interface ProRouteProductBomService {

    /**
     * 按产品+路线查询全部BOM行（按工序顺序、物料编码排序）
     * @param productId 产品ID
     * @param routeId 路线ID
     * @return BOM行列表
     */
    List<ProRouteProductBom> selectByProductAndRoute(Long productId, Long routeId);

    /**
     * 整批保存制程BOM（逻辑删除旧行 + 校验 + 插入新行）
     *
     * L3 校验：
     *   1. 工序必须属于该路线（在 pro_route_process 里查得到）
     *   2. 物料必须真实存在，且不能是产品本身
     *   3. 单套用量必须大于 0
     *   4. 同一工序同一物料不能重复
     *
     * @param productId 产品ID
     * @param routeId 路线ID
     * @param bomList 完整BOM行列表
     * @return 影响行数
     */
    int batchSave(Long productId, Long routeId, List<ProRouteProductBom> bomList);
}
