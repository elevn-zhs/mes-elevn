package com.elevn.mes.pro.service;

import com.elevn.mes.pro.entity.ProRouteProduct;

import java.util.List;

/**
 * 产品制程 Service 接口
 *
 */
public interface ProRouteProductService {

    /**
     * 按产品ID查询全部制程（物料详情页 Tab 用）
     * @param itemId 产品ID
     * @return 制程列表
     */
    List<ProRouteProduct> selectByItemId(Long itemId);

    /**
     * 新增产品制程（L3：路线/产品必须存在、同产品同路线查重、数量必须大于 0）
     * @param proRouteProduct 制程对象
     * @return 影响行数
     */
    int save(ProRouteProduct proRouteProduct);

    /**
     * 修改产品制程（只能改数量/用时/备注，路线和产品不能换——换等于删了重挂）
     * @param proRouteProduct 制程对象
     * @return 影响行数
     */
    int updateById(ProRouteProduct proRouteProduct);

    /**
     * 删除产品制程（联动清理该产品该路线的制程BOM）
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
