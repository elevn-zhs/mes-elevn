package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdProductSop;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 产品SOP Service 接口
 *
 */
@Transactional
public interface MdProductSopService {

    /**
     * 分页 + 多条件查询产品SOP列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdProductSop 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdProductSop> page(int pageNum, int pageSize, MdProductSop mdProductSop);

    /**
     * 根据ID查询产品SOP详情
     */
    MdProductSop queryById(Long id);

    /**
     * 根据ID删除产品SOP（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除产品SOP（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改产品SOP（只更新非 null 字段）
     */
    int updateById(MdProductSop mdProductSop);

    /**
     * 新增产品SOP
     */
    int save(MdProductSop mdProductSop);

    /**
     * 按所属ID查询产品SOP列表（子表一次性取出）
     */
    List<MdProductSop> queryByItemId(Long itemId);

}
