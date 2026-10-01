package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据的Mapper接口
 * 对应表：sys_dict_data
 * 映射文件：resources/mapper/SysDictDataMapper.xml
 *
 *
 * */
@Mapper
public interface SysDictDataMapper  {

	/**
	 * 保存字典数据信息
	 * @param sysDictData 字典数据对象
	 * @return 影响行数（主键会回填到对象的 dictCode 上）
	 * */
	int insert(SysDictData sysDictData);

	/**
	 * 根据ID编辑字典数据信息（只更新非 null 字段）
	 * @param sysDictData 字典数据对象（dictCode 必填）
	 * @return 影响行数
	 * */
	int updateById(SysDictData sysDictData);

	/**
	 * 根据ID删除字典数据信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 字典数据id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询字典数据列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysDictData 封装查询条件
	 * @return 符合条件的字典数据列表
	 * */
	List<SysDictData> selectByCondition(SysDictData sysDictData);

	/**
	 * 根据字典类型查询该字典下的全部条目（下拉框数据源）
	 * @param dictType 字典类型英文key，如 sys_user_sex
	 * @return 字典条目列表，按 dict_sort 升序
	 * */
	List<SysDictData> selectByDictType(String dictType);

	SysDictData selectById(Long id);
}
