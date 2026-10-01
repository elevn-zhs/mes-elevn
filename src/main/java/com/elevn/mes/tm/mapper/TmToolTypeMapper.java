package com.elevn.mes.tm.mapper;

import com.elevn.mes.tm.entity.TmToolType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工装夹具类型表 Mapper 接口
 *
 * 本次只为"工作站详情页选料下拉"提供查询能力：
 * 分页条件查询 + 按 ID 查详情。增删改等工装模块（tm 线）的同学自己补。
 *
 */
public interface TmToolTypeMapper {

    /**
     * 多条件分页查询工装夹具类型列表
     * @param tmToolType 查询条件（null 字段不参与过滤）
     * @return 工装夹具类型列表
     */
    List<TmToolType> selectByCondition(@Param("tmToolType") TmToolType tmToolType);

    /**
     * 根据ID查询单条工装夹具类型
     * @param id 工装夹具类型ID
     * @return 工装夹具类型对象，不存在或已删除返回 null
     */
    TmToolType selectById(@Param("id") Long id);

}
