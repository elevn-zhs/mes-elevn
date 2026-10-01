package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户的Mapper接口
 * 对应表：sys_user
 * 映射文件：resources/mapper/SysUserMapper.xml
 *
 *
 * */
@Mapper
public interface SysUserMapper  {

	/**
	 * 保存用户信息
	 * @param sysUser 用户对象
	 * @return 影响行数（主键会回填到对象的 userId 上）
	 * */
	int insert(SysUser sysUser);

	/**
	 * 根据ID编辑用户信息（只更新非 null 字段）
	 * @param sysUser 用户对象（userId 必填）
	 * @return 影响行数
	 * */
	int updateById(SysUser sysUser);

	/**
	 * 根据ID删除用户信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 用户id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询用户列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysUser 封装查询条件
	 * @return 符合条件的用户列表
	 * */
	List<SysUser> selectByCondition(SysUser sysUser);

	/**
	 * 根据登录账号查询用户（登录用，密码一并查出）
	 * @param userName 登录账号
	 * @return 用户对象，不存在返回 null
	 * */
	SysUser selectByUserName(String userName);

	SysUser selectById(Long id);
}
