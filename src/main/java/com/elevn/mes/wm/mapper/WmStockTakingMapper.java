package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmStockTaking;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 盘点单 Mapper
 *
 */
public interface WmStockTakingMapper {

    /** 多条件分页查询 */
    List<WmStockTaking> selectByCondition(WmStockTaking query);

    /** 按主键查询 */
    WmStockTaking selectById(@Param("takingId") Long takingId);

    /** 新增 */
    int insert(WmStockTaking taking);

    /** 修改基础信息（仅 PREPARE 可改） */
    int updateById(WmStockTaking taking);

    /** 状态推进（where 带旧状态防并发） */
    int updateStatus(@Param("takingId") Long takingId,
                     @Param("fromStatus") String fromStatus,
                     @Param("toStatus") String toStatus);

    /** 逻辑删除（仅 PREPARE 且无差异未过账） */
    int deleteById(@Param("takingId") Long takingId);
}
