package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdUnitMeasure;
import com.elevn.mes.md.mapper.MdUnitMeasureMapper;
import com.elevn.mes.md.service.MdUnitMeasureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 计量单位 Service 实现
 *
 */
@Service
public class MdUnitMeasureServiceImpl implements MdUnitMeasureService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdUnitMeasureMapper mdUnitMeasureMapper;

    @Override
    public PageInfo<MdUnitMeasure> page(int pageNum, int pageSize, MdUnitMeasure mdUnitMeasure) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdUnitMeasure> list = mdUnitMeasureMapper.selectByCondition(mdUnitMeasure);
        return new PageInfo<>(list);
    }

    @Override
    public MdUnitMeasure queryById(Long id) {
        return mdUnitMeasureMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdUnitMeasureMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdUnitMeasureMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdUnitMeasure mdUnitMeasure) {
        if (mdUnitMeasure.getMeasureCode() != null && !"".equals(mdUnitMeasure.getMeasureCode())) {
            checkCodeUnique(mdUnitMeasure);
        }
        mdUnitMeasure.setUpdateBy(DEFAULT_OPERATOR);
        mdUnitMeasure.setUpdateTime(LocalDateTime.now());
        return mdUnitMeasureMapper.updateById(mdUnitMeasure);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdUnitMeasure mdUnitMeasure) {
        checkCodeUnique(mdUnitMeasure);
        mdUnitMeasure.setCreateBy(DEFAULT_OPERATOR);
        mdUnitMeasure.setUpdateBy(DEFAULT_OPERATOR);
        mdUnitMeasure.setCreateTime(LocalDateTime.now());
        mdUnitMeasure.setUpdateTime(LocalDateTime.now());
        if (mdUnitMeasure.getEnableFlag() == null || "".equals(mdUnitMeasure.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdUnitMeasure.setEnableFlag("Y");
        }
        return mdUnitMeasureMapper.insert(mdUnitMeasure);
    }

    @Override
    public MdUnitMeasure queryByMeasureCode(String measureCode) {
        return mdUnitMeasureMapper.selectByMeasureCode(measureCode);
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     * 两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkCodeUnique(MdUnitMeasure mdUnitMeasure) {
        if (mdUnitMeasure.getMeasureCode() == null || "".equals(mdUnitMeasure.getMeasureCode())) {
            throw new BusinessException("计量单位编码不能为空");
        }
        MdUnitMeasure db = mdUnitMeasureMapper.selectByMeasureCode(mdUnitMeasure.getMeasureCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (mdUnitMeasure.getMeasureId() != null && mdUnitMeasure.getMeasureId().equals(db.getMeasureId())) {
            return;
        }
        throw new BusinessException("计量单位编码已存在：" + mdUnitMeasure.getMeasureCode());
    }
}
