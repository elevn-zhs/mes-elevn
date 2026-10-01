package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdProductSip;
import com.elevn.mes.md.mapper.MdProductSipMapper;
import com.elevn.mes.md.service.MdProductSipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品SIP Service 实现
 *
 */
@Service
public class MdProductSipServiceImpl implements MdProductSipService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdProductSipMapper mdProductSipMapper;

    @Override
    public PageInfo<MdProductSip> page(int pageNum, int pageSize, MdProductSip mdProductSip) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdProductSip> list = mdProductSipMapper.selectByCondition(mdProductSip);
        return new PageInfo<>(list);
    }

    @Override
    public MdProductSip queryById(Long id) {
        return mdProductSipMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdProductSipMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdProductSipMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdProductSip mdProductSip) {
        mdProductSip.setUpdateBy(DEFAULT_OPERATOR);
        mdProductSip.setUpdateTime(LocalDateTime.now());
        return mdProductSipMapper.updateById(mdProductSip);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdProductSip mdProductSip) {
        mdProductSip.setCreateBy(DEFAULT_OPERATOR);
        mdProductSip.setUpdateBy(DEFAULT_OPERATOR);
        mdProductSip.setCreateTime(LocalDateTime.now());
        mdProductSip.setUpdateTime(LocalDateTime.now());
        return mdProductSipMapper.insert(mdProductSip);
    }

    @Override
    public List<MdProductSip> queryByItemId(Long itemId) {
        return mdProductSipMapper.selectByItemId(itemId);
    }
}
