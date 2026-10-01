package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmWarehouse;

import java.util.List;

/**
 * 仓库 Service
 *
 */
public interface WmWarehouseService {

    /**
     * 分页 + 多条件查询仓库
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param wmWarehouse 查询条件（编码/名称/是否启用）
     * @return 分页结果
     */
    PageInfo<WmWarehouse> page(int pageNum, int pageSize, WmWarehouse wmWarehouse);

    /**
     * 查询仓库详情（含库区数、库位数、库存总量、库存物料种数）
     * @param id 仓库ID
     * @return 仓库对象，不存在返回 null
     */
    WmWarehouse queryById(Long id);

    /**
     * 新增仓库
     * @param wmWarehouse 仓库对象
     * @return 影响行数
     */
    int save(WmWarehouse wmWarehouse);

    /**
     * 修改仓库（编码不可改，传了不同的编码会被拒绝）
     * @param wmWarehouse 仓库对象
     * @return 影响行数
     */
    int updateById(WmWarehouse wmWarehouse);

    /**
     * 删除仓库（下挂库区库位或有库存时拒绝）
     * @param id 仓库ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 批量删除仓库（逐条做删除校验）
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(Long[] ids);

    /**
     * 查询全部启用的仓库（下拉数据源）
     * @return 仓库列表
     */
    List<WmWarehouse> queryAllEnabled();

    /**
     * 根据编码查询仓库（编码是否已占用的实时校验）
     * @param warehouseCode 仓库编码
     * @return 仓库对象，不存在返回 null
     */
    WmWarehouse queryByWarehouseCode(String warehouseCode);
}
