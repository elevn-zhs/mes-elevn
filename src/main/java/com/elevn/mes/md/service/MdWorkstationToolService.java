package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdWorkstationTool;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作站工装夹具 Service 接口
 *
 */
@Transactional
public interface MdWorkstationToolService {

    /**
     * 分页 + 多条件查询工作站工装夹具列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdWorkstationTool 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdWorkstationTool> page(int pageNum, int pageSize, MdWorkstationTool mdWorkstationTool);

    /**
     * 根据ID查询工作站工装夹具详情
     */
    MdWorkstationTool queryById(Long id);

    /**
     * 根据ID删除工作站工装夹具（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除工作站工装夹具（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改工作站工装夹具（只更新非 null 字段）
     */
    int updateById(MdWorkstationTool mdWorkstationTool);

    /**
     * 新增工作站工装夹具
     */
    int save(MdWorkstationTool mdWorkstationTool);

    /**
     * 按所属ID查询工作站工装夹具列表（子表一次性取出）
     */
    List<MdWorkstationTool> queryByWorkstationId(Long workstationId);

}
