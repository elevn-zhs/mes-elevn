package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdVendor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 供应商 Service 接口
 *
 */
@Transactional
public interface MdVendorService {

    /**
     * 分页 + 多条件查询供应商列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdVendor 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdVendor> page(int pageNum, int pageSize, MdVendor mdVendor);

    /**
     * 根据ID查询供应商详情
     */
    MdVendor queryById(Long id);

    /**
     * 根据ID删除供应商（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除供应商（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改供应商（只更新非 null 字段）
     */
    int updateById(MdVendor mdVendor);

    /**
     * 新增供应商
     */
    int save(MdVendor mdVendor);

    /**
     * 根据编码查询供应商（编码唯一，最多一条）
     */
    MdVendor queryByVendorCode(String vendorCode);

}
