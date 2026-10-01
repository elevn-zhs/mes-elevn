package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdProductSop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品SOP的Mapper接口
 * 对应表：md_product_sop
 * 映射文件：resources/mapper/md/MdProductSopMapper.xml
 *
 *
 * */
@Mapper
public interface MdProductSopMapper  {

	/**
	 * 批量删除产品SOP（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存产品SOP信息
	 * @param mdProductSop 产品SOP对象
	 * @return 影响行数（主键会回填到对象的 sopId 上）
	 * */
	int insert(MdProductSop mdProductSop);

	/**
	 * 根据ID编辑产品SOP信息（只更新非 null 字段）
	 * @param mdProductSop 产品SOP对象（sopId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdProductSop mdProductSop);

	/**
	 * 根据ID删除产品SOP信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 产品SOPid
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询产品SOP列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdProductSop 封装查询条件
	 * @return 符合条件的产品SOP列表
	 * */
	List<MdProductSop> selectByCondition(MdProductSop mdProductSop);

	/**
	 * 按产品SOP所属主表ID查询（子表场景，按父ID一次性取出全部）
	 * @param itemId 所属ID
	 * @return 该父ID下的产品SOP列表
	 * */
	List<MdProductSop> selectByItemId(@Param("itemId") Long itemId);

	MdProductSop selectById(Long id);
}
