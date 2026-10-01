package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 仓储通知单 Mapper
 * 对应表：wm_notice
 * 映射文件：resources/mapper/wm/WmNoticeMapper.xml
 *
 */
@Mapper
public interface WmNoticeMapper {

    /** 新增通知单（主键回填到 noticeId） */
    int insert(WmNotice wmNotice);

    /** 根据ID修改（只更新非 null 字段；notice_type / notice_code 不参与修改） */
    int updateById(WmNotice wmNotice);

    /** 回填通知单编号（新增后按编码规则补上） */
    int updateNoticeCode(WmNotice wmNotice);

    /**
     * 推进状态（只有一个动作：触发检验后 PREPARE → CONFIRMED）
     * where 里带 status = 'PREPARE' —— 重复点"提交检验"时返回 0 行，
     * Service 据此拒绝，不会给 C 线重复报检。
     */
    int updateStatus(@Param("noticeId") Long noticeId, @Param("status") String status);

    /** 根据ID逻辑删除 */
    int deleteById(@Param("id") Long id);

    /** 批量逻辑删除 */
    int deleteBatch(@Param("ids") Long[] ids);

    /** 根据ID查询单条 */
    WmNotice selectById(Long id);

    /** 通知单编号唯一性校验（同一类型下不允许重复） */
    WmNotice selectByTypeAndCode(@Param("noticeType") String noticeType,
                                 @Param("noticeCode") String noticeCode);

    /** 多条件查询通知单列表（必带 noticeType，否则 3 类会混在一起） */
    List<WmNotice> selectByCondition(WmNotice wmNotice);
}
