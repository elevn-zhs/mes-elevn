package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmStockTakingPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 盘点计划 Mapper
 *
 */
public interface WmStockTakingPlanMapper {

    /** 多条件分页查询 */
    List<WmStockTakingPlan> selectByCondition(WmStockTakingPlan query);

    /** 按主键查询 */
    WmStockTakingPlan selectById(@Param("planId") Long planId);

    /** 新增（含范围一起由 Service 落） */
    int insert(WmStockTakingPlan plan);

    /** 修改（编号不可改；仅 PREPARE 可编辑，where 卡状态） */
    int updateById(WmStockTakingPlan plan);

    /** 状态推进（where 带旧状态防并发） */
    int updateStatus(@Param("planId") Long planId,
                     @Param("fromStatus") String fromStatus,
                     @Param("toStatus") String toStatus);

    /** 逻辑删除 */
    int deleteById(@Param("planId") Long planId);

    /** 该计划已生成过几张盘点单（有盘点单的计划不允许改范围/删除） */
    int countTakingByPlanId(@Param("planId") Long planId);
}
