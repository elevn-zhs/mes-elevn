package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysCodingRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据的Mapper接口
 * 对应表：sys_dict_data
 * 映射文件：resources/mapper/SysCodingRuleMapper.xml
 *
 *
 * */
@Mapper
public interface SysCodingRuleMapper {

	/**
	 * 保存字典数据信息
	 * @param sysDictData 字典数据对象
	 * @return 影响行数（主键会回填到对象的 dictCode 上）
	 * */
	int insert(SysCodingRule sysDictData);

	/**
	 * 根据ID编辑字典数据信息（只更新非 null 字段）
	 * @param sysDictData 字典数据对象（dictCode 必填）
	 * @return 影响行数
	 * */
	int updateById(SysCodingRule sysDictData);

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
	List<SysCodingRule> selectByCondition(SysCodingRule sysDictData);

	SysCodingRule selectByRuleCode(@Param("ruleCode") String ruleCode);

	SysCodingRule selectById(Long id);

	/**
	 * 序列号原子自增（取号用）
	 *
	 * 【为什么要单独一个方法，而不是"查出来 +1 再写回去"】
	 * 查-改-写两步之间，另一个请求可能也查到同一个值，
	 * 结果两个单据拿到同一个流水号。交给数据库用 `serial_number = serial_number + 1`
	 * 一条语句完成，靠行锁保证并发下也不会撞号。
	 *
	 * @param ruleCode 规则编码
	 * @return 影响行数
	 */
	int incrementSerialNumber(@Param("ruleCode") String ruleCode);
}
