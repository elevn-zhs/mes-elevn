package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmBarcodeConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 条码规则配置 Mapper
 *
 */
public interface WmBarcodeConfigMapper {

    /** 多条件分页查询 */
    List<WmBarcodeConfig> selectByCondition(WmBarcodeConfig query);

    /** 按主键查询 */
    WmBarcodeConfig selectById(@Param("configId") Long configId);

    /** 按类型查启用中的规则（生成条码时用） */
    WmBarcodeConfig selectEnabledByType(@Param("barcodeType") String barcodeType);

    /** 数某类型下启用中的规则数（保证"一类一规则"） */
    int countEnabledByType(@Param("barcodeType") String barcodeType,
                           @Param("excludeId") Long excludeId);

    /** 新增 */
    int insert(WmBarcodeConfig config);

    /** 修改 */
    int updateById(WmBarcodeConfig config);

    /** 逻辑删除 */
    int deleteById(@Param("configId") Long configId);
}
