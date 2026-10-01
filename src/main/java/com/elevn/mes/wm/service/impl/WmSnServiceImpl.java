package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.pro.entity.ProSnProcess;
import com.elevn.mes.pro.mapper.ProSnProcessMapper;
import com.elevn.mes.sys.service.SysCodingRuleService;
import com.elevn.mes.wm.entity.WmSn;
import com.elevn.mes.wm.mapper.WmSnMapper;
import com.elevn.mes.wm.service.WmSnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * SN Service 实现
 *
 * 【批量生成的防坑点】
 *   1. SN 码取号后要查重（countByCode）—— 取号规则正常不会重，
 *      但手工改过规则表就可能撞号，一物一码的底线不能破
 *   2. 物料快照从 md_item 现查回填，前端只给 itemId + batchCode
 *   3. 整批一个事务：生成 50 个 SN，第 30 个失败就全部回滚，
 *      不留"半批 SN"—— 半批的账最难对
 *
 */
@Service
public class WmSnServiceImpl implements WmSnService {

    @Autowired
    private WmSnMapper snMapper;

    @Autowired
    private MdItemMapper itemMapper;

    @Autowired
    private ProSnProcessMapper snProcessMapper;

    @Autowired
    private SysCodingRuleService codingRuleService;

    private static final String ST_IN_STOCK = "IN_STOCK";

    @Override
    public PageInfo<WmSn> page(int pageNum, int pageSize, WmSn query) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(snMapper.selectByCondition(query));
    }

    @Override
    public WmSn getById(Long snId) {
        WmSn sn = snMapper.selectById(snId);
        if (sn == null) {
            throw new BusinessException("SN 不存在或已被删除");
        }
        return sn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WmSn> generate(String batchCode, Long itemId, Long workorderId,
                               Integer count, String operator) {
        // ---------- 1. 入参校验 ----------
        if (count == null || count <= 0) {
            throw new BusinessException("生成数量必须大于 0");
        }
        if (count > 500) {
            throw new BusinessException("单次最多生成 500 个 SN（量大请分批，防止单事务太重）");
        }
        if (batchCode == null || batchCode.trim().isEmpty()) {
            throw new BusinessException("SN 必须挂批次，批次编码不能为空");
        }
        MdItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("产品物料不存在，ID=" + itemId);
        }

        // ---------- 2. 逐个取号生成（整批一个事务） ----------
        List<WmSn> created = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            WmSn sn = new WmSn();
            sn.setSnCode(nextUniqueSnCode());
            sn.setItemId(item.getItemId());
            sn.setItemCode(item.getItemCode());
            sn.setItemName(item.getItemName());
            sn.setSpecification(item.getSpecification());
            sn.setUnitOfMeasure(item.getUnitOfMeasure());
            sn.setBatchCode(batchCode.trim());
            sn.setWorkorderId(workorderId);
            sn.setStatus(ST_IN_STOCK);
            sn.setCreateBy(operator);
            sn.setUpdateBy(operator);
            snMapper.insert(sn);
            created.add(sn);
        }
        return created;
    }

    /** 取号 + 查重兜底：正常一次就过，撞号最多重试 3 次（规则被手工改过的场景） */
    private String nextUniqueSnCode() {
        for (int retry = 0; retry < 3; retry++) {
            String code = codingRuleService.autoCode("WM_SN");
            if (snMapper.countByCode(code) == 0) {
                return code;
            }
        }
        throw new BusinessException("SN 取号连续撞号 3 次，请检查编码规则 WM_SN 的流水配置");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBind(WmSn sn) {
        WmSn db = snMapper.selectById(sn.getSnId());
        if (db == null) {
            throw new BusinessException("SN 不存在或已被删除");
        }
        if (!ST_IN_STOCK.equals(db.getStatus())) {
            throw new BusinessException("SN 已发货或冻结，不能再改绑定（追溯链不能断）");
        }
        if (sn.getBatchCode() == null || sn.getBatchCode().trim().isEmpty()) {
            throw new BusinessException("SN 必须挂批次");
        }
        int rows = snMapper.updateBind(sn);
        if (rows == 0) {
            throw new BusinessException("绑定失败：SN 可能刚被别人处理过，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long snId, String status) {
        if (snMapper.selectById(snId) == null) {
            throw new BusinessException("SN 不存在或已被删除");
        }
        int rows = snMapper.updateStatus(snId, status);
        if (rows == 0) {
            throw new BusinessException("状态更新失败，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long snId) {
        int rows = snMapper.deleteById(snId);
        if (rows == 0) {
            throw new BusinessException("只有「在库」的 SN 才能删除");
        }
    }

    @Override
    public List<ProSnProcess> trace(Long snId) {
        if (snMapper.selectById(snId) == null) {
            throw new BusinessException("SN 不存在或已被删除");
        }
        // A 线还没写 SN 过站数据时，这里返回空列表 —— 页面展示"暂无过站记录"
        return snProcessMapper.selectBySnId(snId);
    }
}
