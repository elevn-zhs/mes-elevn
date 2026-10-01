package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdWorkshop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车间的Mapper接口
 * 对应表：md_workshop
 * 映射文件：resources/mapper/md/MdWorkshopMapper.xml
 *
 *
 * */
@Mapper
public interface MdWorkshopMapper  {

	/**
	 * 批量删除车间（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存车间信息
	 * @param mdWorkshop 车间对象
	 * @return 影响行数（主键会回填到对象的 workshopId 上）
	 * */
	int insert(MdWorkshop mdWorkshop);

	/**
	 * 根据ID编辑车间信息（只更新非 null 字段）
	 * @param mdWorkshop 车间对象（workshopId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdWorkshop mdWorkshop);

	/**
	 * 根据ID删除车间信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 车间id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询车间列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdWorkshop 封装查询条件
	 * @return 符合条件的车间列表
	 * */
	List<MdWorkshop> selectByCondition(MdWorkshop mdWorkshop);

	/**
	 * 根据车间编码查询（新增/修改时校验编码是否重复）
	 * @param workshopCode 编码
	 * @return 车间对象，不存在返回 null
	 * */
	MdWorkshop selectByWorkshopCode(@Param("workshopCode") String workshopCode);

	MdWorkshop selectById(Long id);
}
