package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProRoute;

import java.util.List;

/**
 * 工艺路线 Service 接口
 *
 */
public interface ProRouteService {

    /**
     * 分页 + 多条件查询工艺路线
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param proRoute 查询条件
     * @return 分页结果
     */
    PageInfo<ProRoute> page(int pageNum, int pageSize, ProRoute proRoute);

    /**
     * 根据ID查询路线详情（含工序明细列表）
     * @param id 路线ID
     * @return 路线对象
     */
    ProRoute queryById(Long id);

    /**
     * 新增工艺路线
     * @param proRoute 路线对象
     * @return 影响行数
     */
    int save(ProRoute proRoute);

    /**
     * 修改工艺路线
     * @param proRoute 路线对象
     * @return 影响行数
     */
    int updateById(ProRoute proRoute);

    /**
     * 删除工艺路线（被产品制程引用时拒绝删除）
     * @param id 路线ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 批量删除工艺路线
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(Long[] ids);

    /**
     * 查询全部启用的路线（产品制程挂路线的下拉数据源）
     * @return 路线列表
     */
    List<ProRoute> queryAllEnabled();

    /**
     * 根据路线编码查询（编码占用实时校验用）
     * @param routeCode 路线编码
     * @return 路线对象，不存在返回 null
     */
    ProRoute queryByRouteCode(String routeCode);
}
