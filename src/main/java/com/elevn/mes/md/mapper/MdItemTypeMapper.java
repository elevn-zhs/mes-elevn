package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdItemType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物料产品分类的Mapper接口
 * 对应表：md_item_type
 * 映射文件：resources/mapper/md/MdItemTypeMapper.xml
 *
 *
 * */
@Mapper
public interface MdItemTypeMapper  {

	/**
	 * 批量删除物料产品分类（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存物料产品分类信息
	 * @param mdItemType 物料产品分类对象
	 * @return 影响行数（主键会回填到对象的 itemTypeId 上）
	 * */
	int insert(MdItemType mdItemType);

	/**
	 * 根据ID编辑物料产品分类信息（只更新非 null 字段）
	 * @param mdItemType 物料产品分类对象（itemTypeId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdItemType mdItemType);

	/**
	 * 根据ID删除物料产品分类信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 物料产品分类id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询物料产品分类列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdItemType 封装查询条件
	 * @return 符合条件的物料产品分类列表
	 * */
	List<MdItemType> selectByCondition(MdItemType mdItemType);

	/**
	 * 根据物料产品分类编码查询（新增/修改时校验编码是否重复）
	 * @param itemTypeCode 编码
	 * @return 物料产品分类对象，不存在返回 null
	 * */
	MdItemType selectByItemTypeCode(@Param("itemTypeCode") String itemTypeCode);

	MdItemType selectById(Long id);

    List<MdItemType> selectByParentTypeIdAndType(@Param("parentTypeId") Long parentTypeId,@Param("type") String type);

    List<MdItemType> selectByParentId(Long parentId);

	Integer selectCountByParentId(Long itemTypeId);
}
