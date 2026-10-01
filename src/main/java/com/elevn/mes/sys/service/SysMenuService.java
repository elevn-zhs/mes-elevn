package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysMenu;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SysMenuService {
    /**
     *
     * @param pageNum
     * @param pageSize
     * @param sysMenu
     * @return
     */
    PageInfo<SysMenu> page(int pageNum,int pageSize,SysMenu sysMenu);
    /**
     *
     * @param id
     * @return
     */
    SysMenu queryById(Long id);
    /**
     *
     * @param id
     * @return
     */
    int deleteById(Long id);
    /**
     *
     * @param sysMenu
     * @return
     */
    int updateById(SysMenu sysMenu);
    /**
     *
     * @param sysMenu
     * @return
     */
    int save(SysMenu sysMenu);

    /**
     * 查询菜单树（一次性查出全部，再在内存里组装成父子层级）
     *
     * 菜单这种数据量小（撑死几百条）、读多写少的数据，用「查全量 + 内存组装」最简单，
     * 一发 SQL 搞定，也不用担心递归查询数据库把库拖垮。
     * 真要是到了几千个节点，再考虑应用层缓存。
     *
     * @param sysMenu 携带查询条件，传一个空对象表示查全部
     * @return 组装好的树，根节点列表；每个节点的 children 里挂着它的子菜单
     */
    List<SysMenu> tree(SysMenu sysMenu);

}
