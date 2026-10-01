package com.elevn.mes.dv.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.dv.entity.DvMachinery;

/**
 * 设备表 Service 接口
 *
 * 本次只为"工作站详情页选料下拉"提供查询能力，增删改留给设备模块（dv 线）的同学实现。
 *
 */
public interface DvMachineryService {

    /**
     * 分页 + 多条件查询设备列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param dvMachinery 查询条件
     * @return 分页结果
     */
    PageInfo<DvMachinery> page(int pageNum, int pageSize, DvMachinery dvMachinery);

    /**
     * 根据ID查询设备详情
     * @param id 设备ID
     * @return 设备对象，不存在或已删除返回 null
     */
    DvMachinery queryById(Long id);

}
