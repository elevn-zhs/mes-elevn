package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.entity.MdItemSubstitute;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.md.mapper.MdItemSubstituteMapper;
import com.elevn.mes.md.service.MdItemSubstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料替代品 Service 实现
 *
 * 【后端二次校验的几条规则】
 * 1. 禁止自我替代：item_id 不能等于 sub_item_id，否则 BOM 展开、MRP 计算会陷入死循环；
 * 2. 唯一性：同一个主物料下不允许出现同一个替代物料（表上有 uk，这里提前给中文提示）；
 * 3. 双向冗余：库里已经有 A<->B 的双向替代时，再录一条 B->A 属于重复，直接拦掉；
 * 4. 冗余字段由后端从 md_item 重新带出 —— 前端传什么都不信，避免列表出现脏数据。
 *
 */
@Service
public class MdItemSubstituteServiceImpl implements MdItemSubstituteService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    private static final String ONE_WAY = "ONE_WAY";
    private static final String TWO_WAY = "TWO_WAY";

    @Autowired
    private MdItemSubstituteMapper mdItemSubstituteMapper;

    @Autowired
    private MdItemMapper mdItemMapper;

    @Override
    public PageInfo<MdItemSubstitute> page(int pageNum, int pageSize, MdItemSubstitute mdItemSubstitute) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdItemSubstitute> list = mdItemSubstituteMapper.selectByCondition(mdItemSubstitute);
        return new PageInfo<>(list);
    }

    @Override
    public MdItemSubstitute queryById(Long id) {
        return mdItemSubstituteMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdItemSubstituteMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdItemSubstituteMapper.deleteBatch(ids);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int updateById(MdItemSubstitute mdItemSubstitute) {
        // 修改收到的往往只是部分字段，先取出库里的老记录，把没传的字段补齐再校验
        MdItemSubstitute old = mdItemSubstituteMapper.selectById(mdItemSubstitute.getSubstituteId());
        if (old == null) {
            throw new BusinessException("替代关系不存在或已被删除");
        }
        Long itemId = mdItemSubstitute.getItemId() != null ? mdItemSubstitute.getItemId() : old.getItemId();
        Long subItemId = mdItemSubstitute.getSubItemId() != null ? mdItemSubstitute.getSubItemId() : old.getSubItemId();
        checkSelf(itemId, subItemId);

        MdItem item = loadItem(itemId, "主物料");
        MdItem subItem = loadItem(subItemId, "替代物料");

        // 唯一性：命中了但不是自己，说明和别的关系撞了
        MdItemSubstitute exist = mdItemSubstituteMapper.selectByUniqueKey(itemId, subItemId);
        if (exist != null && !exist.getSubstituteId().equals(mdItemSubstitute.getSubstituteId())) {
            throw new BusinessException("已经存在「" + item.getItemName() + "」由「" + subItem.getItemName() + "」替代的关系，不能重复添加");
        }
        checkReverse(itemId, subItemId);

        fillRedundant(mdItemSubstitute, item, subItem);
        checkDateRange(mdItemSubstitute);

        mdItemSubstitute.setUpdateBy(DEFAULT_OPERATOR);
        mdItemSubstitute.setUpdateTime(LocalDateTime.now());
        return mdItemSubstituteMapper.updateById(mdItemSubstitute);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdItemSubstitute mdItemSubstitute) {
        checkSelf(mdItemSubstitute.getItemId(), mdItemSubstitute.getSubItemId());

        MdItem item = loadItem(mdItemSubstitute.getItemId(), "主物料");
        MdItem subItem = loadItem(mdItemSubstitute.getSubItemId(), "替代物料");

        MdItemSubstitute exist = mdItemSubstituteMapper.selectByUniqueKey(mdItemSubstitute.getItemId(), mdItemSubstitute.getSubItemId());
        if (exist != null) {
            throw new BusinessException("已经存在「" + item.getItemName() + "」由「" + subItem.getItemName() + "」替代的关系，不能重复添加");
        }
        checkReverse(mdItemSubstitute.getItemId(), mdItemSubstitute.getSubItemId());

        fillRedundant(mdItemSubstitute, item, subItem);
        checkDateRange(mdItemSubstitute);

        // 没传的字段按业务默认值兜底，不要写空值进库
        if (isBlank(mdItemSubstitute.getSubstituteType())) {
            mdItemSubstitute.setSubstituteType(ONE_WAY);
        }
        if (mdItemSubstitute.getSubstituteRatio() == null) {
            mdItemSubstitute.setSubstituteRatio(BigDecimal.ONE);
        }
        if (mdItemSubstitute.getPriority() == null) {
            mdItemSubstitute.setPriority(1);
        }
        if (isBlank(mdItemSubstitute.getEnableFlag())) {
            mdItemSubstitute.setEnableFlag("Y");
        }

        mdItemSubstitute.setCreateBy(DEFAULT_OPERATOR);
        mdItemSubstitute.setUpdateBy(DEFAULT_OPERATOR);
        mdItemSubstitute.setCreateTime(LocalDateTime.now());
        mdItemSubstitute.setUpdateTime(LocalDateTime.now());
        return mdItemSubstituteMapper.insert(mdItemSubstitute);
    }

    @Override
    public List<MdItemSubstitute> queryByItemId(Long itemId) {
        return mdItemSubstituteMapper.selectByItemId(itemId);
    }

    /* ------------------------------------------------------------------ */
    /*  私有：校验与冗余带出                                                */
    /* ------------------------------------------------------------------ */

    /**
     * 不允许自我替代
     */
    private void checkSelf(Long itemId, Long subItemId) {
        if (itemId != null && itemId.equals(subItemId)) {
            throw new BusinessException("不能把物料配置成自己的替代品");
        }
    }

    /**
     * 反方向已经存在双向替代时，再录一条同方向的记录就是重复数据
     */
    private void checkReverse(Long itemId, Long subItemId) {
        MdItemSubstitute reverse = mdItemSubstituteMapper.selectByUniqueKey(subItemId, itemId);
        if (reverse != null && TWO_WAY.equals(reverse.getSubstituteType())) {
            throw new BusinessException("这两个物料之间已经配置了双向替代，不需要再单独添加一条");
        }
    }

    /**
     * 校验日期区间的合理性
     */
    private void checkDateRange(MdItemSubstitute mdItemSubstitute) {
        if (mdItemSubstitute.getEffectiveDate() != null && mdItemSubstitute.getExpireDate() != null
                && mdItemSubstitute.getExpireDate().isBefore(mdItemSubstitute.getEffectiveDate())) {
            throw new BusinessException("失效日期不能早于生效日期");
        }
    }

    /**
     * 查物料，查不到就抛中文异常 —— 比数据库外键报错友好
     */
    private MdItem loadItem(Long itemId, String label) {
        if (itemId == null) {
            throw new BusinessException(label + "不能为空");
        }
        MdItem mdItem = mdItemMapper.selectById(itemId);
        if (mdItem == null) {
            throw new BusinessException(label + "不存在，请重新选择");
        }
        return mdItem;
    }

    /**
     * 冗余字段一律由后端从 md_item 重新带出。
     * 这些列在列表页要直接展示，不能依赖前端传值 —— 前端传错了列表就是脏数据。
     */
    private void fillRedundant(MdItemSubstitute row, MdItem item, MdItem subItem) {
        if (item != null) {
            row.setItemId(item.getItemId());
            row.setItemCode(item.getItemCode());
            row.setItemName(item.getItemName());
        }
        if (subItem != null) {
            row.setSubItemId(subItem.getItemId());
            row.setSubItemCode(subItem.getItemCode());
            row.setSubItemName(subItem.getItemName());
            row.setSubItemSpec(subItem.getSpecification());
            row.setUnitOfMeasure(subItem.getUnitOfMeasure());
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
