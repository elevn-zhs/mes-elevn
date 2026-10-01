package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmBatch;
import com.elevn.mes.wm.entity.WmMaterialStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 批次的Mapper接口
 * 对应表：wm_batch
 * 映射文件：resources/mapper/wm/WmBatchMapper.xml
 *
 */
@Mapper
public interface WmBatchMapper {

    /**
     * 新增批次
     * @param wmBatch 批次对象
     * @return 影响行数（主键回填到 batchId）
     */
    int insert(WmBatch wmBatch);

    /**
     * 根据ID修改（只更新非 null 字段；batch_code / item_id 不参与修改）
     * @param wmBatch 批次对象（batchId 必填）
     * @return 影响行数
     */
    int updateById(WmBatch wmBatch);

    /**
     * 回填批次编号（新增后按主键补上，批次号里带主键可保证唯一）
     * @param wmBatch 批次对象（batchId + batchCode 必填）
     * @return 影响行数
     */
    int updateBatchCode(WmBatch wmBatch);

    /**
     * 根据ID逻辑删除
     * @param id 批次ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量逻辑删除
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(@Param("ids") Long[] ids);

    /**
     * 根据ID查询单条
     * @param id 批次ID
     * @return 批次对象，不存在返回 null
     */
    WmBatch selectById(Long id);

    /**
     * 根据批次编号查询（唯一性校验用）
     * @param batchCode 批次编号
     * @return 批次对象，不存在返回 null
     */
    WmBatch selectByBatchCode(@Param("batchCode") String batchCode);

    /**
     * 多条件查询批次列表（自动过滤已删除数据）
     * @param wmBatch 封装查询条件（itemId / batchCode / lotNumber / 生产日期区间 / qualityStatus）
     * @return 批次列表
     */
    List<WmBatch> selectByCondition(WmBatch wmBatch);

    /**
     * 查询某批次的库存分布（在哪些仓库库位各有多少）
     * @param batchId 批次ID
     * @return 库存记录列表（只取还有在库数量的）
     */
    List<WmMaterialStock> selectStockByBatchId(@Param("batchId") Long batchId);

    /**
     * 统计批次上的库存记录数（删除前的 L3 校验）
     * @param batchId 批次ID
     * @return 库存记录数
     */
    int countStockByBatchId(@Param("batchId") Long batchId);
}
