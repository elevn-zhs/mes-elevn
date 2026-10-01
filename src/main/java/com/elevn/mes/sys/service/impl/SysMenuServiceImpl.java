package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysMenu;
import com.elevn.mes.sys.mapper.SysMenuMapper;
import com.elevn.mes.sys.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysMenuServiceImpl implements SysMenuService {

    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public PageInfo<SysMenu> page(int pageNum, int pageSize, SysMenu sysMenu) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysMenu> sysMenus = sysMenuMapper.selectByCondition(sysMenu);
        return new PageInfo<>(sysMenus);
    }

    @Override
    public SysMenu queryById(Long id) {
        return sysMenuMapper.selectById(id);
    }

    /**
     * 组装菜单树：把平铺的列表拼成带 children 的层级结构
     *
     * 三步走，背下来这套写法，部门树、物料分类树、BOM 树都是同一个套路：
     *   1. 先把所有节点按主键收进 Map —— key 是自己的 id，value 是它自己
     *   2. 再遍历一遍，拿每个节点的 parentId 去 Map 里找爹，找到就塞进爹的 children，找不到就当根节点
     *   3. 按 orderNum 排个序
     *
     * 关键点在第二步：因为 Map 里存的是对象引用，往 parent.children 里 add 的时候，
     * 改的就是 roots 里那个对象本身，不需要再回填一次 —— 这也是初学者最容易绕晕的地方。
     */
    @Override
    public List<SysMenu> tree(SysMenu sysMenu) {
        List<SysMenu> all = sysMenuMapper.selectByCondition(sysMenu);
        if (all == null || all.isEmpty()){
            return new ArrayList<>();
        }

        Map<Long, SysMenu> nodeMap = new LinkedHashMap<>();
        for (SysMenu node : all){
            // 实体里 children 已经初始化过，这里重新给一个干净的集合，防止重复调用时挂重
            node.setChildren(new ArrayList<>());
            nodeMap.put(node.getMenuId(), node);
        }

        List<SysMenu> roots = new ArrayList<>();
        for (SysMenu node : all){
            Long parentId = node.getParentId();
            // parentId 为空、等于 0，或者它的父节点这次没被查出来（比如父节点被停用过滤掉了）
            // 这几种情况都当作根节点处理，避免这笔数据人间蒸发
            if (parentId == null || parentId == 0L || !nodeMap.containsKey(parentId)){
                roots.add(node);
            } else {
                nodeMap.get(parentId).getChildren().add(node);
            }
        }

        sortRecursively(roots);
        return roots;
    }

    @Override
    public int deleteById(Long id) {
        // 菜单有子菜单时不许直接删，否则子菜单会变成没爹的孤儿节点，菜单树永远拼不回去
        SysMenu query = new SysMenu();
        query.setParentId(id);
        List<SysMenu> children = sysMenuMapper.selectByCondition(query);
        if (children != null && !children.isEmpty()){
            throw new BusinessException("该菜单下还有 " + children.size() + " 个子菜单，请先删除子菜单");
        }
        return sysMenuMapper.deleteById(id);
    }

    @Override
    public int updateById(SysMenu sysMenu) {
        // 不允许把菜单的父级改成它自己，否则叶子是自己、根也是自己，组装树的时候直接死循环
        if (sysMenu.getMenuId() != null && sysMenu.getMenuId().equals(sysMenu.getParentId())){
            throw new BusinessException("父级菜单不能是自己");
        }
        sysMenu.setUpdateBy(DEFAULT_OPERATOR);
        sysMenu.setUpdateTime(LocalDateTime.now());
        return sysMenuMapper.updateById(sysMenu);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysMenu sysMenu) {
        // 顶级菜单的 parent_id 统一存 0，不存 null，省得后面 SQL 判断 null 和 0 两种情况
        if (sysMenu.getParentId() == null){
            sysMenu.setParentId(0L);
        }
        sysMenu.setCreateBy(DEFAULT_OPERATOR);
        sysMenu.setUpdateBy(DEFAULT_OPERATOR);
        sysMenu.setCreateTime(LocalDateTime.now());
        sysMenu.setUpdateTime(LocalDateTime.now());
        return sysMenuMapper.insert(sysMenu);
    }

    /**
     * 递归排序：每一层都按 orderNum 从小到大排
     * orderNum 有可能为空，用 Comparator.nullsLast 把没排序号的甩到最后，别让它 NPE
     */
    private void sortRecursively(List<SysMenu> nodes) {
        if (nodes == null || nodes.isEmpty()){
            return;
        }
        nodes.sort(Comparator.comparing(SysMenu::getOrderNum,
                Comparator.nullsLast(Comparator.naturalOrder())));
        for (SysMenu node : nodes){
            sortRecursively(node.getChildren());
        }
    }
}
