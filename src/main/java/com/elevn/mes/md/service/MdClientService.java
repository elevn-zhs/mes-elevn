package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户 Service 接口
 *
 */
@Transactional
public interface MdClientService {

    /**
     * 分页 + 多条件查询客户列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdClient 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdClient> page(int pageNum, int pageSize, MdClient mdClient);

    /**
     * 根据ID查询客户详情
     */
    MdClient queryById(Long id);

    /**
     * 根据ID删除客户（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除客户（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改客户（只更新非 null 字段）
     */
    int updateById(MdClient mdClient);

    /**
     * 新增客户
     */
    int save(MdClient mdClient);

    /**
     * 根据编码查询客户（编码唯一，最多一条）
     */
    MdClient queryByClientCode(String clientCode);

}
