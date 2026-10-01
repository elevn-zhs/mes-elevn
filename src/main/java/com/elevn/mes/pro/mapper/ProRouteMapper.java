package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工艺路线的Mapper接口
 * 对应表：pro_route
 * 映射文件：resources/mapper/pro/ProRouteMapper.xml
 *
 */
@Mapper
public interface ProRouteMapper {

    /**
     * 批量删除工艺路线（逻辑删除）
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") Long[] ids);

    /**
     * 保存工艺路线
     * @param proRoute 工艺路线对象
     * @return 影响行数（主键回填到 routeId）
     */
    int insert(ProRoute proRoute);

    /**
     * 根据ID编辑工艺路线（只更新非 null 字段）
     * @param proRoute 工艺路线对象（routeId 必填）
     * @return 影响行数
     */
    int updateById(ProRoute proRoute);

    /**
     * 根据ID删除工艺路线（逻辑删除）
     * @param id 路线ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 多条件查询工艺路线列表（自动过滤已删除数据）
     * @param proRoute 封装查询条件
     * @return 路线列表
     */
    List<ProRoute> selectByCondition(ProRoute proRoute);

    /**
     * 根据路线编码查询（校验编码是否重复）
     * @param routeCode 路线编码
     * @return 路线对象，不存在返回 null
     */
    ProRoute selectByRouteCode(@Param("routeCode") String routeCode);

    /**
     * 根据ID查询单条
     * @param id 路线ID
     * @return 路线对象，不存在返回 null
     */
    ProRoute selectById(Long id);

    /**
     * 统计路线被产品制程引用的次数（pro_route_product.route_id）
     * @param routeId 路线ID
     * @return 引用次数
     */
    int selectReferenceCount(@Param("routeId") Long routeId);

    /**
     * 查询全部启用的路线（产品制程挂路线的下拉数据源）
     * @return 路线列表
     */
    List<ProRoute> selectAllEnabled();
}
