package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdWorkstation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作站 Service 接口
 *
 */
@Transactional
public interface MdWorkstationService {

    /**
     * 分页 + 多条件查询工作站列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdWorkstation 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdWorkstation> page(int pageNum, int pageSize, MdWorkstation mdWorkstation);

    /**
     * 根据ID查询工作站详情
     */
    MdWorkstation queryById(Long id);

    /**
     * 根据ID删除工作站（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除工作站（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改工作站（只更新非 null 字段）
     */
    int updateById(MdWorkstation mdWorkstation);

    /**
     * 新增工作站
     */
    int save(MdWorkstation mdWorkstation);

    /**
     * 根据编码查询工作站（编码唯一，最多一条）
     */
    MdWorkstation queryByWorkstationCode(String workstationCode);

    /**
     * 按工序ID查询可承担该工序的启用工作站（排产选工作站的下拉数据源）
     *
     * 一道工序可能对应多个工作站（如"焊接"既有手工焊接站也有波峰焊线），
     * 排产时要让用户按实际产能选，所以返回列表。
     */
    List<MdWorkstation> queryByProcessId(Long processId);

}
