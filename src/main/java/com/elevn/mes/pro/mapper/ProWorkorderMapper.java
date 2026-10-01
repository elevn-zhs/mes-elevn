package com.elevn.mes.pro.mapper;

import com.elevn.mes.pro.entity.ProWorkorder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产工单的Mapper接口
 * 对应表：pro_workorder
 * 映射文件：resources/mapper/pro/ProWorkorderMapper.xml
 *
 */
@Mapper
public interface ProWorkorderMapper {

    /**
     * 批量删除生产工单（逻辑删除）
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") Long[] ids);

    /**
     * 保存生产工单
     * @param proWorkorder 工单对象
     * @return 影响行数（主键回填到 workorderId）
     */
    int insert(ProWorkorder proWorkorder);

    /**
     * 根据ID编辑生产工单（只更新非 null 字段）
     * @param proWorkorder 工单对象（workorderId 必填）
     * @return 影响行数
     */
    int updateById(ProWorkorder proWorkorder);

    /**
     * 根据ID删除生产工单（逻辑删除）
     * @param id 工单ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 多条件查询生产工单列表（自动过滤已删除数据）
     * @param proWorkorder 封装查询条件
     * @return 工单列表
     */
    List<ProWorkorder> selectByCondition(ProWorkorder proWorkorder);

    /**
     * 根据工单编码查询（校验编码是否重复）
     * @param workorderCode 工单编码
     * @return 工单对象，不存在返回 null
     */
    ProWorkorder selectByWorkorderCode(@Param("workorderCode") String workorderCode);

    /**
     * 根据ID查询单条
     * @param id 工单ID
     * @return 工单对象，不存在返回 null
     */
    ProWorkorder selectById(Long id);

    /**
     * 状态流转：把工单状态改成目标状态，同时写入对应的时间字段
     *
     * 一个方法覆盖下达/完工/取消三种流转，靠传参区分：
     *   下达 CONFIRMED —— 只改 status
     *   完工 FINISHED  —— 改 status + finish_date
     *   取消 CANCELED  —— 改 status + cancel_date
     * 这样不用为每个流转写一句几乎一样的 update。
     *
     * @param workorderId 工单ID
     * @param status      目标状态
     * @param finishDate  完成时间（非完工流转传 null）
     * @param cancelDate  取消时间（非取消流转传 null）
     * @param updateBy    操作人
     * @return 影响行数
     */
    int updateStatus(@Param("workorderId") Long workorderId,
                     @Param("status") String status,
                     @Param("finishDate") LocalDateTime finishDate,
                     @Param("cancelDate") LocalDateTime cancelDate,
                     @Param("updateBy") String updateBy);

    /**
     * 累加已排产数量（排产时调用）
     * @param workorderId 工单ID
     * @param quantity    本次排产数量
     * @param updateBy    操作人
     * @return 影响行数
     */
    int addScheduledQuantity(@Param("workorderId") Long workorderId,
                             @Param("quantity") BigDecimal quantity,
                             @Param("updateBy") String updateBy);

    /**
     * 已排产数量归零（撤销排产时调用）
     *
     * 用置零而不是 addScheduledQuantity(负数)：
     * 撤销后该工单就没有任何任务了，直接归零比做减法更不容易算错，
     * 也顺带修正历史上可能累积的误差。
     *
     * @param workorderId 工单ID
     * @param updateBy    操作人
     * @return 影响行数
     */
    int resetScheduledQuantity(@Param("workorderId") Long workorderId,
                               @Param("updateBy") String updateBy);

    /**
     * 统计工单下的 BOM 行数（下达前校验用）
     * @param workorderId 工单ID
     * @return BOM 行数
     */
    int selectBomCount(@Param("workorderId") Long workorderId);

    /**
     * 累加已生产数量（报工使任务完工后调用）
     *
     * 只在"该工单全部工序任务都完工"时调用一次，
     * 累加值就是本次完工任务所代表的产量（即任务的排产数量）。
     *
     * 与 addScheduledQuantity 同理，让数据库做加法而非先查后写。
     *
     * @param workorderId 工单ID
     * @param quantity    本次新增的已生产数量
     * @param updateBy    操作人
     * @return 影响行数
     */
    int addProducedQuantity(@Param("workorderId") Long workorderId,
                            @Param("quantity") BigDecimal quantity,
                            @Param("updateBy") String updateBy);
}
