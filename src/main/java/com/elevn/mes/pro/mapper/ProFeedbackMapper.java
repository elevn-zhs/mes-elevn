package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProFeedback;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产报工 Mapper
 *
 */
public interface ProFeedbackMapper {

    /** 新增报工（主键由数据库自增） */
    int insert(ProFeedback feedback);

    /** 按主键查询 */
    ProFeedback selectById(@Param("id") Long id);

    /** 按任务ID查询该任务的全部报工（按报工时间倒序） */
    List<ProFeedback> selectByTaskId(@Param("taskId") Long taskId);

    /** 条件查询报工列表（列表页，不分页） */
    List<ProFeedback> selectByCondition(ProFeedback feedback);

    /**
     * 统计某任务已报工数量合计（不含冲销）
     * 报工前置校验用：累计已报 + 本次报工 不许超过任务的排产数量。
     */
    BigDecimal sumFeedbackByTaskId(@Param("taskId") Long taskId);

    /** 按主键逻辑删除 */
    int deleteById(@Param("id") Long id);

    /**
     * 冲销：把状态置为 REVERSED 并留下冲销人/时间/原因（不删行）
     *
     * 只改状态与留痕字段，数量与消耗的回退由 Service 在同一个事务里做，
     * 这里不做任何扣减，保持"一张表只被一种 SQL 改"的清爽。
     *
     * @param id          报工ID
     * @param reason      冲销原因（必填）
     * @param reverseBy   冲销人
     * @param reverseTime 冲销时间
     * @return 影响行数
     */
    int reverseById(@Param("id") Long id,
                    @Param("reason") String reason,
                    @Param("reverseBy") String reverseBy,
                    @Param("reverseTime") LocalDateTime reverseTime);

    /**
     * 回填报工编号
     *
     * 编号里带自增主键（FB + 日期 + 6 位主键），而主键要 insert 之后才知道，
     * 所以先插入再回填一次。这样不用额外维护编码规则表也不会重复。
     */
    int updateFeedbackCode(@Param("id") Long id, @Param("feedbackCode") String feedbackCode);
}
