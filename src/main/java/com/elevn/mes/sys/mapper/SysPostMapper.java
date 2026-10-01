package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位的Mapper接口
 * 对应表：sys_post
 * 映射文件：resources/mapper/SysPostMapper.xml
 *
 *
 * */
@Mapper
public interface SysPostMapper  {

	/**
	 * 保存岗位信息
	 * @param sysPost 岗位对象
	 * @return 影响行数（主键会回填到对象的 postId 上）
	 * */
	int insert(SysPost sysPost);

	/**
	 * 根据ID编辑岗位信息（只更新非 null 字段）
	 * @param sysPost 岗位对象（postId 必填）
	 * @return 影响行数
	 * */
	int updateById(SysPost sysPost);

	/**
	 * 根据ID删除岗位信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 岗位id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询岗位列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysPost 封装查询条件
	 * @return 符合条件的岗位列表
	 * */
	List<SysPost> selectByCondition(SysPost sysPost);

	SysPost selectById(Long id);
}
