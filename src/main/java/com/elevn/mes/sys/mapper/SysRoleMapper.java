package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色的Mapper接口
 * 对应表：sys_role
 * 映射文件：resources/mapper/SysRoleMapper.xml
 *
 *
 * */
@Mapper
public interface SysRoleMapper {

	/**
	 * 保存角色信息
	 * @param sysRole 角色对象
	 * @return 影响行数（主键会回填到对象的 roleId 上）
	 * */
	int insert(SysRole sysRole);

	/**
	 * 根据ID编辑角色信息（只更新非 null 字段）
	 * @param sysRole 角色对象（roleId 必填）
	 * @return 影响行数
	 * */
	int updateById(SysRole sysRole);

	/**
	 * 根据ID删除角色信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 角色id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询角色列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysRole 封装查询条件
	 * @return 符合条件的角色列表
	 * */
	List<SysRole> selectByCondition(SysRole sysRole);

	/**
	 * 根据用户ID查询其拥有的角色（用户 - 角色 关联查询）
	 * @param userId 用户ID
	 * @return 角色列表
	 * */
	List<SysRole> selectRolesByUserId(Long userId);

	SysRole selectById(Long id);

	SysRole selectByRoleName(String roleName);

    List<SysRole> selectAll();
}
