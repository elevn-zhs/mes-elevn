package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdProductSip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品SIP的Mapper接口
 * 对应表：md_product_sip
 * 映射文件：resources/mapper/md/MdProductSipMapper.xml
 *
 *
 * */
@Mapper
public interface MdProductSipMapper  {

	/**
	 * 批量删除产品SIP（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存产品SIP信息
	 * @param mdProductSip 产品SIP对象
	 * @return 影响行数（主键会回填到对象的 sipId 上）
	 * */
	int insert(MdProductSip mdProductSip);

	/**
	 * 根据ID编辑产品SIP信息（只更新非 null 字段）
	 * @param mdProductSip 产品SIP对象（sipId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdProductSip mdProductSip);

	/**
	 * 根据ID删除产品SIP信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 产品SIPid
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询产品SIP列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdProductSip 封装查询条件
	 * @return 符合条件的产品SIP列表
	 * */
	List<MdProductSip> selectByCondition(MdProductSip mdProductSip);

	/**
	 * 按产品SIP所属主表ID查询（子表场景，按父ID一次性取出全部）
	 * @param itemId 所属ID
	 * @return 该父ID下的产品SIP列表
	 * */
	List<MdProductSip> selectByItemId(@Param("itemId") Long itemId);

	MdProductSip selectById(Long id);
}
