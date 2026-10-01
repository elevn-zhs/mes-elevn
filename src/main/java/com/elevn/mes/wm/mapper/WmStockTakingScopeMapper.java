package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmStockTakingScope;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 盘点范围 Mapper
 *
 */
public interface WmStockTakingScopeMapper {

    /** 按计划查范围 */
    List<WmStockTakingScope> selectByPlanId(@Param("planId") Long planId);

    /** 新增 */
    int insert(WmStockTakingScope scope);

    /** 按计划清空范围（整批替换式：编辑计划范围时先删后插，保证幂等） */
    int deleteByPlanId(@Param("planId") Long planId);
}
