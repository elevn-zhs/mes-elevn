package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysDept;
import com.elevn.mes.sys.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型的Mapper接口
 * 对应表：sys_dict_type
 * 映射文件：resources/mapper/SysDictTypeMapper.xml
 *
 *
 * */
@Mapper
public interface SysDictTypeMapper  {
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存字典类型信息
	 * @param sysDictType 字典类型对象
	 * @return 影响行数（主键会回填到对象的 dictId 上）
	 * */
	int insert(SysDictType sysDictType);

	/**
	 * 根据ID编辑字典类型信息（只更新非 null 字段）
	 * @param sysDictType 字典类型对象（dictId 必填）
	 * @return 影响行数
	 * */
	int updateById(SysDictType sysDictType);

	/**
	 * 根据ID删除字典类型信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 字典类型id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询字典类型列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysDictType 封装查询条件
	 * @return 符合条件的字典类型列表
	 * */
	List<SysDictType> selectByCondition(SysDictType sysDictType);

	/**
	 * 根据字典类型查询（新增时校验 dictType 是否重复）
	 * @param dictType 字典类型英文key
	 * @return 字典类型对象，不存在返回 null
	 * */
	SysDictType selectByDictType(String dictType);

	SysDictType selectById(Long id);

	/**
	 *
	 * @param dictType
	 * @return
	 */
	SysDictType selectByType(String dictType);
}
