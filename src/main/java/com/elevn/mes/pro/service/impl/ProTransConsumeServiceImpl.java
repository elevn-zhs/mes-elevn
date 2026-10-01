package com.elevn.mes.pro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.pro.entity.ProCard;
import com.elevn.mes.pro.entity.ProMaterialRequire;
import com.elevn.mes.pro.entity.ProRouteProductBom;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProTransConsume;
import com.elevn.mes.pro.mapper.ProCardMapper;
import com.elevn.mes.pro.mapper.ProRouteProductBomMapper;
import com.elevn.mes.pro.mapper.ProTaskMapper;
import com.elevn.mes.pro.mapper.ProTransConsumeMapper;
import com.elevn.mes.pro.service.ProTransConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 物料消耗 Service 实现
 *
 * ============================================================
 * 一、为什么用「倒冲」而不是「手工领料核销」
 * ============================================================
 * 车间现场不会为每颗螺丝单独开一张领料单。真实做法是：
 *   按产出的成品数量 × 制程BOM 单位用量，反推回去记消耗 —— 这叫倒冲（backflush）。
 *
 * 好处是：只要有报工，用料账就是全的，不依赖仓管员补录；
 * 代价是：它记的是"应该消耗了多少"。真正"领了多少"要等 B 线的领料单，
 *        两者对上，差额就是车间损耗 —— 那是 B 线接入后的事。
 *
 * ============================================================
 * 二、不良品为什么也要算消耗
 * ============================================================
 * 料已经投进去、已经加工过了，做成不良品它照样把料吃掉了。
 * 所以倒冲基数取的是「合格 + 不良」，不含待检 —— 待检品还没判定，
 * 但它同样已经加工完，料也已经消耗。于是本模块刻意让消耗跟着**报工总量**走，
 * 而流转卡只让「合格 + 不良」流入下一道 —— 两者口径不同是有意的：
 *   流转卡管"活能不能往下走"，消耗管"料有没有被吃掉"。
 *
 * ============================================================
 * 三、单位用量的来源
 * ============================================================
 *   pro_route_product_bom.quantity，按 (route_id, process_id, product_id) 命中。
 *   这三个值正好是生产任务上现成的字段，不用额外关联工单。
 *
 * ============================================================
 * 四、数量精度
 * ============================================================
 *   表中数量列是 decimal(18,6)，乘法结果统一 setScale(6, HALF_UP)，
 *   避免出现 18.000000000000001 这种浮点尾巴污染台账。
 *
 */
@Service
public class ProTransConsumeServiceImpl implements ProTransConsumeService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 来源单据类型：倒冲（本模块自动生成，没有前置单据） */
    private static final String SOURCE_TYPE_BACKFLUSH = "BACKFLUSH";

    /** 数量精度：与建表 decimal(18,6) 保持一致 */
    private static final int SCALE = 6;

    @Autowired
    private ProTransConsumeMapper proTransConsumeMapper;

    @Autowired
    private ProRouteProductBomMapper proRouteProductBomMapper;

    @Autowired
    private ProTaskMapper proTaskMapper;

    @Autowired
    private ProCardMapper proCardMapper;

    // ============================================================
    // 查询
    // ============================================================

    @Override
    public PageInfo<ProTransConsume> page(int pageNum, int pageSize, ProTransConsume condition) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProTransConsume> list = proTransConsumeMapper.selectByCondition(condition);
        return new PageInfo<>(list);
    }

    @Override
    public List<ProTransConsume> queryList(ProTransConsume condition) {
        return proTransConsumeMapper.selectByCondition(condition);
    }

    @Override
    public ProTransConsume queryById(Long recordId) {
        if (recordId == null) {
            throw new BusinessException("请指定要查询的消耗记录");
        }
        return proTransConsumeMapper.selectById(recordId);
    }

    @Override
    public List<ProTransConsume> queryByTaskId(Long taskId) {
        if (taskId == null) {
            return Collections.emptyList();
        }
        return proTransConsumeMapper.selectByTaskId(taskId);
    }

    @Override
    public List<ProTransConsume> queryByWorkorderId(Long workorderId) {
        if (workorderId == null) {
            return Collections.emptyList();
        }
        return proTransConsumeMapper.selectByWorkorderId(workorderId);
    }

    @Override
    public List<ProTransConsume> summaryByWorkorder(Long workorderId) {
        if (workorderId == null) {
            return Collections.emptyList();
        }
        return proTransConsumeMapper.sumByWorkorderItem(workorderId);
    }

    @Override
    public List<ProMaterialRequire> queryMaterialRequire(Long taskId) {
        if (taskId == null) {
            throw new BusinessException("请选择要查看用料的工序任务");
        }
        ProTask task = proTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("生产任务不存在或已被删除");
        }
        // 老数据可能没有排产快照，取不到用料维度就返回空表，前端显示"该工序不消耗物料"
        if (task.getRouteId() == null || task.getProcessId() == null || task.getItemId() == null) {
            return Collections.emptyList();
        }
        return proTransConsumeMapper.selectMaterialRequire(task.getRouteId(), task.getProcessId(),
                task.getItemId(), task.getTaskId(), nvl(task.getQuantity()));
    }

    @Override
    public List<ProMaterialRequire> compareByWorkorder(Long workorderId) {
        if (workorderId == null) {
            throw new BusinessException("请选择要查看用料的工单");
        }
        return proTransConsumeMapper.selectWorkorderMaterialCompare(workorderId);
    }

    // ============================================================
    // 报工倒冲：生成物料消耗（挂在报工事务里）
    // ============================================================
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int generateOnFeedback(ProTask task, BigDecimal outputQty,
                                  LocalDateTime consumeTime, String userName, Long feedbackId) {
        // ---- 前置守卫：缺信息就静默跳过，不能因为用料没配就卡住报工 ----
        if (task == null) {
            return 0;
        }
        if (task.getRouteId() == null || task.getProcessId() == null || task.getItemId() == null) {
            return 0;
        }
        BigDecimal output = nvl(outputQty);
        if (output.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        // ---- 1. 取这道工序在制程BOM 上的用料清单 ----
        List<ProRouteProductBom> boms = proRouteProductBomMapper.selectByRouteProcessProduct(
                task.getRouteId(), task.getProcessId(), task.getItemId());
        if (boms == null || boms.isEmpty()) {
            // 测试、老化这类不投料的工序本来就没配用料，正常
            return 0;
        }

        // ---- 2. 拿流转卡，把消耗挂到卡（批次）上，便于按批次追溯 ----
        ProCard card = proCardMapper.selectByWorkorderId(task.getWorkorderId());

        LocalDateTime now = LocalDateTime.now();
        String operator = userName == null || userName.isEmpty() ? DEFAULT_OPERATOR : userName;
        // 没有卡就退回用报工时间，别让消耗时间空着
        LocalDateTime consumeAt = consumeTime == null ? now : consumeTime;

        // ---- 3. 逐条倒冲：消耗 = 本次产出 × 单位用量 ----
        List<ProTransConsume> rows = new ArrayList<>(boms.size());
        for (ProRouteProductBom bom : boms) {
            BigDecimal unitQty = nvl(bom.getQuantity());
            BigDecimal consumed = unitQty.multiply(output).setScale(SCALE, RoundingMode.HALF_UP);
            // 用量小到四舍五入后为 0 的料（如 0.1 件/套 报 1 件），不产生 0 消耗的废记录
            if (consumed.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            ProTransConsume entity = new ProTransConsume();
            entity.setTransOrderId(card == null ? null : card.getCardId());
            entity.setTransOrderCode(card == null ? null : card.getCardCode());
            entity.setTaskId(task.getTaskId());
            // 挂回来源报工：冲销时按这个 ID 精确回退，不必按时间猜
            entity.setFeedbackId(feedbackId);
            // 消耗发生在哪个工位，以排产时指定的工作站为准
            entity.setWorkstationId(task.getWorkstationId());
            entity.setProcessId(task.getProcessId());
            entity.setWorkorderId(task.getWorkorderId());
            // 批次号：优先取流转卡上的批次，没有卡就退回工单号
            entity.setBatchCode(card == null ? task.getWorkorderCode() : card.getBatchCode());
            // 倒冲没有前置单据，来源类型标记出来，方便事后区分"手工领料核销"
            entity.setSourceDocType(SOURCE_TYPE_BACKFLUSH);

            // 物料快照直接取制程BOM 行上的冗余字段，不再回查 md_item
            entity.setItemId(bom.getItemId());
            entity.setItemCode(bom.getItemCode());
            entity.setItemName(bom.getItemName());
            entity.setSpecification(bom.getSpecification());
            entity.setUnitOfMeasure(bom.getUnitOfMeasure());

            entity.setQuantityConsumed(consumed);
            entity.setConsumeDate(consumeAt);
            entity.setRemark("报工倒冲生成");
            entity.setCreateBy(operator);
            entity.setCreateTime(now);
            entity.setUpdateBy(operator);
            entity.setUpdateTime(now);
            rows.add(entity);
        }

        for (ProTransConsume row : rows) {
            proTransConsumeMapper.insert(row);
        }
        return rows.size();
    }

    // ============================================================
    // 清理
    // ============================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int removeByTaskId(Long taskId) {
        if (taskId == null) {
            return 0;
        }
        return proTransConsumeMapper.deleteByTaskId(taskId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int removeByFeedbackId(Long feedbackId) {
        if (feedbackId == null) {
            return 0;
        }
        return proTransConsumeMapper.deleteByFeedbackId(feedbackId);
    }

    @Override
    public int deleteById(Long recordId) {
        if (recordId == null) {
            throw new BusinessException("请指定要删除的消耗记录");
        }
        return proTransConsumeMapper.deleteById(recordId, DEFAULT_OPERATOR);
    }

    // ============================================================
    // 小工具
    // ============================================================

    /** null 视作 0，避免到处写判空 */
    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
