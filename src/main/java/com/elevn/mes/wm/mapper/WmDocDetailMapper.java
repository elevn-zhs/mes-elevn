package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmDocDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 出入库单据明细（库位落位）Mapper
 * 对应表：wm_doc_detail
 * 映射文件：resources/mapper/wm/WmDocDetailMapper.xml
 *
 */
@Mapper
public interface WmDocDetailMapper {

    /** 新增落位明细（主键回填到 detailId） */
    int insert(WmDocDetail wmDocDetail);

    /** 回填库存记录ID（入库时库存行是新建的，插完才知道 ID） */
    int updateMaterialStockId(WmDocDetail wmDocDetail);

    /** 按单据ID物理删除所有落位明细（过账回滚或重算时用；这些是过程数据，不留） */
    int deleteByDocId(@Param("docId") Long docId);

    /** 按单据ID查询落位明细 */
    List<WmDocDetail> selectByDocId(@Param("docId") Long docId);
}
