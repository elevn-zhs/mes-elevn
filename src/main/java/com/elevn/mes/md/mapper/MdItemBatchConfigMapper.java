package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdItemBatchConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物料批次属性配置的Mapper接口
 * 对应表：md_item_batch_config
 * 映射文件：resources/mapper/md/MdItemBatchConfigMapper.xml
 *
 *
 * */
@Mapper
public interface MdItemBatchConfigMapper  {

	/**
	 * 批量删除物料批次属性配置（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存物料批次属性配置信息
	 * @param mdItemBatchConfig 物料批次属性配置对象
	 * @return 影响行数（主键会回填到对象的 configId 上）
	 * */
	int insert(MdItemBatchConfig mdItemBatchConfig);

	/**
	 * 根据ID编辑物料批次属性配置信息（只更新非 null 字段）
	 * @param mdItemBatchConfig 物料批次属性配置对象（configId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdItemBatchConfig mdItemBatchConfig);

	/**
	 * 根据ID删除物料批次属性配置信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 物料批次属性配置id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询物料批次属性配置列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdItemBatchConfig 封装查询条件
	 * @return 符合条件的物料批次属性配置列表
	 * */
	List<MdItemBatchConfig> selectByCondition(MdItemBatchConfig mdItemBatchConfig);

	/**
	 * 按物料批次属性配置所属主表ID查询（一对一场景，只取一条）
	 * @param itemId 所属ID
	 * @return 物料批次属性配置对象，不存在返回 null
	 * */
	MdItemBatchConfig selectByItemId(@Param("itemId") Long itemId);

	MdItemBatchConfig selectById(Long id);
}
