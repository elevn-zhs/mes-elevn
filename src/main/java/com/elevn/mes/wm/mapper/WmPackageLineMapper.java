package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmPackageLine;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 装箱单明细行 Mapper
 *
 */
public interface WmPackageLineMapper {

    /** 按箱查明细行 */
    List<WmPackageLine> selectByPackageId(@Param("packageId") Long packageId);

    /** 按主键查单行 */
    WmPackageLine selectById(@Param("lineId") Long lineId);

    /** 新增 */
    int insert(WmPackageLine line);

    /** 修改装箱数量/备注（快照列不允许改 —— 装箱那一刻的事实不能事后涂改） */
    int updateById(WmPackageLine line);

    /** 按主键删除（物理删除：行还没定稿，删了重加就行） */
    int deleteById(@Param("lineId") Long lineId);

    /** 按箱清空明细（整箱删除时调用） */
    int deleteByPackageId(@Param("packageId") Long packageId);

    /** 数某个箱有没有明细行 */
    int countByPackageId(@Param("packageId") Long packageId);

    /** 汇总某库存行已被装箱的数量（校验装箱总量别超过库存现存） */
    BigDecimal sumQuantityByStockId(@Param("materialStockId") Long materialStockId);
}
