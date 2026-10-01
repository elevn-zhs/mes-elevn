package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmDocLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 出入库单据行 Mapper
 * 对应表：wm_doc_line
 * 映射文件：resources/mapper/wm/WmDocLineMapper.xml
 *
 */
@Mapper
public interface WmDocLineMapper {

    /** 新增行（主键回填到 lineId） */
    int insert(WmDocLine wmDocLine);

    /** 根据ID修改（只更新非 null 字段） */
    int updateById(WmDocLine wmDocLine);

    /** 回填物料快照（过账前统一补 itemCode/itemName/规格/单位） */
    int updateItemSnapshot(WmDocLine wmDocLine);

    /** 回填库存记录ID（出库定位到库存行、入库建完库存行之后都要回填） */
    int updateMaterialStockId(WmDocLine wmDocLine);

    /** 根据ID逻辑删除 */
    int deleteById(@Param("id") Long id);

    /** 按单据ID逻辑删除所有行（删单据时一起清掉） */
    int deleteByDocId(@Param("docId") Long docId);

    /** 根据ID查询单条 */
    WmDocLine selectById(Long id);

    /** 按单据ID查询行列表（按行号排序） */
    List<WmDocLine> selectByDocId(@Param("docId") Long docId);

    /** 统计单据下的行数（过账前必须至少有一行） */
    int countByDocId(@Param("docId") Long docId);

    /**
     * 取当前行号最大值（单行新增时好接着往下编号）
     * 没有行时返回 null，调用方按 0 处理
     */
    Integer selectMaxLineNo(@Param("docId") Long docId);
}
