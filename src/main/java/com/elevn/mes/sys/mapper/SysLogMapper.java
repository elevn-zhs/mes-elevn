package com.elevn.mes.sys.mapper;

import com.elevn.mes.sys.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志的Mapper接口
 * 对应表：sys_log
 * 映射文件：resources/mapper/SysLogMapper.xml
 *
 *
 * */
@Mapper
public interface SysLogMapper {

	/**
	 * 保存操作日志信息
	 * @param sysLog 操作日志对象
	 * @return 影响行数（主键会回填到对象的 logId 上）
	 * */
	int insert(SysLog sysLog);

	/**
	 * 根据ID编辑操作日志信息（只更新非 null 字段）
	 * @param sysLog 操作日志对象（logId 必填）
	 * @return 影响行数
	 * */
	int updateById(SysLog sysLog);

	/**
	 * 根据ID删除操作日志信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 操作日志id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询操作日志列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param sysLog 封装查询条件
	 * @return 符合条件的操作日志列表
	 * */
	List<SysLog> selectByCondition(SysLog sysLog);

	SysLog selectById(Long id);
}
