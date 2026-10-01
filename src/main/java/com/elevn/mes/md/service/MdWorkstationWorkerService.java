package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdWorkstationWorker;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作站人力资源 Service 接口
 *
 */
@Transactional
public interface MdWorkstationWorkerService {

    /**
     * 分页 + 多条件查询工作站人力资源列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdWorkstationWorker 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdWorkstationWorker> page(int pageNum, int pageSize, MdWorkstationWorker mdWorkstationWorker);

    /**
     * 根据ID查询工作站人力资源详情
     */
    MdWorkstationWorker queryById(Long id);

    /**
     * 根据ID删除工作站人力资源（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除工作站人力资源（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改工作站人力资源（只更新非 null 字段）
     */
    int updateById(MdWorkstationWorker mdWorkstationWorker);

    /**
     * 新增工作站人力资源
     */
    int save(MdWorkstationWorker mdWorkstationWorker);

    /**
     * 按所属ID查询工作站人力资源列表（子表一次性取出）
     */
    List<MdWorkstationWorker> queryByWorkstationId(Long workstationId);

}
