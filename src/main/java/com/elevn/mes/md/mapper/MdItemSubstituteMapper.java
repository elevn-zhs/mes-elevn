package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdItemSubstitute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物料替代品的Mapper接口
 * 对应表：md_item_substitute
 * 映射文件：resources/mapper/md/MdItemSubstituteMapper.xml
 *
 *
 * */
@Mapper
public interface MdItemSubstituteMapper  {

	/**
	 * 批量删除物料替代品（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存物料替代品信息
	 * @param mdItemSubstitute 物料替代品对象
	 * @return 影响行数（主键会回填到对象的 substituteId 上）
	 * */
	int insert(MdItemSubstitute mdItemSubstitute);

	/**
	 * 根据ID编辑物料替代品信息（只更新非 null 字段）
	 * @param mdItemSubstitute 物料替代品对象（substituteId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdItemSubstitute mdItemSubstitute);

	/**
	 * 根据ID删除物料替代品信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 物料替代品id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询物料替代品列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdItemSubstitute 封装查询条件
	 * @return 符合条件的物料替代品列表
	 * */
	List<MdItemSubstitute> selectByCondition(MdItemSubstitute mdItemSubstitute);

	/**
	 * 按所属主表ID查询（子表场景，按父ID一次性取出全部）
	 * @param itemId 所属物料ID
	 * @return 该物料下的记录
	 * */
	List<MdItemSubstitute> selectByItemId(@Param("itemId") Long itemId);

	MdItemSubstitute selectById(Long id);

	/**
	 * 按「业务唯一键」查是否已存在（新增查重、修改时排除自身都要用到）
	 * @return 命中的物料替代品，不存在返回 null
	 * */
	MdItemSubstitute selectByUniqueKey(@Param("itemId") Long itemId, @Param("targetId") Long targetId);
}
