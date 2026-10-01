package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdProductSip;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 产品SIP Service 接口
 *
 */
@Transactional
public interface MdProductSipService {

    /**
     * 分页 + 多条件查询产品SIP列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdProductSip 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdProductSip> page(int pageNum, int pageSize, MdProductSip mdProductSip);

    /**
     * 根据ID查询产品SIP详情
     */
    MdProductSip queryById(Long id);

    /**
     * 根据ID删除产品SIP（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除产品SIP（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改产品SIP（只更新非 null 字段）
     */
    int updateById(MdProductSip mdProductSip);

    /**
     * 新增产品SIP
     */
    int save(MdProductSip mdProductSip);

    /**
     * 按所属ID查询产品SIP列表（子表一次性取出）
     */
    List<MdProductSip> queryByItemId(Long itemId);

}
