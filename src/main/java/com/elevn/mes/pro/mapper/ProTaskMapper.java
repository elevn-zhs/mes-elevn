package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 生产任务的Mapper接口
 * 对应表：pro_task
 * 映射文件：resources/mapper/pro/ProTaskMapper.xml
 *
 */
@Mapper
public interface ProTaskMapper {

    /**
     * 批量插入生产任务（排产写入侧）
     * @param list 任务列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<ProTask> list);

    /**
     * 按工单ID物理删除全部任务（撤销排产 / 重新排产前清旧数据）
     *
     * 为什么是物理删除而不是逻辑删除：
     *   任务行是排产算出来的派生数据，工单 + 路线 + 工时就能重新算出来，
     *   逻辑删除只会让"同一工单同一工序"出现多行，甘特图和报工查询都要额外过滤。
     *   需要留痕的话应该做操作日志，而不是让业务表承担。
     *
     * @param workorderId 工单ID
     * @return 影响行数
     */
    int deleteByWorkorderId(@Param("workorderId") Long workorderId);

    /**
     * 按工单ID查询任务列表（按开始时间、任务编号排序）—— 甘特图与详情页用
     * @param workorderId 工单ID
     * @return 任务列表
     */
    List<ProTask> selectByWorkorderId(@Param("workorderId") Long workorderId);

    /**
     * 按主键查询单条
     * @param id 任务ID
     * @return 任务对象，不存在返回 null
     */
    ProTask selectById(Long id);

    /**
     * 统计某工单下"已经报过工"的任务数（撤销排产的前置校验）
     *
     * 只要有任何一条任务的已生产数量 > 0，就说明产线已经开工，
     * 这时撤销排产会把报工数据变成孤儿，必须拒绝。
     *
     * @param workorderId 工单ID
     * @return 已报工的任务数
     */
    int countProducedByWorkorderId(@Param("workorderId") Long workorderId);

    /**
     * 条件查询任务列表（列表页 / 甘特图，不带分页）
     * @param proTask 查询条件（工单、工序、状态、时间区间）
     * @return 任务列表
     */
    List<ProTask> selectByCondition(ProTask proTask);

    /**
     * 按主键修改任务（只更新非 null 字段）
     * @param proTask 任务对象（taskId 必填）
     * @return 影响行数
     */
    int updateById(ProTask proTask);

    /**
     * 按主键逻辑删除任务（单条取消用）
     * @param id 任务ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 累计已排产数量：某产品已排了多少（转产能负荷看板用）
     * @param itemId 产品ID
     * @return 已排产数量合计
     */
    BigDecimal sumScheduledByItemId(@Param("itemId") Long itemId);

    // ============================================================
    // 报工回写相关
    // ============================================================

    /**
     * 报工后累计任务的已生产/合格/不良数量，并按需推进状态。
     *
     * 为什么用 `ifnull(...,0) + #{qty}` 而不是先查再写：
     *   车间可能多人在同一工作站同时报工，先查后写会互相覆盖。
     *   直接让数据库做加法，语义是"在现有值上再增这么多"，天然并发安全。
     *
     * 为什么状态也一起在这里改：
     *   任务的"生产中/已完工"完全由报工数量决定，分散在两处写容易出现
     *   "数量报满了但状态还是待生产"的不一致。
     *
     * @param taskId             任务ID
     * @param produced           本次新增的报工数量（计入已生产）
     * @param qualified          本次新增的合格品数量
     * @param unqualified        本次新增的不良品数量
     * @param status             目标状态（NORMAL/WORKING/FINISHED），传 null 表示不改状态
     * @param finishDate         完工时间，仅在 status=FINISHED 时有意义
     * @param updateBy           更新人
     * @param updateTime         更新时间
     * @return 影响行数
     */
    int addProducedQuantity(@Param("taskId") Long taskId,
                           @Param("produced") BigDecimal produced,
                           @Param("qualified") BigDecimal qualified,
                           @Param("unqualified") BigDecimal unqualified,
                           @Param("status") String status,
                           @Param("finishDate") java.time.LocalDateTime finishDate,
                           @Param("updateBy") String updateBy,
                           @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 统计某工单下"还没完工"的自产任务数（工单是否该自动完工的判据）
     *
     * 排除 CANCELED：已取消的任务不该阻塞工单完工，
     * 否则一条任务被取消，工单就永远完不了。
     *
     * @param workorderId 工单ID
     * @return 未完工的任务数，0 表示全部工序都完工了
     */
    int countUnfinishedByWorkorderId(@Param("workorderId") Long workorderId);
}
