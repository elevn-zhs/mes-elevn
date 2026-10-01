package com.elevn.mes.tm.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.tm.entity.TmToolType;

/**
 * 工装夹具类型表 Service 接口
 *
 * 本次只为"工作站详情页选料下拉"提供查询能力，增删改留给工装模块（tm 线）的同学实现。
 *
 */
public interface TmToolTypeService {

    /**
     * 分页 + 多条件查询工装夹具类型列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param tmToolType 查询条件
     * @return 分页结果
     */
    PageInfo<TmToolType> page(int pageNum, int pageSize, TmToolType tmToolType);

    /**
     * 根据ID查询工装夹具类型详情
     * @param id 工装夹具类型ID
     * @return 工装夹具类型对象，不存在或已删除返回 null
     */
    TmToolType queryById(Long id);

}
