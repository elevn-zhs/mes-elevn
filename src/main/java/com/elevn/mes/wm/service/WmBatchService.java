package com.elevn.mes.wm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.wm.entity.WmBatch;

/**
 * 批次 Service
 *
 */
public interface WmBatchService {

    /**
     * 分页 + 多条件查询批次
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param wmBatch 查询条件（物料/批号/生产日期区间/质量状态）
     * @return 分页结果
     */
    PageInfo<WmBatch> page(int pageNum, int pageSize, WmBatch wmBatch);

    /**
     * 查询批次详情（含库存分布与库存总量）
     * @param id 批次ID
     * @return 批次对象，不存在返回 null
     */
    WmBatch queryById(Long id);

    /**
     * 新增批次（批次编号由后端生成；属性按物料的批次配置校验必填）
     * @param wmBatch 批次对象
     * @return 影响行数
     */
    int save(WmBatch wmBatch);

    /**
     * 修改批次（批次编号与所属物料不可改）
     * @param wmBatch 批次对象
     * @return 影响行数
     */
    int updateById(WmBatch wmBatch);

    /**
     * 删除批次（还有库存时拒绝）
     * @param id 批次ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 批量删除批次（逐条做删除校验）
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(Long[] ids);
}
