package com.elevn.mes.dv.mapper;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.dv.entity.DvMachinery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 设备表 Mapper 接口
 *
 * 本次只为"工作站详情页选料下拉"提供查询能力：
 * 分页条件查询 + 按 ID 查详情。增删改等设备模块（dv 线）的同学自己补。
 *
 */
public interface DvMachineryMapper {

    /**
     * 多条件分页查询设备列表
     * @param dvMachinery 查询条件（null 字段不参与过滤）
     * @return 设备列表
     */
    List<DvMachinery> selectByCondition(@Param("dvMachinery") DvMachinery dvMachinery);

    /**
     * 根据ID查询单条设备
     * @param id 设备ID
     * @return 设备对象，不存在或已删除返回 null
     */
    DvMachinery selectById(@Param("id") Long id);

}
