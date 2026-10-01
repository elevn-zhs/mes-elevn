package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdProductBom;
import com.elevn.mes.md.mapper.MdProductBomMapper;
import com.elevn.mes.md.service.MdProductBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品BOM Service 实现
 *
 */
@Service
public class MdProductBomServiceImpl implements MdProductBomService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdProductBomMapper mdProductBomMapper;

    @Override
    public PageInfo<MdProductBom> page(int pageNum, int pageSize, MdProductBom mdProductBom) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdProductBom> list = mdProductBomMapper.selectByCondition(mdProductBom);
        return new PageInfo<>(list);
    }

    @Override
    public MdProductBom queryById(Long id) {
        return mdProductBomMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdProductBomMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdProductBomMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdProductBom mdProductBom) {
        mdProductBom.setUpdateBy(DEFAULT_OPERATOR);
        mdProductBom.setUpdateTime(LocalDateTime.now());
        return mdProductBomMapper.updateById(mdProductBom);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdProductBom mdProductBom) {
        mdProductBom.setCreateBy(DEFAULT_OPERATOR);
        mdProductBom.setUpdateBy(DEFAULT_OPERATOR);
        mdProductBom.setCreateTime(LocalDateTime.now());
        mdProductBom.setUpdateTime(LocalDateTime.now());
        if (mdProductBom.getEnableFlag() == null || "".equals(mdProductBom.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdProductBom.setEnableFlag("Y");
        }
        return mdProductBomMapper.insert(mdProductBom);
    }

    @Override
    public List<MdProductBom> queryByItemId(Long itemId) {
        return mdProductBomMapper.selectByItemId(itemId);
    }
}
