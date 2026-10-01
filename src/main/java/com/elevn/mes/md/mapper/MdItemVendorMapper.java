package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdItemVendor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物料供应商的Mapper接口
 * 对应表：md_item_vendor
 * 映射文件：resources/mapper/md/MdItemVendorMapper.xml
 *
 *
 * */
@Mapper
public interface MdItemVendorMapper  {

	/**
	 * 批量删除物料供应商（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存物料供应商信息
	 * @param mdItemVendor 物料供应商对象
	 * @return 影响行数（主键会回填到对象的 itemVendorId 上）
	 * */
	int insert(MdItemVendor mdItemVendor);

	/**
	 * 根据ID编辑物料供应商信息（只更新非 null 字段）
	 * @param mdItemVendor 物料供应商对象（itemVendorId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdItemVendor mdItemVendor);

	/**
	 * 根据ID删除物料供应商信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 物料供应商id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询物料供应商列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdItemVendor 封装查询条件
	 * @return 符合条件的物料供应商列表
	 * */
	List<MdItemVendor> selectByCondition(MdItemVendor mdItemVendor);

	/**
	 * 按所属主表ID查询（子表场景，按父ID一次性取出全部）
	 * @param itemId 所属物料ID
	 * @return 该物料下的记录
	 * */
	List<MdItemVendor> selectByItemId(@Param("itemId") Long itemId);

	MdItemVendor selectById(Long id);

	/**
	 * 按「业务唯一键」查是否已存在（新增查重、修改时排除自身都要用到）
	 * @return 命中的物料供应商，不存在返回 null
	 * */
	MdItemVendor selectByUniqueKey(@Param("itemId") Long itemId, @Param("targetId") Long targetId);

	/**
	 * 把某个物料下其它供货关系的主供标志清掉，保证「一个物料只有一个主供应商」。
	 * excludeId 用来排除自身：修改场景改的就是当前这条，不能把自己也清了。
	 */
	int clearPrimaryFlag(@Param("itemId") Long itemId, @Param("excludeId") Long excludeId);
}
