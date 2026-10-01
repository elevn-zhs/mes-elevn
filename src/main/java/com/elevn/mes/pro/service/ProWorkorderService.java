package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProWorkorder;
import com.elevn.mes.pro.entity.ProWorkorderBom;

import java.util.List;

/**
 * 生产工单 Service
 *
 */
public interface ProWorkorderService {

    /**
     * 分页 + 多条件查询工单
     */
    PageInfo<ProWorkorder> page(int pageNum, int pageSize, ProWorkorder proWorkorder);

    /**
     * 根据ID查询工单
     */
    ProWorkorder queryById(Long id);

    /**
     * 新增工单（状态固定为 PREPARE，不允许前端自己传状态）
     */
    int save(ProWorkorder proWorkorder);

    /**
     * 修改工单（只有 PREPARE 待下达状态可以改业务字段）
     */
    int updateById(ProWorkorder proWorkorder);

    /**
     * 删除工单（只有 PREPARE 状态可以删）
     */
    int deleteById(Long id);

    /**
     * 批量删除工单
     */
    int deleteBatch(Long[] ids);

    /**
     * 根据工单编码查询（编码查重用）
     */
    ProWorkorder queryByWorkorderCode(String workorderCode);

    /**
     * 下达工单：PREPARE → CONFIRMED，并按产品BOM展开工单BOM
     *
     * 这是工单的核心动作，做三件事：
     *   1. 状态校验（只有待下达能下达）
     *   2. 完整性校验（自产必须有产品制程，否则排产时拆不出工序）
     *   3. 按产品BOM展开 pro_workorder_bom，数量 = BOM单位用量 × 工单生产数量
     *
     * @param workorderId 工单ID
     * @return 展开出的 BOM 行数
     */
    int confirm(Long workorderId);

    /**
     * 完工：CONFIRMED → FINISHED，记录完成时间
     */
    int finish(Long workorderId);

    /**
     * 取消：PREPARE/CONFIRMED → CANCELED，记录取消日期
     */
    int cancel(Long workorderId);

    /**
     * 查询工单的 BOM 明细（详情页 Tab）
     */
    List<ProWorkorderBom> queryBomList(Long workorderId);

    /**
     * 重新展开工单BOM（手工重算入口，用于产品BOM调整后同步）
     */
    int rebuildBom(Long workorderId);
}
