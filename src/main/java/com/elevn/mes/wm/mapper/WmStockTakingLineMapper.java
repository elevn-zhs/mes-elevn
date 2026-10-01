package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmStockTakingLine;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 盘点单明细行 Mapper
 *
 */
public interface WmStockTakingLineMapper {

    /** 按盘点单查明细行 */
    List<WmStockTakingLine> selectByTakingId(@Param("takingId") Long takingId);

    /** 按主键查询 */
    WmStockTakingLine selectById(@Param("lineId") Long lineId);

    /** 新增（generate 时批量落） */
    int insert(WmStockTakingLine line);

    /** 录入实盘数（taking_quantity + diff_quantity + 行状态一起更新） */
    int updateTaking(@Param("lineId") Long lineId,
                     @Param("takingQuantity") BigDecimal takingQuantity,
                     @Param("diffQuantity") BigDecimal diffQuantity);

    /** 数没录完的行（过账前置校验：必须 0） */
    int countUncounted(@Param("takingId") Long takingId);

    /** 差异行清单（diff != 0，过账与差异查询共用） */
    List<WmStockTakingLine> selectDiffLines(@Param("takingId") Long takingId);
}
