package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdItemBatchConfig;
import org.springframework.transaction.annotation.Transactional;

/**
 * 物料批次属性配置 Service 接口
 *
 */
@Transactional
public interface MdItemBatchConfigService {

    /**
     * 分页 + 多条件查询物料批次属性配置列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdItemBatchConfig 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdItemBatchConfig> page(int pageNum, int pageSize, MdItemBatchConfig mdItemBatchConfig);

    /**
     * 根据ID查询物料批次属性配置详情
     */
    MdItemBatchConfig queryById(Long id);

    /**
     * 根据ID删除物料批次属性配置（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除物料批次属性配置（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改物料批次属性配置（只更新非 null 字段）
     */
    int updateById(MdItemBatchConfig mdItemBatchConfig);

    /**
     * 新增物料批次属性配置
     */
    int save(MdItemBatchConfig mdItemBatchConfig);

    /**
     * 按所属ID查询物料批次属性配置（一对一，最多一条）
     */
    MdItemBatchConfig queryByItemId(Long itemId);

}
