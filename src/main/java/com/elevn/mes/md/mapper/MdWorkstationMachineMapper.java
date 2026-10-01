package com.elevn.mes.md.mapper;

import com.elevn.mes.md.entity.MdWorkstationMachine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作站设备资源的Mapper接口
 * 对应表：md_workstation_machine
 * 映射文件：resources/mapper/md/MdWorkstationMachineMapper.xml
 *
 *
 * */
@Mapper
public interface MdWorkstationMachineMapper  {

	/**
	 * 批量删除工作站设备资源（逻辑删除：把 del_flag 置为 1，记录仍留在库里）
	 * @param ids 主键数组
	 * @return 影响行数
	 * */
	int deleteBatch(@Param("ids") Long [] ids);

	/**
	 * 保存工作站设备资源信息
	 * @param mdWorkstationMachine 工作站设备资源对象
	 * @return 影响行数（主键会回填到对象的 recordId 上）
	 * */
	int insert(MdWorkstationMachine mdWorkstationMachine);

	/**
	 * 根据ID编辑工作站设备资源信息（只更新非 null 字段）
	 * @param mdWorkstationMachine 工作站设备资源对象（recordId 必填）
	 * @return 影响行数
	 * */
	int updateById(MdWorkstationMachine mdWorkstationMachine);

	/**
	 * 根据ID删除工作站设备资源信息（逻辑删除：更新 del_flag = '1'，数据不真删）
	 * @param id 工作站设备资源id
	 * @return 影响行数
	 * */
	int deleteById(@Param("id") Long id);

	/**
	 * 多条件查询工作站设备资源列表（null 或空字符串的字段不参与过滤，自动过滤已删除数据）
	 * @param mdWorkstationMachine 封装查询条件
	 * @return 符合条件的工作站设备资源列表
	 * */
	List<MdWorkstationMachine> selectByCondition(MdWorkstationMachine mdWorkstationMachine);

	/**
	 * 按工作站设备资源所属主表ID查询（子表场景，按父ID一次性取出全部）
	 * @param workstationId 所属ID
	 * @return 该父ID下的工作站设备资源列表
	 * */
	List<MdWorkstationMachine> selectByWorkstationId(@Param("workstationId") Long workstationId);

	MdWorkstationMachine selectById(Long id);

	/**
	 * 查询同一工作站下是否已绑定过同一设备（重复绑定校验）
	 * @param workstationId 工作站ID
	 * @param machineryId 设备ID
	 * @param excludeRecordId 排除的记录ID（修改时传自身主键，新增传 null）
	 * @return 命中的记录数，大于 0 说明重复
	 * */
	int selectDuplicate(@Param("workstationId") Long workstationId,
	                    @Param("machineryId") Long machineryId,
	                    @Param("excludeRecordId") Long excludeRecordId);
}
