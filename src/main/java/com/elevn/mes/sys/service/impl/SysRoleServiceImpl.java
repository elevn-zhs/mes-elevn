package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysRole;
import com.elevn.mes.sys.mapper.SysRoleMapper;
import com.elevn.mes.sys.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {
    @Autowired
    private SysRoleMapper roleMapper;
    @Override
    public PageInfo<SysRole> page(int pageNum, int pageSize, SysRole role) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysRole> SysRoles = roleMapper.selectByCondition(role);
        return new PageInfo<>(SysRoles);
    }

    @Override
    public List<SysRole> queryAll() {
        return roleMapper.selectAll();
    }

    @Override
    public SysRole queryById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return roleMapper.deleteById(id);
    }

    @Override
    public int updateById(SysRole role) {
        SysRole dbRole = roleMapper.selectByRoleName(role.getRoleName());
        if(dbRole != null && !dbRole.getRoleId().equals(role.getRoleId())) {
            throw new BusinessException("角色名字已经存在，请更换");
        }
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy("admin");
        return roleMapper.updateById(role);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysRole role) {
        SysRole dbRole = roleMapper.selectByRoleName(role.getRoleName());
        if(dbRole != null ) {
            throw new BusinessException("角色名字已经存在，请更换");
        }
        role.setCreateTime(LocalDateTime.now());
        role.setCreateBy("admin");
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateBy("admin");
        return roleMapper.insert(role);
    }
}
