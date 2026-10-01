package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdItemVendor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料供应商 Service 接口
 *
 */
@Transactional
public interface MdItemVendorService {

    /**
     * 分页 + 多条件查询物料供应商列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdItemVendor 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdItemVendor> page(int pageNum, int pageSize, MdItemVendor mdItemVendor);

    /**
     * 根据ID查询物料供应商详情
     */
    MdItemVendor queryById(Long id);

    /**
     * 根据ID删除物料供应商（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除物料供应商（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改物料供应商（只更新非 null 字段）
     */
    int updateById(MdItemVendor mdItemVendor);

    /**
     * 新增物料供应商
     */
    int save(MdItemVendor mdItemVendor);

    /**
     * 按所属物料ID查询列表（物料详情 tab 一次取出）
     */
    List<MdItemVendor> queryByItemId(Long itemId);

}
