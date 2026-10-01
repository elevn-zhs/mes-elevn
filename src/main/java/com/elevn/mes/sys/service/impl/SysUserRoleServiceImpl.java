package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysUserRole;
import com.elevn.mes.sys.mapper.SysUserRoleMapper;
import com.elevn.mes.sys.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserRoleServiceImpl implements SysUserRoleService {

    /**
     * 登录模块还没做，先写死占位，后续改成取当前登录用户
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 给用户重新分配角色：先软删该用户原有的全部角色，再逐条写入本次提交的
     *
     * 【为什么一定加事务】
     * 这两步必须是一个整体。假设删完旧的、插入新角色时炸了（比如某个 roleId 在库里并不存在），
     * 没有事务的话，这个用户的角色就被清空了 —— 一个好好的管理员，刷新页面突然啥也点不了。
     * 加上 @Transactional，出异常会整体回滚，角色维持原样，页面只是提示"分配失败"。
     *
     * 【为什么不用批量 insert】
     * 循环 insert 一次插入一条，数据量小的时候完全够用，SQL 还好排错。
     * 真到了要给一个用户配几百个角色的场景，再改成 Mapper 里 foreach 拼批量 SQL，
     * 或者干脆用 MP 的 saveBatch。现在是教学项目，可读性优先。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int assignRole(Long userId, Long[] roleIds) {
        if (userId == null){
            throw new BusinessException("用户ID不能为空，无法确定给谁分配角色");
        }
        // 一步：清空原有角色（软删）
        sysUserRoleMapper.deleteByUserId(userId);

        if (roleIds == null || roleIds.length == 0){
            return 0;
        }

        int count = 0;
        for (Long roleId : roleIds){
            if (roleId == null){
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRole.setCreateBy(DEFAULT_OPERATOR);
            sysUserRole.setCreateTime(LocalDateTime.now());
            count += sysUserRoleMapper.insert(sysUserRole);
        }
        return count;
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        if (userId == null){
            throw new BusinessException("用户ID不能为空");
        }
        return sysUserRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public PageInfo<SysUserRole> page(int pageNum, int pageSize, SysUserRole sysUserRole) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysUserRole> list = sysUserRoleMapper.selectByCondition(sysUserRole);
        return new PageInfo<>(list);
    }

    @Override
    public SysUserRole queryById(Long id) {
        return sysUserRoleMapper.selectById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysUserRole sysUserRole) {
        sysUserRole.setCreateBy(DEFAULT_OPERATOR);
        sysUserRole.setUpdateBy(DEFAULT_OPERATOR);
        sysUserRole.setCreateTime(LocalDateTime.now());
        sysUserRole.setUpdateTime(LocalDateTime.now());
        return sysUserRoleMapper.insert(sysUserRole);
    }

    @Override
    public int updateById(SysUserRole sysUserRole) {
        sysUserRole.setUpdateBy(DEFAULT_OPERATOR);
        sysUserRole.setUpdateTime(LocalDateTime.now());
        return sysUserRoleMapper.updateById(sysUserRole);
    }

    @Override
    public int deleteById(Long id) {
        return sysUserRoleMapper.deleteById(id);
    }
}
