package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItemBatchConfig;
import com.elevn.mes.md.mapper.MdItemBatchConfigMapper;
import com.elevn.mes.md.service.MdItemBatchConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料批次属性配置 Service 实现
 *
 */
@Service
public class MdItemBatchConfigServiceImpl implements MdItemBatchConfigService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdItemBatchConfigMapper mdItemBatchConfigMapper;

    @Override
    public PageInfo<MdItemBatchConfig> page(int pageNum, int pageSize, MdItemBatchConfig mdItemBatchConfig) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdItemBatchConfig> list = mdItemBatchConfigMapper.selectByCondition(mdItemBatchConfig);
        return new PageInfo<>(list);
    }

    @Override
    public MdItemBatchConfig queryById(Long id) {
        return mdItemBatchConfigMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdItemBatchConfigMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdItemBatchConfigMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdItemBatchConfig mdItemBatchConfig) {
        mdItemBatchConfig.setUpdateBy(DEFAULT_OPERATOR);
        mdItemBatchConfig.setUpdateTime(LocalDateTime.now());
        return mdItemBatchConfigMapper.updateById(mdItemBatchConfig);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdItemBatchConfig mdItemBatchConfig) {
        mdItemBatchConfig.setCreateBy(DEFAULT_OPERATOR);
        mdItemBatchConfig.setUpdateBy(DEFAULT_OPERATOR);
        mdItemBatchConfig.setCreateTime(LocalDateTime.now());
        mdItemBatchConfig.setUpdateTime(LocalDateTime.now());
        if (mdItemBatchConfig.getEnableFlag() == null || "".equals(mdItemBatchConfig.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdItemBatchConfig.setEnableFlag("Y");
        }
        return mdItemBatchConfigMapper.insert(mdItemBatchConfig);
    }

    @Override
    public MdItemBatchConfig queryByItemId(Long itemId) {
        return mdItemBatchConfigMapper.selectByItemId(itemId);
    }
}
