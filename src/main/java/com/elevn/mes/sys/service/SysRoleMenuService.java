package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysRoleMenu;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色与菜单（权限）关联的业务接口
 *
 */
@Transactional
public interface SysRoleMenuService {

    /**
     * 查询某个角色已经授权过的菜单ID集合
     * 用途：打开「角色授权」弹窗时，把菜单树上的节点回显成勾选状态
     * @param roleId 角色ID
     * @return 已授权的菜单ID列表，没有授权过返回空集合
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 给角色重新授权（全量覆盖）
     * 注意这里是「先清空旧授权，再写入新授权」，至于为什么这么选，见实现类的注释说明
     * @param roleId 角色ID
     * @param menuIds 本次提交的菜单ID数组，传空数组表示取消该角色的全部授权
     * @return 本次实际写入的关联记录数
     */
    int assignMenu(Long roleId, Long[] menuIds);

    /**
     * 关联本身也是一张表，基础的增删改查一样保留
     * 日常排查问题（比如"这个角色到底授权了啥"）时用得到
     */
    PageInfo<SysRoleMenu> page(int pageNum, int pageSize, SysRoleMenu sysRoleMenu);

    SysRoleMenu queryById(Long id);

    int save(SysRoleMenu sysRoleMenu);

    int updateById(SysRoleMenu sysRoleMenu);

    int deleteById(Long id);

}
