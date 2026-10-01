package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联的Mapper接口
 * 对应表：sys_role_menu
 * 映射文件：resources/mapper/SysRoleMenuMapper.xml
 *
 *
 * */
@Mapper
public interface SysRoleMenuMapper  {

	/**
	 * 保存角色权限关联信息
	 * @param sysRoleMenu 角色权限关联对象
	 * @return 影响行数（主键会回填到对象的 id 上）
	 * */
	int insert(SysRoleMenu sysRoleMenu);

	/**
	 * 根据ID编辑角色权限关联信息（只更新非 null 字段）
	 * @param sysRoleMenu 角色权限关联对象（id 必填）
	 * @return 影响行数
	 * */
	int updateById(SysRoleMenu sysRoleMenu);

	/**
	 * 根据ID删除角色权限关联信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 角色权限关联id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询角色权限关联列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysRoleMenu 封装查询条件
	 * @return 符合条件的角色权限关联列表
	 * */
	List<SysRoleMenu> selectByCondition(SysRoleMenu sysRoleMenu);

	/**
	 * 根据主键ID查询单条关联记录
	 * @param id 关联记录的主键id
	 * @return 关联对象（连同 roleId / menuId 一起查出），不存在返回 null
	 * */
	SysRoleMenu selectById(Long id);

	/**
	 * 根据角色ID查询已授权的权限ID集合（回显角色授权树用）
	 * @param roleId 角色ID
	 * @return 权限ID列表
	 * */
	List<Long> selectMenuIdsByRoleId(Long roleId);

	/**
	 * 清空某个角色的全部授权（重新授权前先清理，逻辑删除）
	 * @param roleId 角色ID
	 * @return 影响行数
	 * */
	int deleteByRoleId(Long roleId);
}
