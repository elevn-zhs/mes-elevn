package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdWorkstation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作站的Mapper接口
 * 对应表：md_workstation
 * 映射文件：resources/mapper/md/MdWorkstationMapper.xml
 *
 *
 * */
@Mapper
public interface MdWorkstationMapper  {

	/**
	 * 批量删除工作站（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存工作站信息
	 * @param mdWorkstation 工作站对象
	 * @return 影响行数（主键会回填到对象的 workstationId 上）
	 * */
	int insert(MdWorkstation mdWorkstation);

	/**
	 * 根据ID编辑工作站信息（只更新非 null 字段）
	 * @param mdWorkstation 工作站对象（workstationId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdWorkstation mdWorkstation);

	/**
	 * 根据ID删除工作站信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 工作站id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询工作站列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdWorkstation 封装查询条件
	 * @return 符合条件的工作站列表
	 * */
	List<MdWorkstation> selectByCondition(MdWorkstation mdWorkstation);

	/**
	 * 根据工作站编码查询（新增/修改时校验编码是否重复）
	 * @param workstationCode 编码
	 * @return 工作站对象，不存在返回 null
	 * */
	MdWorkstation selectByWorkstationCode(@Param("workstationCode") String workstationCode);

	MdWorkstation selectById(Long id);

	/**
	 * 按工序ID查询可承担该工序的启用工作站（排产选工作站的下拉数据源）
	 *
	 * 一道工序在车间里可能对应多个工作站（如"焊接"既有手工焊接站也有波峰焊线），
	 * 排产时要让用户自己选，所以这里返回的是列表而不是单条。
	 *
	 * @param processId 工序ID
	 * @return 该工序下的启用工作站列表
	 * */
	List<MdWorkstation> selectByProcessId(@Param("processId") Long processId);
}
