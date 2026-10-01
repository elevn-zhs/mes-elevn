package com.elevn.mes.pro.service;

import com.elevn.mes.pro.entity.ProRouteProcess;

import java.util.List;

/**
 * 工艺路线工序明细 Service 接口
 *
 */
public interface ProRouteProcessService {

    /**
     * 按路线ID查询全部明细（按 order_num 升序）
     * @param routeId 路线ID
     * @return 明细列表
     */
    List<ProRouteProcess> selectByRouteId(Long routeId);

    /**
     * 整批保存路线工序明细（逻辑删除旧明细 + 校验 + 插入新明细）
     *
     * L3 校验全在这里：
     *   1. 工序必须存在且启用
     *   2. 同一路线内工序不能重复
     *   3. 顺序号必须从 1 开始连续
     *   4. 下一道工序必须是本路线内的工序（末工序不设下一道）
     *   5. 下一道链条不能成环
     *
     * @param routeId 路线ID
     * @param processList 明细列表（前端整表编辑后一次性提交）
     * @return 影响行数
     */
    int batchSave(Long routeId, List<ProRouteProcess> processList);
}
