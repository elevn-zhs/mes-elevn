package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysRoleMenu;
import com.elevn.mes.sys.mapper.SysRoleMenuMapper;
import com.elevn.mes.sys.service.SysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysRoleMenuServiceImpl implements SysRoleMenuService {

    /**
     * 登录模块还没做，先写死一个操作人占位。
     * 等做完鉴权，这里会改成从 ThreadLocal / SecurityContext 里取当前登录用户。
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 【给角色授权】为什么用「先清空再全量写入」，而不是「差量更新」
     *
     * 授权页的交互形态决定了这两种写法的取舍：
     *
     * 1. 差量更新（推荐进阶做法）
     *    先 selectMenuIdsByRoleId 查库里的旧集合，跟本次提交的新集合做三个差集：
     *      - 新有旧无 → insert
     *      - 旧有新无 → deleteByRoleId 单条软删（现在 Mapper 还没有这个方法，需要补）
     *      - 两边都有 → 不动
     *    优点：不会留下垃圾数据，主键也不会无意义地疯涨；
     *    缺点：代码长一轮，要在 Service 里写集合比对，初学者容易绕晕。
     *
     * 2. 先清后插（本项目采用）
     *    一条 deleteByRoleId 软删全部旧记录，然后循环 insert 新记录。
     *    优点：逻辑直白，三行看懂，课堂上讲「@Transactional 保证这一步要么全成要么全败」很顺；
     *    缺点：每次点保存都会产生一轮新的自增主键，软删记录会在表里堆积，
     *          所以 sys_role_menu 这类中间表要定期清历史 del_flag='1' 的数据（或者干脆物理删除）。
     *
     * 另外注意：本表没有 (role_id, menu_id) 唯一索引，只靠数据库兜不住重复授权，
     * 所以「同一组合不能出现两条有效记录」这件事，必须靠 service 层的这次清空来保证。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int assignMenu(Long roleId, Long[] menuIds) {
        if (roleId == null){
            throw new BusinessException("角色ID不能为空，无法确定给谁授权");
        }
        // 第一步：清空该角色原有的全部授权（软删，del_flag = '1'）
        sysRoleMenuMapper.deleteByRoleId(roleId);

        // 一个菜单都没勾，视为取消授权，到这里就可以收工了
        if (menuIds == null || menuIds.length == 0){
            return 0;
        }

        // 第二步：把本次勾选的菜单逐条写回
        int count = 0;
        for (Long menuId : menuIds){
            if (menuId == null){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setRoleId(roleId);
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setCreateBy(DEFAULT_OPERATOR);
            sysRoleMenu.setCreateTime(LocalDateTime.now());
            count += sysRoleMenuMapper.insert(sysRoleMenu);
        }
        return count;
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        if (roleId == null){
            throw new BusinessException("角色ID不能为空");
        }
        return sysRoleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public PageInfo<SysRoleMenu> page(int pageNum, int pageSize, SysRoleMenu sysRoleMenu) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysRoleMenu> list = sysRoleMenuMapper.selectByCondition(sysRoleMenu);
        return new PageInfo<>(list);
    }

    @Override
    public SysRoleMenu queryById(Long id) {
        return sysRoleMenuMapper.selectById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysRoleMenu sysRoleMenu) {
        sysRoleMenu.setCreateBy(DEFAULT_OPERATOR);
        sysRoleMenu.setUpdateBy(DEFAULT_OPERATOR);
        sysRoleMenu.setCreateTime(LocalDateTime.now());
        sysRoleMenu.setUpdateTime(LocalDateTime.now());
        return sysRoleMenuMapper.insert(sysRoleMenu);
    }

    @Override
    public int updateById(SysRoleMenu sysRoleMenu) {
        sysRoleMenu.setUpdateBy(DEFAULT_OPERATOR);
        sysRoleMenu.setUpdateTime(LocalDateTime.now());
        return sysRoleMenuMapper.updateById(sysRoleMenu);
    }

    @Override
    public int deleteById(Long id) {
        return sysRoleMenuMapper.deleteById(id);
    }
}
