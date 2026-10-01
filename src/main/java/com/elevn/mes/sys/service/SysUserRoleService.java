package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysUserRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户与角色关联的业务接口
 *
 */
@Transactional
public interface SysUserRoleService {

    /**
     * 查询某个用户已经拥有的角色ID集合
     * 用途：打开「分配角色」弹窗时，把角色勾选框回显成勾选状态
     * @param userId 用户ID
     * @return 已分配的角色ID列表，没有分配过返回空集合
     */
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 给用户重新分配角色（全量覆盖）
     * @param userId 用户ID
     * @param roleIds 本次提交的角色ID数组，传空数组表示清空该用户的全部角色
     * @return 本次实际写入的关联记录数
     */
    int assignRole(Long userId, Long[] roleIds);

    PageInfo<SysUserRole> page(int pageNum, int pageSize, SysUserRole sysUserRole);

    SysUserRole queryById(Long id);

    int save(SysUserRole sysUserRole);

    int updateById(SysUserRole sysUserRole);

    int deleteById(Long id);

}
