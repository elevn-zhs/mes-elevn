package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdProductBom;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 产品BOM Service 接口
 *
 */
@Transactional
public interface MdProductBomService {

    /**
     * 分页 + 多条件查询产品BOM列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdProductBom 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdProductBom> page(int pageNum, int pageSize, MdProductBom mdProductBom);

    /**
     * 根据ID查询产品BOM详情
     */
    MdProductBom queryById(Long id);

    /**
     * 根据ID删除产品BOM（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除产品BOM（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改产品BOM（只更新非 null 字段）
     */
    int updateById(MdProductBom mdProductBom);

    /**
     * 新增产品BOM
     */
    int save(MdProductBom mdProductBom);

    /**
     * 按所属ID查询产品BOM列表（子表一次性取出）
     */
    List<MdProductBom> queryByItemId(Long itemId);

}
