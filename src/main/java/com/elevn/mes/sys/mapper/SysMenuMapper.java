package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限（菜单）的Mapper接口
 * 对应表：sys_menu
 * 映射文件：resources/mapper/SysMenuMapper.xml
 *
 *
 * */
@Mapper
public interface SysMenuMapper  {

	/**
	 * 保存权限（菜单）信息
	 * @param sysMenu 权限（菜单）对象
	 * @return 影响行数（主键会回填到对象的 menuId 上）
	 * */
	int insert(SysMenu sysMenu);

	/**
	 * 根据ID编辑权限（菜单）信息（只更新非 null 字段）
	 * @param sysMenu 权限（菜单）对象（menuId 必填）
	 * @return 影响行数
	 * */
	int updateById(SysMenu sysMenu);

	/**
	 * 根据ID删除权限（菜单）信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 权限（菜单）id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询权限（菜单）列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysMenu 封装查询条件
	 * @return 符合条件的权限（菜单）列表
	 * */
	List<SysMenu> selectByCondition(SysMenu sysMenu);

	/**
	 * 根据用户ID查询其可见菜单（用户 - 角色 - 权限 三表关联）
	 * 只查 M目录 / C菜单 两类，用于构建前端侧边栏
	 * @param userId 用户ID
	 * @return 菜单列表（业务层再递归组装成树）
	 * */
	List<SysMenu> selectMenusByUserId(Long userId);

	SysMenu selectById(Long id);
}
