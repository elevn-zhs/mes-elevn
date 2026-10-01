package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物料产品 Service 接口
 *
 */
@Transactional
public interface MdItemService {




    /**
     * 分页 + 多条件查询物料产品列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdItem 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdItem> page(int pageNum, int pageSize, MdItem mdItem);

    /**
     * 根据ID查询物料产品详情
     */
    MdItem queryById(Long id);

    /**
     * 根据ID删除物料产品（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除物料产品（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改物料产品（只更新非 null 字段）
     */
    int updateById(MdItem mdItem);

    /**
     * 新增物料产品
     */
    int save(MdItem mdItem);

    /**
     * 根据编码查询物料产品（编码唯一，最多一条）
     */
    MdItem queryByItemCode(String itemCode);
    /**
     * BOM 选料专用分页查询：给某个物料挑子件时，列出可以作为子件的候选物料
     *
     * 排除三类物料：
     * 1、当前物料自身 —— BOM 不允许自我引用
     * 2、当前物料的所有上级（祖先）—— 选中会形成闭环，BOM 展开 / MRP 计算无限递归
     * 3、当前物料的所有下级（子孙）—— 已经间接挂在下面了，再挂一次会造成用量重复计算
     *
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdItem 查询条件对象，为 null 的字段不参与过滤
     * @param excludeItemId 当前物料ID，必传；不传抛 BusinessException
     */
    PageInfo<MdItem> pageForBom(int pageNum, int pageSize, MdItem mdItem, Long excludeItemId);


    // AI助手工具
    List<MdItem> queryByName(String itemName);

    List<MdItem> queryByCategoryId(Long catId);

}
