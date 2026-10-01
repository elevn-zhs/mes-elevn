package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdVendor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商的Mapper接口
 * 对应表：md_vendor
 * 映射文件：resources/mapper/md/MdVendorMapper.xml
 *
 *
 * */
@Mapper
public interface MdVendorMapper  {

	/**
	 * 批量删除供应商（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存供应商信息
	 * @param mdVendor 供应商对象
	 * @return 影响行数（主键会回填到对象的 vendorId 上）
	 * */
	int insert(MdVendor mdVendor);

	/**
	 * 根据ID编辑供应商信息（只更新非 null 字段）
	 * @param mdVendor 供应商对象（vendorId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdVendor mdVendor);

	/**
	 * 根据ID删除供应商信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 供应商id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询供应商列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdVendor 封装查询条件
	 * @return 符合条件的供应商列表
	 * */
	List<MdVendor> selectByCondition(MdVendor mdVendor);

	/**
	 * 根据供应商编码查询（新增/修改时校验编码是否重复）
	 * @param vendorCode 编码
	 * @return 供应商对象，不存在返回 null
	 * */
	MdVendor selectByVendorCode(@Param("vendorCode") String vendorCode);

	MdVendor selectById(Long id);
}
