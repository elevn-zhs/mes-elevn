package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdWorkshop;
import org.springframework.transaction.annotation.Transactional;

/**
 * 车间 Service 接口
 *
 */
@Transactional
public interface MdWorkshopService {

    /**
     * 分页 + 多条件查询车间列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdWorkshop 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdWorkshop> page(int pageNum, int pageSize, MdWorkshop mdWorkshop);

    /**
     * 根据ID查询车间详情
     */
    MdWorkshop queryById(Long id);

    /**
     * 根据ID删除车间（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除车间（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改车间（只更新非 null 字段）
     */
    int updateById(MdWorkshop mdWorkshop);

    /**
     * 新增车间
     */
    int save(MdWorkshop mdWorkshop);

    /**
     * 根据编码查询车间（编码唯一，最多一条）
     */
    MdWorkshop queryByWorkshopCode(String workshopCode);

}
