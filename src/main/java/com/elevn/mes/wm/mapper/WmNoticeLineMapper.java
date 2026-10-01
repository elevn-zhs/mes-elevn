package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmNoticeLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓储通知单行 Mapper
 * 对应表：wm_notice_line
 * 映射文件：resources/mapper/wm/WmNoticeLineMapper.xml
 *
 */
@Mapper
public interface WmNoticeLineMapper {

    /** 新增行（主键回填到 lineId） */
    int insert(WmNoticeLine wmNoticeLine);

    /** 根据ID修改（只更新非 null 字段；notice_id / notice_type 不参与修改） */
    int updateById(WmNoticeLine wmNoticeLine);

    /** 回填检验单信息（C 线的检验单回来之后把 qcId / qcCode / 合格数量写回来） */
    int updateQcInfo(WmNoticeLine wmNoticeLine);

    /** 根据ID逻辑删除 */
    int deleteById(@Param("id") Long id);

    /** 按通知单ID逻辑删除所有行（改单时整批替换） */
    int deleteByNoticeId(@Param("noticeId") Long noticeId);

    /** 根据ID查询单条 */
    WmNoticeLine selectById(Long id);

    /** 按通知单ID查询行列表（按行号排序） */
    List<WmNoticeLine> selectByNoticeId(@Param("noticeId") Long noticeId);

    /** 统计通知单下的行数（触发检验前必须至少有一行） */
    int countByNoticeId(@Param("noticeId") Long noticeId);

    /**
     * 统计已经报检（qc_flag = 'Y'）的行数
     * 用于"已经报过检的通知单不允许删"这条 L3 校验
     */
    int countQcFlaggedByNoticeId(@Param("noticeId") Long noticeId);
}
