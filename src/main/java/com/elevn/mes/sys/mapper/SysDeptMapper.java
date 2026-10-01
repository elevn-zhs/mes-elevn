package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门的Mapper接口
 * 对应表：sys_dept
 * 映射文件：resources/mapper/SysDeptMapper.xml
 *
 *
 * */
@Mapper
public interface SysDeptMapper  {


	/**
	 * 通过parentId查询所有子部门列表
	 * @param parentId
	 * @return
	 */
	List<SysDept> selectByParentId(Long parentId);


	/**
	 * 保存部门信息
	 * @param sysDept 部门对象
	 * @return 影响行数（主键会回填到对象的 deptId 上）
	 * */
	int insert(SysDept sysDept);

	/**
	 * 根据ID编辑部门信息（只更新非 null 字段）
	 * @param sysDept 部门对象（deptId 必填）
	 * @return 影响行数
	 * */
	int updateById(SysDept sysDept);

	/**
	 * 根据ID删除部门信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 部门id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询部门列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysDept 封装查询条件
	 * @return 符合条件的部门列表
	 * */
	List<SysDept> selectByCondition(SysDept sysDept);

	SysDept selectById(Long id);
}
