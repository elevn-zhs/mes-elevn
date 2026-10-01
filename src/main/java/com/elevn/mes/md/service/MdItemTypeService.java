package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdItemType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料产品分类 Service 接口
 *
 */
@Transactional
public interface MdItemTypeService {

    /**
     * 根据type查询物料产品分类；
     * @param type
     * @return 查询树结构
     */
    List<MdItemType> queryTree(String type);

    /**
     * 分页 + 多条件查询物料产品分类列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdItemType 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdItemType> page(int pageNum, int pageSize, MdItemType mdItemType);

    /**
     * 根据ID查询物料产品分类详情
     */
    MdItemType queryById(Long id);

    /**
     * 根据ID删除物料产品分类（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除物料产品分类（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改物料产品分类（只更新非 null 字段）
     */
    int updateById(MdItemType mdItemType);

    /**
     * 新增物料产品分类
     */
    int save(MdItemType mdItemType);

    /**
     * 根据编码查询物料产品分类（编码唯一，最多一条）
     */
    MdItemType queryByItemTypeCode(String itemTypeCode);

    List<MdItemType> queryByParentId(Long parentId);
}
