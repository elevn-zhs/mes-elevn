package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProWorkorderBom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 生产工单BOM组成的Mapper接口
 * 对应表：pro_workorder_bom
 * 映射文件：resources/mapper/pro/ProWorkorderBomMapper.xml
 *
 */
@Mapper
public interface ProWorkorderBomMapper {

    /**
     * 批量插入工单BOM行（工单下达时按产品BOM一次性展开）
     * @param list BOM 行列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<ProWorkorderBom> list);

    /**
     * 根据工单ID查询BOM明细
     * @param workorderId 工单ID
     * @return BOM 行列表
     */
    List<ProWorkorderBom> selectByWorkorderId(@Param("workorderId") Long workorderId);

    /**
     * 根据工单ID物理清理 BOM 行
     *
     * 这里用物理删除而不是逻辑删除，原因：
     *   工单下达是"整批展开"的幂等操作，重新展开时如果不把旧行彻底删掉，
     *   逻辑删除的行会一直堆在表里，而且 del_flag='1' 的行还会占用
     *   (workorder_id, item_id) 这个业务上的唯一组合，导致重复展开时看起来"插不进去"。
     *   BOM 展开行是可重算的派生数据，没有独立业务价值，直接物理删更干净。
     *
     * @param workorderId 工单ID
     * @return 影响行数
     */
    int deleteByWorkorderId(@Param("workorderId") Long workorderId);

    /**
     * 统计工单BOM行数
     * @param workorderId 工单ID
     * @return 行数
     */
    int selectCountByWorkorderId(@Param("workorderId") Long workorderId);
}
