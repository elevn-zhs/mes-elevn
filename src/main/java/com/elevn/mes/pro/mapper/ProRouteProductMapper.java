package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProRouteProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品制程的Mapper接口
 * 对应表：pro_route_product
 * 映射文件：resources/mapper/pro/ProRouteProductMapper.xml
 *
 */
@Mapper
public interface ProRouteProductMapper {

    /**
     * 保存产品制程
     * @param proRouteProduct 产品制程对象
     * @return 影响行数（主键回填到 recordId）
     */
    int insert(ProRouteProduct proRouteProduct);

    /**
     * 根据ID编辑产品制程（只更新非 null 字段）
     * @param proRouteProduct 产品制程对象（recordId 必填）
     * @return 影响行数
     */
    int updateById(ProRouteProduct proRouteProduct);

    /**
     * 根据ID删除产品制程（逻辑删除）
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询单条
     * @param id 记录ID
     * @return 产品制程对象，不存在返回 null
     */
    ProRouteProduct selectById(Long id);

    /**
     * 按产品ID查询全部制程（物料详情页 Tab 用）
     * @param itemId 产品ID
     * @return 制程列表
     */
    List<ProRouteProduct> selectByItemId(@Param("itemId") Long itemId);

    /**
     * 查重：同一产品是否已挂同一条路线
     * @param itemId 产品ID
     * @param routeId 路线ID
     * @param excludeRecordId 修改场景排除自身，可空
     * @return 命中条数
     */
    int countDuplicate(@Param("itemId") Long itemId,
                       @Param("routeId") Long routeId,
                       @Param("excludeRecordId") Long excludeRecordId);
}
