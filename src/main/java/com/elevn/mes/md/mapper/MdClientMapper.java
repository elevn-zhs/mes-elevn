package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户的Mapper接口
 * 对应表：md_client
 * 映射文件：resources/mapper/md/MdClientMapper.xml
 *
 *
 * */
@Mapper
public interface MdClientMapper  {

	/**
	 * 批量删除客户（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存客户信息
	 * @param mdClient 客户对象
	 * @return 影响行数（主键会回填到对象的 clientId 上）
	 * */
	int insert(MdClient mdClient);

	/**
	 * 根据ID编辑客户信息（只更新非 null 字段）
	 * @param mdClient 客户对象（clientId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdClient mdClient);

	/**
	 * 根据ID删除客户信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 客户id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询客户列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdClient 封装查询条件
	 * @return 符合条件的客户列表
	 * */
	List<MdClient> selectByCondition(MdClient mdClient);

	/**
	 * 根据客户编码查询（新增/修改时校验编码是否重复）
	 * @param clientCode 编码
	 * @return 客户对象，不存在返回 null
	 * */
	MdClient selectByClientCode(@Param("clientCode") String clientCode);

	MdClient selectById(Long id);
}
