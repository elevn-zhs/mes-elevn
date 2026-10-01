package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProCard;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工序流转卡 Mapper
 * 对应表：pro_card
 *
 * 注意：流转卡是排产的派生数据（一工单一卡），所以撤销排产 / 重新排产时
 * 用物理删除整卡重建，不留垃圾行。
 *
 */
public interface ProCardMapper {

    /** 新增流转卡（回填自增主键） */
    int insert(ProCard card);

    /** 按主键查 */
    ProCard selectById(@Param("cardId") Long cardId);

    /** 按工单查卡（一工单一卡） */
    ProCard selectByWorkorderId(@Param("workorderId") Long workorderId);

    /** 条件查询（分页） */
    List<ProCard> selectByCondition(@Param("cardCode") String cardCode,
                                    @Param("workorderCode") String workorderCode,
                                    @Param("itemName") String itemName,
                                    @Param("status") String status);

    /** 回填流转卡编号（insert 之后拿主键拼编号再 update） */
    int updateCardCode(@Param("cardId") Long cardId,
                       @Param("cardCode") String cardCode,
                       @Param("updateBy") String updateBy,
                       @Param("updateTime") LocalDateTime updateTime);

    /** 更新状态 */
    int updateStatus(@Param("cardId") Long cardId,
                     @Param("status") String status,
                     @Param("updateBy") String updateBy,
                     @Param("updateTime") LocalDateTime updateTime);

    /** 按工单物理删除（撤销排产 / 重新排产时用） */
    int deleteByWorkorderId(@Param("workorderId") Long workorderId);
}
