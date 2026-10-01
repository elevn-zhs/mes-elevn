package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProRouteProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工艺路线工序明细的Mapper接口
 * 对应表：pro_route_process
 * 映射文件：resources/mapper/pro/ProRouteProcessMapper.xml
 *
 */
@Mapper
public interface ProRouteProcessMapper {

    /**
     * 批量插入路线工序明细（整批替换的写入侧）
     * @param list 明细列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<ProRouteProcess> list);

    /**
     * 按路线ID删除全部明细（逻辑删除，整批替换的清理侧）
     * @param routeId 路线ID
     * @return 影响行数
     */
    int deleteByRouteId(@Param("routeId") Long routeId);

    /**
     * 按路线ID查询全部明细（按 order_num 升序）
     * @param routeId 路线ID
     * @return 明细列表
     */
    List<ProRouteProcess> selectByRouteId(@Param("routeId") Long routeId);

    /**
     * 统计某工序是否被任何路线明细引用（工序删除检查用）
     * @param processId 工序ID
     * @return 引用次数
     */
    int countByProcessId(@Param("processId") Long processId);
}
