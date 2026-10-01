package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmLocation;

import java.util.List;

/**
 * 库区库位 Service
 *
 */
public interface WmLocationService {

    /**
     * 分页 + 多条件查询库区库位
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param wmLocation 查询条件（仓库/类型/编码/名称/启用状态）
     * @return 分页结果
     */
    PageInfo<WmLocation> page(int pageNum, int pageSize, WmLocation wmLocation);

    /**
     * 查询库区库位树（库区为父、库位为子）
     * @param wmLocation 过滤条件（warehouseId / enableFlag）
     * @return 树形列表，库区节点带 children
     */
    List<WmLocation> queryTree(WmLocation wmLocation);

    /**
     * 查询详情（库位会额外带出当前存放的物料与批次）
     * @param id 库区/库位ID
     * @return 对象，不存在返回 null
     */
    WmLocation queryById(Long id);

    /**
     * 新增库区 / 库位
     * @param wmLocation 对象
     * @return 影响行数
     */
    int save(WmLocation wmLocation);

    /**
     * 修改库区 / 库位（类型与所属库区不可改）
     * @param wmLocation 对象
     * @return 影响行数
     */
    int updateById(WmLocation wmLocation);

    /**
     * 删除库区 / 库位（有子节点或已有库存时拒绝）
     * @param id 库区/库位ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 批量删除（逐条做删除校验）
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(Long[] ids);

    /**
     * 按类型 + 编码查询（编码是否已占用的实时校验）
     * @param locationType AREA / LOCATION
     * @param locationCode 编码
     * @return 对象，不存在返回 null
     */
    WmLocation queryByCodeAndType(String locationType, String locationCode);
}
