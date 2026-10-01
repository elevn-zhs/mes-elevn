package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProCardProcess;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工序流转卡-工序过站记录 Mapper
 * 对应表：pro_card_process
 *
 * 推进过站只走两个累加方法，禁止在别处直接 update 数量：
 *   addOutputQuantity —— 本道报工：累计产出 + 不良 + 出站时间 + 操作工
 *   addInputQuantity  —— 下一道投料：首次进入写 input_time，累计投入
 *
 * 报工冲销时反向走两个减法方法，与推进严格对称：
 *   subtractOutputQuantity —— 本道产出减回，减到 0 清出站时间
 *   subtractInputQuantity  —— 下一道投入减回，减到 0 清进站时间
 *
 */
public interface ProCardProcessMapper {

    /** 批量铺行（排产建卡时一次写入全部工序） */
    int insertBatch(@Param("list") List<ProCardProcess> list);

    /** 按卡查全部过站行（按序号排序） */
    List<ProCardProcess> selectByCardId(@Param("cardId") Long cardId);

    /** 按卡 + 工序精确定位某一行 */
    ProCardProcess selectByCardAndProcess(@Param("cardId") Long cardId,
                                          @Param("processId") Long processId);

    /**
     * 本道报工推进：产出/不良累加 + 出站时间 + 操作工回填。
     * ifnull 累加保证并发安全，与任务/工单的数量回写同一风格。
     */
    int addOutputQuantity(@Param("recordId") Long recordId,
                          @Param("output") BigDecimal output,
                          @Param("unqualified") BigDecimal unqualified,
                          @Param("outputTime") LocalDateTime outputTime,
                          @Param("userId") Long userId,
                          @Param("userName") String userName,
                          @Param("nickName") String nickName,
                          @Param("updateBy") String updateBy,
                          @Param("updateTime") LocalDateTime updateTime);

    /**
     * 下一道投料：首次进入写 input_time（已有时不动，保留最早的进站时间），
     * 投入数量累加。
     */
    int addInputQuantity(@Param("recordId") Long recordId,
                         @Param("inputTime") LocalDateTime inputTime,
                         @Param("input") BigDecimal input,
                         @Param("updateBy") String updateBy,
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 冲销回退本道产出：产出/不良减回，减到 0 时把出站时间清空。
     *
     * 减到 0 才清出站时间，是因为分批报工下本道可能还有别的批次没冲销，
     * 那些批次的出站时间必须保留。用 greatest(...,0) 兜底防止减成负数。
     */
    int subtractOutputQuantity(@Param("recordId") Long recordId,
                               @Param("output") BigDecimal output,
                               @Param("unqualified") BigDecimal unqualified,
                               @Param("updateBy") String updateBy,
                               @Param("updateTime") LocalDateTime updateTime);

    /**
     * 冲销回退下一道投入：投入减回，减到 0 时把进站时间清空
     * （投入归零说明下一道其实还没开工，进站时间是本次报工带出来的，应当撤回）。
     */
    int subtractInputQuantity(@Param("recordId") Long recordId,
                              @Param("input") BigDecimal input,
                              @Param("updateBy") String updateBy,
                              @Param("updateTime") LocalDateTime updateTime);

    /** 按卡物理删除（重建过站行时用） */
    int deleteByCardId(@Param("cardId") Long cardId);
}
