package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmLocation;
import com.elevn.mes.wm.entity.WmMaterialStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 库区库位的Mapper接口
 * 对应表：wm_location
 * 映射文件：resources/mapper/wm/WmLocationMapper.xml
 *
 * 【一张表两级，查询要注意带 location_type】
 *   不带类型条件的话，库区和库位会混在同一个列表里返回，
 *   页面上就会出现"库区下面又挂着库区"这种看不懂的数据。
 *
 */
@Mapper
public interface WmLocationMapper {

    /**
     * 新增库区/库位
     * @param wmLocation 对象
     * @return 影响行数（主键回填到 locationId）
     */
    int insert(WmLocation wmLocation);

    /**
     * 根据ID修改（只更新非 null 字段；location_type / parent_id 不参与修改）
     * @param wmLocation 对象（locationId 必填）
     * @return 影响行数
     */
    int updateById(WmLocation wmLocation);

    /**
     * 根据ID逻辑删除
     * @param id 库区/库位ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量逻辑删除
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") Long[] ids);

    /**
     * 根据ID查询单条
     * @param id 库区/库位ID
     * @return 对象，不存在返回 null
     */
    WmLocation selectById(Long id);

    /**
     * 按「类型 + 编码」查询（唯一键是这两个字段的组合，校验重复用）
     * @param locationType AREA / LOCATION
     * @param locationCode 编码
     * @return 对象，不存在返回 null
     */
    WmLocation selectByCodeAndType(@Param("locationType") String locationType,
                                   @Param("locationCode") String locationCode);

    /**
     * 多条件查询列表（自动过滤已删除数据）
     * @param wmLocation 封装查询条件（warehouseId / locationType / locationCode / locationName / enableFlag）
     * @return 列表
     */
    List<WmLocation> selectByCondition(WmLocation wmLocation);

    /**
     * 查询树形用的全部节点（AREA + LOCATION）
     * @param wmLocation 过滤条件（warehouseId 可选）
     * @return 平铺列表，由 Service 组装成树
     */
    List<WmLocation> selectAllForTree(WmLocation wmLocation);

    /**
     * 查询某库位当前存放的物料与批次（详情页用）
     * @param locationId 库位ID
     * @return 库存记录列表（只取还有在库数量的）
     */
    List<WmMaterialStock> selectStockByLocationId(@Param("locationId") Long locationId);

    /**
     * 统计子节点数量（删除前的 L3 校验：有下级就不让删）
     * @param locationId 库区ID
     * @return 子节点数量
     */
    int countChildren(@Param("locationId") Long locationId);

    /**
     * 统计该库位上的库存记录数（删除前的 L3 校验）
     * @param locationId 库位ID
     * @return 库存记录数
     */
    int countStockByLocationId(@Param("locationId") Long locationId);
}
