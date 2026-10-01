package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联的Mapper接口
 * 对应表：sys_user_role
 * 映射文件：resources/mapper/SysUserRoleMapper.xml
 *
 *
 * */
@Mapper
public interface SysUserRoleMapper {

	/**
	 * 保存用户角色关联信息
	 * @param sysUserRole 用户角色关联对象
	 * @return 影响行数（主键会回填到对象的 id 上）
	 * */
	int insert(SysUserRole sysUserRole);

	/**
	 * 根据ID编辑用户角色关联信息（只更新非 null 字段）
	 * @param sysUserRole 用户角色关联对象（id 必填）
	 * @return 影响行数
	 * */
	int updateById(SysUserRole sysUserRole);

	/**
	 * 根据ID删除用户角色关联信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 用户角色关联id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询用户角色关联列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysUserRole 封装查询条件
	 * @return 符合条件的用户角色关联列表
	 * */
	List<SysUserRole> selectByCondition(SysUserRole sysUserRole);

	/**
	 * 根据主键ID查询单条关联记录
	 * @param id 关联记录的主键id
	 * @return 关联对象（连同 userId / roleId 一起查出），不存在返回 null
	 * */
	SysUserRole selectById(Long id);

	/**
	 * 根据用户ID查询已分配的角色ID集合（回显用户角色勾选框用）
	 * @param userId 用户ID
	 * @return 角色ID列表
	 * */
	List<Long> selectRoleIdsByUserId(Long userId);

	/**
	 * 清空某个用户的全部角色（重新分配前先清理，逻辑删除）
	 * @param userId 用户ID
	 * @return 影响行数
	 * */
	int deleteByUserId(Long userId);
}
