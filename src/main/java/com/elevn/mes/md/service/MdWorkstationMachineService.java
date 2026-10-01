package com.elevn.mes.md.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.md.entity.MdWorkstationMachine;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作站设备资源 Service 接口
 *
 */
@Transactional
public interface MdWorkstationMachineService {

    /**
     * 分页 + 多条件查询工作站设备资源列表
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @param mdWorkstationMachine 查询条件对象，为 null 的字段不参与过滤
     */
    PageInfo<MdWorkstationMachine> page(int pageNum, int pageSize, MdWorkstationMachine mdWorkstationMachine);

    /**
     * 根据ID查询工作站设备资源详情
     */
    MdWorkstationMachine queryById(Long id);

    /**
     * 根据ID删除工作站设备资源（逻辑删除）
     */
    int deleteById(Long id);

    /**
     * 批量删除工作站设备资源（逻辑删除）
     */
    int deleteBatch(Long[] ids);

    /**
     * 修改工作站设备资源（只更新非 null 字段）
     */
    int updateById(MdWorkstationMachine mdWorkstationMachine);

    /**
     * 新增工作站设备资源
     */
    int save(MdWorkstationMachine mdWorkstationMachine);

    /**
     * 按所属ID查询工作站设备资源列表（子表一次性取出）
     */
    List<MdWorkstationMachine> queryByWorkstationId(Long workstationId);

}
