package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmWarehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓库的Mapper接口
 * 对应表：wm_warehouse
 * 映射文件：resources/mapper/wm/WmWarehouseMapper.xml
 *
 */
@Mapper
public interface WmWarehouseMapper {

    /**
     * 新增仓库
     * @param wmWarehouse 仓库对象
     * @return 影响行数（主键回填到 warehouseId）
     */
    int insert(WmWarehouse wmWarehouse);

    /**
     * 根据ID修改仓库（只更新非 null 字段；warehouse_code 不参与修改）
     * @param wmWarehouse 仓库对象（warehouseId 必填）
     * @return 影响行数
     */
    int updateById(WmWarehouse wmWarehouse);

    /**
     * 根据ID逻辑删除仓库
     * @param id 仓库ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量逻辑删除仓库
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") Long[] ids);

    /**
     * 根据ID查询单条
     * @param id 仓库ID
     * @return 仓库对象，不存在返回 null
     */
    WmWarehouse selectById(Long id);

    /**
     * 详情查询：基础字段 + 库区数/库位数/库存总量/库存物料种数
     * @param id 仓库ID
     * @return 仓库对象（含统计字段），不存在返回 null
     */
    WmWarehouse selectDetailById(Long id);

    /**
     * 根据编码查询（校验编码是否重复）
     * @param warehouseCode 仓库编码
     * @return 仓库对象，不存在返回 null
     */
    WmWarehouse selectByWarehouseCode(@Param("warehouseCode") String warehouseCode);

    /**
     * 多条件查询仓库列表（自动过滤已删除数据）
     * @param wmWarehouse 封装查询条件
     * @return 仓库列表
     */
    List<WmWarehouse> selectByCondition(WmWarehouse wmWarehouse);

    /**
     * 查询全部启用的仓库（库区库位、单据选仓库的下拉数据源）
     * @return 仓库列表
     */
    List<WmWarehouse> selectAllEnabled();

    /**
     * 统计仓库下的库区/库位数量（删除前的 L3 校验用）
     * @param warehouseId 仓库ID
     * @return 子节点数量
     */
    int countLocationByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 统计仓库下的库存记录数（只要有在库数量的就算，删除前的 L3 校验用）
     * @param warehouseId 仓库ID
     * @return 库存记录数
     */
    int countStockByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 按类型统计库区/库位数量（详情页显示用）
     * @param warehouseId 仓库ID
     * @param locationType AREA / LOCATION
     * @return 数量
     */
    int countLocationByType(@Param("warehouseId") Long warehouseId,
                            @Param("locationType") String locationType);
}
