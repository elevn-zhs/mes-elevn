package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProProcessContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工序内容的Mapper接口
 * 对应表：pro_process_content
 * 映射文件：resources/mapper/pro/ProProcessContentMapper.xml
 *
 */
@Mapper
public interface ProProcessContentMapper {

    /**
     * 批量删除工序内容（逻辑删除）
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") Long[] ids);

    /**
     * 保存工序内容
     * @param proProcessContent 工序内容对象
     * @return 影响行数（主键回填到 contentId）
     */
    int insert(ProProcessContent proProcessContent);

    /**
     * 根据ID编辑工序内容（只更新非 null 字段）
     * @param proProcessContent 工序内容对象（contentId 必填）
     * @return 影响行数
     */
    int updateById(ProProcessContent proProcessContent);

    /**
     * 根据ID删除工序内容（逻辑删除）
     * @param id 内容ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按工序ID查询全部内容（按 order_num 升序，子表数据量小不分页）
     * @param processId 工序ID
     * @return 内容列表
     */
    List<ProProcessContent> selectByProcessId(@Param("processId") Long processId);

    /**
     * 根据ID查询单条
     * @param id 内容ID
     * @return 工序内容对象，不存在返回 null
     */
    ProProcessContent selectById(Long id);

    /**
     * 按工序ID删除全部内容（逻辑删除，主表删除时联动清理）
     * @param processId 工序ID
     * @return 影响行数
     */
    int deleteByProcessId(@Param("processId") Long processId);
}
