package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工序的Mapper接口
 * 对应表：pro_process
 * 映射文件：resources/mapper/pro/ProProcessMapper.xml
 *
 */
@Mapper
public interface ProProcessMapper {

    /**
     * 批量删除工序（逻辑删除：把 del_flag 置为 1）
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") Long[] ids);

    /**
     * 保存工序信息
     * @param proProcess 工序对象
     * @return 影响行数（主键回填到 processId）
     */
    int insert(ProProcess proProcess);

    /**
     * 根据ID编辑工序信息（只更新非 null 字段）
     * @param proProcess 工序对象（processId 必填）
     * @return 影响行数
     */
    int updateById(ProProcess proProcess);

    /**
     * 根据ID删除工序（逻辑删除）
     * @param id 工序ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 多条件查询工序列表（自动过滤已删除数据）
     * @param proProcess 封装查询条件
     * @return 工序列表
     */
    List<ProProcess> selectByCondition(ProProcess proProcess);

    /**
     * 根据工序编码查询（校验编码是否重复）
     * @param processCode 工序编码
     * @return 工序对象，不存在返回 null
     */
    ProProcess selectByProcessCode(@Param("processCode") String processCode);

    /**
     * 根据ID查询单条
     * @param id 工序ID
     * @return 工序对象，不存在返回 null
     */
    ProProcess selectById(Long id);

    /**
     * 统计工序被下游引用的次数（md_workstation.process_id + pro_route_process.process_id）
     * 删除前的 L3 业务校验用：被引用的工序不允许删除
     * @param processId 工序ID
     * @return 引用次数
     */
    int selectReferenceCount(@Param("processId") Long processId);
}
