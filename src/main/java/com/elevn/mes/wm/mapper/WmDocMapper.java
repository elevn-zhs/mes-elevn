package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 出入库单据头 Mapper
 * 对应表：wm_doc
 * 映射文件：resources/mapper/wm/WmDocMapper.xml
 *
 */
@Mapper
public interface WmDocMapper {

    /** 新增单据（主键回填到 docId） */
    int insert(WmDoc wmDoc);

    /** 根据ID修改（只更新非 null 字段；doc_type / doc_code / io_flag 不参与修改） */
    int updateById(WmDoc wmDoc);

    /** 回填单据编号（新增后按编码规则补上） */
    int updateDocCode(WmDoc wmDoc);

    /**
     * 过账：把状态从 PREPARE 改为 CONFIRMED
     *
     * 【条件里必须带 status = 'PREPARE'】
     * 两个人同时点了过账，第一个改了状态，第二个的 update 影响 0 行 ——
     * Service 看到返回 0 就知道"这张单已经被处理过了"，直接拒绝，
     * 库存不会被扣两遍。这就是用数据库行锁做并发防护的常规做法。
     */
    int updateExecute(WmDoc wmDoc);

    /**
     * 取消单据：PREPARE → CANCELED
     *
     * 只有"还没动过库存"的单才能取消。已经过账的单子想反悔，
     * 只能开一张反方向的单子（红冲），不能靠改状态把账抹掉。
     * 因此 where 里同样带 status = 'PREPARE'。
     */
    int updateCancel(WmDoc wmDoc);

    /**
     * 调拨送达确认：CONFIRMED → FINISHED，同时把 confirm_flag 置 'Y'
     * 调拨是唯一有"两次动作"的单据：过账时货离开源仓，送达确认后整单才算完。
     */
    int updateFinish(WmDoc wmDoc);

    /** 根据ID逻辑删除 */
    int deleteById(@Param("id") Long id);

    /** 批量逻辑删除 */
    int deleteBatch(@Param("ids") Long[] ids);

    /** 根据ID查询单条 */
    WmDoc selectById(Long id);

    /** 多条件查询单据列表（必带 docType，否则 14 类单据会混在一起） */
    List<WmDoc> selectByCondition(WmDoc wmDoc);

    /** 统计某状态下的单据数（删除/修改前的校验用） */
    int countByDocIdAndStatus(@Param("docId") Long docId, @Param("status") String status);
}
