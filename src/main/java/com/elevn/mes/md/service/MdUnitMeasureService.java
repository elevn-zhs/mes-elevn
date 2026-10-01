package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdUnitMeasure;
import org.springframework.transaction.annotation.Transactional;

/**
 * 计量单位 Service 接口
 *
 */
@Transactional
public interface MdUnitMeasureService {

    /**
     * 分页 + 多条件查询计量单位列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdUnitMeasure 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdUnitMeasure> page(int pageNum, int pageSize, MdUnitMeasure mdUnitMeasure);

    /**
     * 根据ID查询计量单位详情
     */
    MdUnitMeasure queryById(Long id);

    /**
     * 根据ID删除计量单位（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除计量单位（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改计量单位（只更新非 null 字段）
     */
    int updateById(MdUnitMeasure mdUnitMeasure);

    /**
     * 新增计量单位
     */
    int save(MdUnitMeasure mdUnitMeasure);

    /**
     * 根据编码查询计量单位（编码唯一，最多一条）
     */
    MdUnitMeasure queryByMeasureCode(String measureCode);

}
