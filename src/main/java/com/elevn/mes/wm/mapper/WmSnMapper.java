package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmSn;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SN Mapper
 *
 */
public interface WmSnMapper {

    /** 多条件分页查询（join 工单取编码） */
    List<WmSn> selectByCondition(WmSn query);

    /** 按主键查询 */
    WmSn selectById(@Param("snId") Long snId);

    /** SN 码是否已存在（一物一码，生成时防重） */
    int countByCode(@Param("snCode") String snCode);

    /** 新增 */
    int insert(WmSn sn);

    /** 改状态（IN_STOCK / SHIPPED / FROZEN） */
    int updateStatus(@Param("snId") Long snId, @Param("status") String status);

    /** 重新绑定批次/工单（仅 IN_STOCK 可改） */
    int updateBind(WmSn sn);

    /** 逻辑删除（仅 IN_STOCK） */
    int deleteById(@Param("snId") Long snId);
}
