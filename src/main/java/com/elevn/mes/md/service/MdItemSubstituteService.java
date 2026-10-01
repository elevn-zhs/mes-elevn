package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdItemSubstitute;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料替代品 Service 接口
 *
 */
@Transactional
public interface MdItemSubstituteService {

    /**
     * 分页 + 多条件查询物料替代品列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdItemSubstitute 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdItemSubstitute> page(int pageNum, int pageSize, MdItemSubstitute mdItemSubstitute);

    /**
     * 根据ID查询物料替代品详情
     */
    MdItemSubstitute queryById(Long id);

    /**
     * 根据ID删除物料替代品（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除物料替代品（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改物料替代品（只更新非 null 字段）
     */
    int updateById(MdItemSubstitute mdItemSubstitute);

    /**
     * 新增物料替代品
     */
    int save(MdItemSubstitute mdItemSubstitute);

    /**
     * 按所属物料ID查询列表（物料详情 tab 一次取出）
     */
    List<MdItemSubstitute> queryByItemId(Long itemId);

}
