package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物料产品的Mapper接口
 * 对应表：md_item
 * 映射文件：resources/mapper/md/MdItemMapper.xml
 *
 *
 * */
@Mapper
public interface MdItemMapper  {

	/**
	 * 批量删除物料产品（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存物料产品信息
	 * @param mdItem 物料产品对象
	 * @return 影响行数（主键会回填到对象的 itemId 上）
	 * */
	int insert(MdItem mdItem);

	/**
	 * 根据ID编辑物料产品信息（只更新非 null 字段）
	 * @param mdItem 物料产品对象（itemId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdItem mdItem);

	/**
	 * 根据ID删除物料产品信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 物料产品id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询物料产品列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdItem 封装查询条件
	 * @return 符合条件的物料产品列表
	 * */
	List<MdItem> selectByCondition(MdItem mdItem);

	/**
	 * 根据物料产品编码查询（新增/修改时校验编码是否重复）
	 * @param itemCode 编码
	 * @return 物料产品对象，不存在返回 null
	 * */
	MdItem selectByItemCode(@Param("itemCode") String itemCode);

	MdItem selectById(Long id);

	/**
	 * 查询 BOM 选料时需要排除的物料ID集合：当前物料自身 + 它的所有上级（祖先）+ 它的所有下级（子孙）
	 *
	 * 【为什么要排除】
	 * 上级：把某个上级选成自己的子件，就会构成 X → 上级 → X 的闭环，BOM 逐级展开、MRP 需求计算会无限递归。
	 * 下级：下级已经通过别的路径挂在当前物料下面了，再挂一次会造成用量重复计算（同一物料被计两遍）。
	 *
	 * 【为什么单独查一次而不是写进分页 SQL】
	 * 这条 SQL 带 with recursive，PageHelper 做 count 查询时会把原 SQL 套进
	 * "select count(0) from ( 原SQL ) tmp_count"，CTE 出现在派生表里兼容性很差。
	 * 拆成两步：先算出排除ID集合（一次查询、数据量极小），再走普通分页 SQL，分页交给 PageHelper 更稳。
	 *
	 * @param excludeItemId 当前物料ID
	 * @return 需要排除的物料ID集合（一定包含 excludeItemId 自身）
	 * */
	List<Long> selectBomExcludeIds(@Param("excludeItemId") Long excludeItemId);

	/**
	 * BOM 选料专用分页查询：多条件 + 排除指定物料ID集合
	 * @param mdItem 封装查询条件，null 或空字符串的字段不参与过滤
	 * @param excludeIds 需要排除的物料ID集合（由 selectBomExcludeIds 查出来）
	 * @return 候选物料列表
	 * */
	List<MdItem> selectForBom(@Param("item") MdItem mdItem, @Param("excludeIds") List<Long> excludeIds);

    List<MdItem> selectByName(String itemName);

	List<MdItem> selectByTypeId(Long catId);
}
