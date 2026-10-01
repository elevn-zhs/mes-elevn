package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdWorkstationWorker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作站人力资源的Mapper接口
 * 对应表：md_workstation_worker
 * 映射文件：resources/mapper/md/MdWorkstationWorkerMapper.xml
 *
 *
 * */
@Mapper
public interface MdWorkstationWorkerMapper  {

	/**
	 * 批量删除工作站人力资源（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存工作站人力资源信息
	 * @param mdWorkstationWorker 工作站人力资源对象
	 * @return 影响行数（主键会回填到对象的 recordId 上）
	 * */
	int insert(MdWorkstationWorker mdWorkstationWorker);

	/**
	 * 根据ID编辑工作站人力资源信息（只更新非 null 字段）
	 * @param mdWorkstationWorker 工作站人力资源对象（recordId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdWorkstationWorker mdWorkstationWorker);

	/**
	 * 根据ID删除工作站人力资源信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 工作站人力资源id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询工作站人力资源列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdWorkstationWorker 封装查询条件
	 * @return 符合条件的工作站人力资源列表
	 * */
	List<MdWorkstationWorker> selectByCondition(MdWorkstationWorker mdWorkstationWorker);

	/**
	 * 按工作站人力资源所属主表ID查询（子表场景，按父ID一次性取出全部）
	 * @param workstationId 所属ID
	 * @return 该父ID下的工作站人力资源列表
	 * */
	List<MdWorkstationWorker> selectByWorkstationId(@Param("workstationId") Long workstationId);

	MdWorkstationWorker selectById(Long id);

	/**
	 * 查询同一工作站下是否已绑定过同一岗位（重复绑定校验）
	 * @param workstationId 工作站ID
	 * @param postId 岗位ID
	 * @param excludeRecordId 排除的记录ID（修改时传自身主键，新增传 null）
	 * @return 命中的记录数，大于 0 说明重复
	 * */
	int selectDuplicate(@Param("workstationId") Long workstationId,
	                    @Param("postId") Long postId,
	                    @Param("excludeRecordId") Long excludeRecordId);
}
