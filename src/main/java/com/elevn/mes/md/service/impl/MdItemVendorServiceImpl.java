package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.entity.MdItemVendor;
import com.elevn.mes.md.entity.MdVendor;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.md.mapper.MdItemVendorMapper;
import com.elevn.mes.md.mapper.MdVendorMapper;
import com.elevn.mes.md.service.MdItemVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料供应商 Service 实现
 *
 * 【后端二次校验的几条规则】
 * 1. 唯一性：同一个物料不能重复挂同一家供应商（表上有 uk，这里提前给中文提示）；
 * 2. 主供唯一：一个物料只允许有一个主供应商，设为主供时会先把其它记录的主供标志清掉；
 * 3. 冗余字段（双方编码 / 名称）由后端从 md_item、md_vendor 重新带出，前端传什么都不信；
 * 4. 日期区间、价格、起订量做合理性校验 —— 库里能存不代表业务上说得通。
 *
 */
@Service
public class MdItemVendorServiceImpl implements MdItemVendorService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdItemVendorMapper mdItemVendorMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Autowired
    private MdVendorMapper mdVendorMapper;

    @Override
    public PageInfo<MdItemVendor> page(int pageNum, int pageSize, MdItemVendor mdItemVendor) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdItemVendor> list = mdItemVendorMapper.selectByCondition(mdItemVendor);
        return new PageInfo<>(list);
    }

    @Override
    public MdItemVendor queryById(Long id) {
        return mdItemVendorMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdItemVendorMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdItemVendorMapper.deleteBatch(ids);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int updateById(MdItemVendor mdItemVendor) {
        MdItemVendor old = mdItemVendorMapper.selectById(mdItemVendor.getItemVendorId());
        if (old == null) {
            throw new BusinessException("供货关系不存在或已被删除");
        }
        Long itemId = mdItemVendor.getItemId() != null ? mdItemVendor.getItemId() : old.getItemId();
        Long vendorId = mdItemVendor.getVendorId() != null ? mdItemVendor.getVendorId() : old.getVendorId();

        MdItem item = loadItem(itemId);
        MdVendor vendor = loadVendor(vendorId);

        MdItemVendor exist = mdItemVendorMapper.selectByUniqueKey(itemId, vendorId);
        if (exist != null && !exist.getItemVendorId().equals(mdItemVendor.getItemVendorId())) {
            throw new BusinessException("物料「" + item.getItemName() + "」已经挂过供应商「" + vendor.getVendorName() + "」，不能重复添加");
        }

        fillRedundant(mdItemVendor, item, vendor);
        checkNumber(mdItemVendor);
        checkDateRange(mdItemVendor);

        // 改为主供之前，先把该物料下其它记录的 primary_flag 清掉（排除自己）
        if ("Y".equals(mdItemVendor.getPrimaryFlag())) {
            mdItemVendorMapper.clearPrimaryFlag(itemId, mdItemVendor.getItemVendorId());
        }

        mdItemVendor.setUpdateBy(DEFAULT_OPERATOR);
        mdItemVendor.setUpdateTime(LocalDateTime.now());
        return mdItemVendorMapper.updateById(mdItemVendor);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdItemVendor mdItemVendor) {
        MdItem item = loadItem(mdItemVendor.getItemId());
        MdVendor vendor = loadVendor(mdItemVendor.getVendorId());

        MdItemVendor exist = mdItemVendorMapper.selectByUniqueKey(mdItemVendor.getItemId(), mdItemVendor.getVendorId());
        if (exist != null) {
            throw new BusinessException("物料「" + item.getItemName() + "」已经挂过供应商「" + vendor.getVendorName() + "」，不能重复添加");
        }

        fillRedundant(mdItemVendor, item, vendor);
        checkNumber(mdItemVendor);
        checkDateRange(mdItemVendor);

        // 没传的字段按业务默认值兜底
        if (isBlank(mdItemVendor.getCurrency())) {
            mdItemVendor.setCurrency("CNY");
        }
        if (isBlank(mdItemVendor.getPrimaryFlag())) {
            mdItemVendor.setPrimaryFlag("N");
        }
        if (mdItemVendor.getPriority() == null) {
            mdItemVendor.setPriority(1);
        }
        if (isBlank(mdItemVendor.getEnableFlag())) {
            mdItemVendor.setEnableFlag("Y");
        }
        if (mdItemVendor.getLeadTime() == null) {
            mdItemVendor.setLeadTime(0);
        }

        // 新增即为主供时，先把该物料下其它记录的主供标志清掉
        if ("Y".equals(mdItemVendor.getPrimaryFlag())) {
            mdItemVendorMapper.clearPrimaryFlag(mdItemVendor.getItemId(), null);
        }

        mdItemVendor.setCreateBy(DEFAULT_OPERATOR);
        mdItemVendor.setUpdateBy(DEFAULT_OPERATOR);
        mdItemVendor.setCreateTime(LocalDateTime.now());
        mdItemVendor.setUpdateTime(LocalDateTime.now());
        return mdItemVendorMapper.insert(mdItemVendor);
    }

    @Override
    public List<MdItemVendor> queryByItemId(Long itemId) {
        return mdItemVendorMapper.selectByItemId(itemId);
    }

    /* ------------------------------------------------------------------ */
    /*  私有：校验与冗余带出                                                */
    /* ------------------------------------------------------------------ */

    private MdItem loadItem(Long itemId) {
        if (itemId == null) {
            throw new BusinessException("物料不能为空");
        }
        MdItem mdItem = mdItemMapper.selectById(itemId);
        if (mdItem == null) {
            throw new BusinessException("物料不存在，请重新选择");
        }
        return mdItem;
    }

    private MdVendor loadVendor(Long vendorId) {
        if (vendorId == null) {
            throw new BusinessException("供应商不能为空");
        }
        MdVendor mdVendor = mdVendorMapper.selectById(vendorId);
        if (mdVendor == null) {
            throw new BusinessException("供应商不存在，请重新选择");
        }
        return mdVendor;
    }

    /**
     * 冗余字段一律由后端从主表重新带出，列表页直接展示，不能依赖前端传值
     */
    private void fillRedundant(MdItemVendor row, MdItem item, MdVendor vendor) {
        if (item != null) {
            row.setItemId(item.getItemId());
            row.setItemCode(item.getItemCode());
            row.setItemName(item.getItemName());
        }
        if (vendor != null) {
            row.setVendorId(vendor.getVendorId());
            row.setVendorCode(vendor.getVendorCode());
            row.setVendorName(vendor.getVendorName());
        }
    }

    /**
     * 价格、起订量、交货周期：库里能存不代表业务上说得通，负数要拦掉
     */
    private void checkNumber(MdItemVendor row) {
        if (row.getPurchasePrice() != null && row.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("采购单价不能为负数");
        }
        if (row.getMinOrderQty() != null && row.getMinOrderQty().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("最小起订量不能为负数");
        }
        if (row.getLeadTime() != null && row.getLeadTime() < 0) {
            throw new BusinessException("交货周期不能为负数");
        }
    }

    private void checkDateRange(MdItemVendor row) {
        if (row.getEffectiveDate() != null && row.getExpireDate() != null
                && row.getExpireDate().isBefore(row.getEffectiveDate())) {
            throw new BusinessException("失效日期不能早于生效日期");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
