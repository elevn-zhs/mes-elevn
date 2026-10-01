package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysDept;

import java.util.List;

public interface SysDeptService {

    /**
     * 查询所有部门
     * @return
     */
    List<SysDept> queryAllForTree();

    int save(SysDept Dept);

    int updateById(SysDept Dept);

    SysDept queryById(Long id);

    int deleteById(Long id);

    PageInfo<SysDept> page(int pageNum,int pageSize,SysDept Dept);
}
