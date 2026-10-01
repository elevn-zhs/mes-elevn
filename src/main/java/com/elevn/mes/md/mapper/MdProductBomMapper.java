package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdProductBom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品BOM的Mapper接口
 * 对应表：md_product_bom
 * 映射文件：resources/mapper/md/MdProductBomMapper.xml
 *
 *
 * */
@Mapper
public interface MdProductBomMapper  {

	/**
	 * 批量删除产品BOM（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存产品BOM信息
	 * @param mdProductBom 产品BOM对象
	 * @return 影响行数（主键会回填到对象的 bomId 上）
	 * */
	int insert(MdProductBom mdProductBom);

	/**
	 * 根据ID编辑产品BOM信息（只更新非 null 字段）
	 * @param mdProductBom 产品BOM对象（bomId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdProductBom mdProductBom);

	/**
	 * 根据ID删除产品BOM信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 产品BOMid
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询产品BOM列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdProductBom 封装查询条件
	 * @return 符合条件的产品BOM列表
	 * */
	List<MdProductBom> selectByCondition(MdProductBom mdProductBom);

	/**
	 * 按产品BOM所属主表ID查询（子表场景，按父ID一次性取出全部）
	 * @param itemId 所属ID
	 * @return 该父ID下的产品BOM列表
	 * */
	List<MdProductBom> selectByItemId(@Param("itemId") Long itemId);

	MdProductBom selectById(Long id);
}
