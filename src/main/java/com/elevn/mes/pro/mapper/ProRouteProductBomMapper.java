package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProRouteProductBom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 制程物料BOM的Mapper接口
 * 对应表：pro_route_product_bom
 * 映射文件：resources/mapper/pro/ProRouteProductBomMapper.xml
 *
 */
@Mapper
public interface ProRouteProductBomMapper {

    /**
     * 批量插入制程BOM（整批替换的写入侧）
     * @param list BOM行列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<ProRouteProductBom> list);

    /**
     * 按产品+路线删除全部BOM行（逻辑删除，整批替换的清理侧）
     * @param productId 产品ID
     * @param routeId 路线ID
     * @return 影响行数
     */
    int deleteByProductAndRoute(@Param("productId") Long productId, @Param("routeId") Long routeId);

    /**
     * 按产品+路线查询全部BOM行（按工序顺序、物料编码排序）
     * @param productId 产品ID
     * @param routeId 路线ID
     * @return BOM行列表
     */
    List<ProRouteProductBom> selectByProductAndRoute(@Param("productId") Long productId,
                                                     @Param("routeId") Long routeId);

    /**
     * 按 路线 + 工序 + 产品 查用料行（报工倒冲消耗时取单位用量）
     *
     * 三个参数正好是生产任务上现成的字段：
     *   task.routeId / task.processId / task.itemId（产品物料ID）
     *
     * @param routeId   工艺路线ID
     * @param processId 工序ID
     * @param productId 产品物料ID（md_item.item_id）
     * @return 该工序的用料行（含单位用量 quantity）
     */
    List<ProRouteProductBom> selectByRouteProcessProduct(@Param("routeId") Long routeId,
                                                         @Param("processId") Long processId,
                                                         @Param("productId") Long productId);
}
