package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysRole;

import java.util.List;

public interface SysRoleService {
    int save(SysRole role);

    int updateById(SysRole role);

    SysRole queryById(Long id);

    int deleteById(Long id);

    PageInfo<SysRole> page(int pageNum,int pageSize,SysRole role);

    List<SysRole> queryAll();
}
