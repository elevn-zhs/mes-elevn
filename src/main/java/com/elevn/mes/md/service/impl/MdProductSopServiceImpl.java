package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdProductSop;
import com.elevn.mes.md.mapper.MdProductSopMapper;
import com.elevn.mes.md.service.MdProductSopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品SOP Service 实现
 *
 */
@Service
public class MdProductSopServiceImpl implements MdProductSopService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdProductSopMapper mdProductSopMapper;

    @Override
    public PageInfo<MdProductSop> page(int pageNum, int pageSize, MdProductSop mdProductSop) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdProductSop> list = mdProductSopMapper.selectByCondition(mdProductSop);
        return new PageInfo<>(list);
    }

    @Override
    public MdProductSop queryById(Long id) {
        return mdProductSopMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdProductSopMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdProductSopMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdProductSop mdProductSop) {
        mdProductSop.setUpdateBy(DEFAULT_OPERATOR);
        mdProductSop.setUpdateTime(LocalDateTime.now());
        return mdProductSopMapper.updateById(mdProductSop);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdProductSop mdProductSop) {
        mdProductSop.setCreateBy(DEFAULT_OPERATOR);
        mdProductSop.setUpdateBy(DEFAULT_OPERATOR);
        mdProductSop.setCreateTime(LocalDateTime.now());
        mdProductSop.setUpdateTime(LocalDateTime.now());
        return mdProductSopMapper.insert(mdProductSop);
    }

    @Override
    public List<MdProductSop> queryByItemId(Long itemId) {
        return mdProductSopMapper.selectByItemId(itemId);
    }
}
