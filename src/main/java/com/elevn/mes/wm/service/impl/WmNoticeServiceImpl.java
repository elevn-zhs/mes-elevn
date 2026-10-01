package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.sys.service.SysCodingRuleService;
import com.elevn.mes.wm.entity.WmNotice;
import com.elevn.mes.wm.entity.WmNoticeLine;
import com.elevn.mes.wm.mapper.WmNoticeLineMapper;
import com.elevn.mes.wm.mapper.WmNoticeMapper;
import com.elevn.mes.wm.service.WmNoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓储通知单 Service 实现
 *
 * 【本模块的三条 L3 规则】
 *   1. 通知类型决定必填字段（和工单的"来源类型决定订单编号"是同一套思路）：
 *      到货通知 → 必须有供应商；发货通知 → 必须有客户；备料申请 → 必须有生产工单
 *   2. 通知单编号不可改、类型不可改（编号冗余在下游单据里，类型决定触发哪种检验）
 *   3. 已经报过检的通知单不能删 —— 检验单是 C 线的正式记录，
 *      来源没了，那条检验记录就成了孤儿
 *
 * 【通知单全程不碰库存】
 *   这个类里没有一处会改 wm_material_stock。真正动库存的是后面的出入库单据过账。
 *
 */
@Service
public class WmNoticeServiceImpl implements WmNoticeService {

    private static final Logger log = LoggerFactory.getLogger(WmNoticeServiceImpl.class);

    private static final String DEFAULT_OPERATOR = "admin";

    /** 通知类型 */
    private static final String TYPE_ARRIVAL = "ARRIVAL";
    private static final String TYPE_SALES = "SALES";
    private static final String TYPE_MATERIAL_REQUEST = "MATERIAL_REQUEST";

    /** 状态 */
    private static final String ST_PREPARE = "PREPARE";
    private static final String ST_CONFIRMED = "CONFIRMED";

    /** 检验类型（对应 wm_doc.qc_type / 检验单的检验类型） */
    private static final String QC_TYPE_IQC = "IQC";
    private static final String QC_TYPE_OQC = "OQC";

    @Autowired
    private WmNoticeMapper wmNoticeMapper;

    @Autowired
    private WmNoticeLineMapper wmNoticeLineMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Autowired
    private SysCodingRuleService sysCodingRuleService;

    // ====================================================================
    // 一、查询
    // ====================================================================

    @Override
    public PageInfo<WmNotice> page(int pageNum, int pageSize, WmNotice wmNotice) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(wmNoticeMapper.selectByCondition(wmNotice));
    }

    @Override
    public WmNotice queryById(Long noticeId) {
        WmNotice notice = wmNoticeMapper.selectById(noticeId);
        if (notice != null) {
            notice.setLineList(wmNoticeLineMapper.selectByNoticeId(noticeId));
        }
        return notice;
    }

    @Override
    public List<WmNoticeLine> queryLineList(Long noticeId) {
        return wmNoticeLineMapper.selectByNoticeId(noticeId);
    }

    // ====================================================================
    // 二、新增 / 修改 / 删除
    // ====================================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(WmNotice wmNotice) {
        checkNoticeType(wmNotice.getNoticeType());
        wmNotice.setStatus(ST_PREPARE);
        if (wmNotice.getNoticeDate() == null) {
            wmNotice.setNoticeDate(LocalDateTime.now());
        }
        checkBizRules(wmNotice);
        checkLineList(wmNotice);

        if (isBlank(wmNotice.getNoticeName())) {
            wmNotice.setNoticeName(buildDefaultNoticeName(wmNotice.getNoticeType()));
        }
        if (isBlank(wmNotice.getApplicantName())) {
            wmNotice.setApplicantName(DEFAULT_OPERATOR);
            wmNotice.setApplicantNick(DEFAULT_OPERATOR);
        }
        wmNotice.setCreateBy(DEFAULT_OPERATOR);
        wmNotice.setUpdateBy(DEFAULT_OPERATOR);
        wmNotice.setCreateTime(LocalDateTime.now());
        wmNotice.setUpdateTime(LocalDateTime.now());
        // 编号同样走 sys_coding_rules 的原子取号，三类通知各有一条规则
        wmNotice.setNoticeCode(sysCodingRuleService.autoCode("WM_NOTICE_" + wmNotice.getNoticeType()));

        // 兜底查重：编号是取号器发的，正常不会撞；万一规则被改过，这里能拦住并给一句人话
        if (wmNoticeMapper.selectByTypeAndCode(wmNotice.getNoticeType(), wmNotice.getNoticeCode()) != null) {
            throw new BusinessException("通知单编号[" + wmNotice.getNoticeCode()
                    + "]已存在，请检查编码规则的流水号是否被改动过");
        }

        int rows = wmNoticeMapper.insert(wmNotice);
        if (rows == 1) {
            insertLines(wmNotice);
        }
        return rows;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int updateById(WmNotice wmNotice) {
        WmNotice db = checkExists(wmNotice.getNoticeId());
        checkStatusCanModify(db, "修改");

        if (!isBlank(wmNotice.getNoticeType()) && !wmNotice.getNoticeType().equals(db.getNoticeType())) {
            throw new BusinessException("通知类型不允许修改（当前为 " + db.getNoticeType()
                    + "）。如需更换请新建通知单");
        }
        if (!isBlank(wmNotice.getNoticeCode()) && !wmNotice.getNoticeCode().equals(db.getNoticeCode())) {
            throw new BusinessException("通知单编号不允许修改（当前为 " + db.getNoticeCode() + "）");
        }
        wmNotice.setNoticeType(null);
        wmNotice.setNoticeCode(null);
        wmNotice.setStatus(null);

        // 校验要在"合并了库里已有值"的视图上做，否则只改备注会被误报"请选择供应商"
        WmNotice merged = mergeForCheck(db, wmNotice);
        checkBizRules(merged);

        wmNotice.setUpdateBy(DEFAULT_OPERATOR);
        wmNotice.setUpdateTime(LocalDateTime.now());
        int rows = wmNoticeMapper.updateById(wmNotice);

        // 行整批替换式修改，保证幂等
        if (wmNotice.getLineList() != null) {
            if (wmNotice.getLineList().isEmpty()) {
                throw new BusinessException("通知单至少要有一行明细，不能全部删掉");
            }
            wmNoticeLineMapper.deleteByNoticeId(wmNotice.getNoticeId());
            WmNotice forLine = new WmNotice();
            forLine.setNoticeId(db.getNoticeId());
            forLine.setNoticeType(db.getNoticeType());
            forLine.setLineList(wmNotice.getLineList());
            insertLines(forLine);
        }
        return rows;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteById(Long noticeId) {
        WmNotice db = checkExists(noticeId);
        checkStatusCanModify(db, "删除");
        checkNoQcGenerated(noticeId);
        wmNoticeLineMapper.deleteByNoticeId(noticeId);
        return wmNoticeMapper.deleteById(noticeId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteBatch(Long[] noticeIds) {
        if (noticeIds == null || noticeIds.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        for (Long noticeId : noticeIds) {
            WmNotice db = checkExists(noticeId);
            checkStatusCanModify(db, "删除");
            checkNoQcGenerated(noticeId);
        }
        for (Long noticeId : noticeIds) {
            wmNoticeLineMapper.deleteByNoticeId(noticeId);
        }
        return wmNoticeMapper.deleteBatch(noticeIds);
    }

    // ====================================================================
    // 三、触发检验（通知单唯一的状态推进动作）
    // ====================================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String triggerQc(Long noticeId) {
        WmNotice notice = checkExists(noticeId);
        if (!ST_PREPARE.equals(notice.getStatus())) {
            throw new BusinessException("通知单[" + notice.getNoticeCode()
                    + "]已经触发过检验了，请勿重复提交");
        }
        if (wmNoticeLineMapper.countByNoticeId(noticeId) == 0) {
            throw new BusinessException("通知单[" + notice.getNoticeCode() + "]没有明细行，无法报检");
        }

        String qcType = resolveQcType(notice.getNoticeType());
        if (qcType == null) {
            throw new BusinessException("备料申请单不触发检验。它触发的是备料/领料，请到生产领料单里处理");
        }

        // ------------------------------------------------------------------
        // TODO 待接入 C 线（质量管理）
        //
        // 正式流程应该是：调 C 线的检验单接口（POST /api/qc/inspection），
        // 由 C 线按通知单的物料/数量生成 IQC 或 OQC 检验单，
        // 返回 qcId / qcCode 之后，用 wmNoticeLineMapper.updateQcInfo 回填到每一行上，
        // 等到检验出结果，再把 quantity_qualified 与判定结论写回来。
        //
        // 现在 C 线还没提供这个接口，所以先把行上的 qc_flag 置 'Y' 表示"已报检"，
        // 并且打一条 WARN 日志 —— 这样联调时一眼就能看出"这里有活没干完"，
        // 而不是悄无声音地假装报检成功了。
        // ------------------------------------------------------------------
        // 这里刻意用字符串拼接而不是 log.warn("...{}...", a, b, c) 的占位符写法：
        // 占位符重载在不同 slf4j 版本里的签名不完全一样（老版本没有 varargs 形式），
        // 拼成一个字符串调 log.warn(String) 走到哪儿都不会有歧义。
        log.warn("【TODO 待接入 C 线】通知单 " + notice.getNoticeCode()
                + "（类型 " + notice.getNoticeType() + "）需要触发 " + qcType
                + " 检验，C 线检验单接口尚未就绪，本次只把明细行标记为待检，未生成检验单");

        List<WmNoticeLine> lines = wmNoticeLineMapper.selectByNoticeId(noticeId);
        for (WmNoticeLine line : lines) {
            WmNoticeLine update = new WmNoticeLine();
            update.setLineId(line.getLineId());
            update.setQcFlag("Y");
            update.setUpdateBy(DEFAULT_OPERATOR);
            update.setUpdateTime(LocalDateTime.now());
            wmNoticeLineMapper.updateById(update);
        }

        // 推进状态：updateStatus 的 where 带 status = 'PREPARE'，
        // 两个人同时点"提交检验"时，第二个返回 0 行，直接拒绝，不会给 C 线重复报检
        if (wmNoticeMapper.updateStatus(noticeId, ST_CONFIRMED) == 0) {
            throw new BusinessException("通知单[" + notice.getNoticeCode()
                    + "]已经被处理过了，请刷新后重试");
        }
        return qcType;
    }

    // ====================================================================
    // 四、校验（L3）
    // ====================================================================

    private void checkNoticeType(String noticeType) {
        if (isBlank(noticeType)) {
            throw new BusinessException("通知类型不能为空");
        }
        if (!TYPE_ARRIVAL.equals(noticeType) && !TYPE_SALES.equals(noticeType)
                && !TYPE_MATERIAL_REQUEST.equals(noticeType)) {
            throw new BusinessException("通知类型[" + noticeType + "]不是有效的仓储通知类型");
        }
    }

    /**
     * 通知类型决定必填字段 —— 和工单的"来源类型决定订单编号与客户"是同一套思路：
     * 到货一定有供应商，发货一定有客户，备料一定有工单。少一个都说不清这单是跟谁做的。
     */
    private void checkBizRules(WmNotice notice) {
        String type = notice.getNoticeType();
        if (TYPE_ARRIVAL.equals(type)) {
            if (notice.getPartnerId() == null) {
                throw new BusinessException("到货通知单必须选择供应商（货是谁送来的）");
            }
        } else if (TYPE_SALES.equals(type)) {
            if (notice.getPartnerId() == null) {
                throw new BusinessException("发货通知单必须选择客户（货是发给谁的）");
            }
        } else if (TYPE_MATERIAL_REQUEST.equals(type)) {
            if (notice.getWorkorderId() == null) {
                throw new BusinessException("备料申请单必须选择生产工单（备料是为哪张工单准备的）");
            }
        }
    }

    private void checkLineList(WmNotice notice) {
        List<WmNoticeLine> lines = notice.getLineList();
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException("请至少添加一行明细");
        }
        for (int i = 0; i < lines.size(); i++) {
            WmNoticeLine line = lines.get(i);
            if (line.getItemId() == null) {
                throw new BusinessException("第 " + (i + 1) + " 行：请选择物料");
            }
            if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("第 " + (i + 1) + " 行：数量必须大于 0");
            }
        }
    }

    /** 已经报过检的不允许删：检验单是 C 线的正式记录，来源没了它就成了孤儿 */
    private void checkNoQcGenerated(Long noticeId) {
        int qcCount = wmNoticeLineMapper.countQcFlaggedByNoticeId(noticeId);
        if (qcCount > 0) {
            throw new BusinessException("该通知单已有 " + qcCount
                    + " 行报过检，不能删除。检验记录要留痕，如需作废请联系质量部门先撤单");
        }
    }

    private WmNotice checkExists(Long noticeId) {
        if (noticeId == null) {
            throw new BusinessException("通知单ID不能为空");
        }
        WmNotice db = wmNoticeMapper.selectById(noticeId);
        if (db == null) {
            throw new BusinessException("通知单不存在或已被删除");
        }
        return db;
    }

    private void checkStatusCanModify(WmNotice db, String action) {
        if (!ST_PREPARE.equals(db.getStatus())) {
            throw new BusinessException("通知单[" + db.getNoticeCode() + "]已经触发过检验，不能再" + action
                    + "。检验已经报出去了，改来源会让质量部门对不上账");
        }
    }

    // ====================================================================
    // 五、内部小工具
    // ====================================================================

    private String resolveQcType(String noticeType) {
        if (TYPE_ARRIVAL.equals(noticeType)) {
            return QC_TYPE_IQC;
        }
        if (TYPE_SALES.equals(noticeType)) {
            return QC_TYPE_OQC;
        }
        return null;
    }

    private WmNotice mergeForCheck(WmNotice db, WmNotice param) {
        WmNotice merged = new WmNotice();
        merged.setNoticeId(db.getNoticeId());
        merged.setNoticeType(db.getNoticeType());
        merged.setPartnerId(param.getPartnerId() != null ? param.getPartnerId() : db.getPartnerId());
        merged.setWorkorderId(param.getWorkorderId() != null ? param.getWorkorderId() : db.getWorkorderId());
        return merged;
    }

    private void insertLines(WmNotice notice) {
        List<WmNoticeLine> lines = notice.getLineList();
        if (lines == null || lines.isEmpty()) {
            return;
        }
        int lineNo = 0;
        for (WmNoticeLine line : lines) {
            MdItem item = loadItem(line.getItemId());
            line.setItemCode(item.getItemCode());
            line.setItemName(item.getItemName());
            line.setSpecification(item.getSpecification());
            line.setUnitOfMeasure(item.getUnitOfMeasure());
            line.setUnitName(item.getUnitName());
            line.setNoticeId(notice.getNoticeId());
            line.setNoticeType(notice.getNoticeType());
            line.setLineNo(++lineNo);
            if (line.getQcFlag() == null) {
                line.setQcFlag("N");
            }
            line.setCreateBy(DEFAULT_OPERATOR);
            line.setUpdateBy(DEFAULT_OPERATOR);
            line.setCreateTime(LocalDateTime.now());
            line.setUpdateTime(LocalDateTime.now());
            wmNoticeLineMapper.insert(line);
        }
    }

    private MdItem loadItem(Long itemId) {
        if (itemId == null) {
            throw new BusinessException("物料不能为空");
        }
        MdItem item = mdItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("物料不存在或已被删除（物料ID：" + itemId + "）");
        }
        return item;
    }

    private String buildDefaultNoticeName(String noticeType) {
        return "WMTZ-" + noticeType + "-" + LocalDateTime.now().toLocalDate();
    }

    private boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }
}
