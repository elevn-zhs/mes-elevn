package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdUnitMeasure;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 计量单位的Mapper接口
 * 对应表：md_unit_measure
 * 映射文件：resources/mapper/md/MdUnitMeasureMapper.xml
 *
 *
 * */
@Mapper
public interface MdUnitMeasureMapper  {

	/**
	 * 批量删除计量单位（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存计量单位信息
	 * @param mdUnitMeasure 计量单位对象
	 * @return 影响行数（主键会回填到对象的 measureId 上）
	 * */
	int insert(MdUnitMeasure mdUnitMeasure);

	/**
	 * 根据ID编辑计量单位信息（只更新非 null 字段）
	 * @param mdUnitMeasure 计量单位对象（measureId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdUnitMeasure mdUnitMeasure);

	/**
	 * 根据ID删除计量单位信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 计量单位id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询计量单位列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdUnitMeasure 封装查询条件
	 * @return 符合条件的计量单位列表
	 * */
	List<MdUnitMeasure> selectByCondition(MdUnitMeasure mdUnitMeasure);

	/**
	 * 根据计量单位编码查询（新增/修改时校验编码是否重复）
	 * @param measureCode 编码
	 * @return 计量单位对象，不存在返回 null
	 * */
	MdUnitMeasure selectByMeasureCode(@Param("measureCode") String measureCode);

	MdUnitMeasure selectById(Long id);
}
