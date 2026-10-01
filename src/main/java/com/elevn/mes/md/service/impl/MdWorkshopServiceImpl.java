package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdWorkshop;
import com.elevn.mes.md.mapper.MdWorkshopMapper;
import com.elevn.mes.md.service.MdWorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车间 Service 实现
 *
 */
@Service
public class MdWorkshopServiceImpl implements MdWorkshopService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdWorkshopMapper mdWorkshopMapper;

    @Override
    public PageInfo<MdWorkshop> page(int pageNum, int pageSize, MdWorkshop mdWorkshop) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdWorkshop> list = mdWorkshopMapper.selectByCondition(mdWorkshop);
        return new PageInfo<>(list);
    }

    @Override
    public MdWorkshop queryById(Long id) {
        return mdWorkshopMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdWorkshopMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdWorkshopMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdWorkshop mdWorkshop) {
        if (mdWorkshop.getWorkshopCode() != null && !"".equals(mdWorkshop.getWorkshopCode())) {
            checkCodeUnique(mdWorkshop);
        }
        mdWorkshop.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkshop.setUpdateTime(LocalDateTime.now());
        return mdWorkshopMapper.updateById(mdWorkshop);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdWorkshop mdWorkshop) {
        checkCodeUnique(mdWorkshop);
        mdWorkshop.setCreateBy(DEFAULT_OPERATOR);
        mdWorkshop.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkshop.setCreateTime(LocalDateTime.now());
        mdWorkshop.setUpdateTime(LocalDateTime.now());
        if (mdWorkshop.getEnableFlag() == null || "".equals(mdWorkshop.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdWorkshop.setEnableFlag("Y");
        }
        return mdWorkshopMapper.insert(mdWorkshop);
    }

    @Override
    public MdWorkshop queryByWorkshopCode(String workshopCode) {
        return mdWorkshopMapper.selectByWorkshopCode(workshopCode);
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     * 两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkCodeUnique(MdWorkshop mdWorkshop) {
        if (mdWorkshop.getWorkshopCode() == null || "".equals(mdWorkshop.getWorkshopCode())) {
            throw new BusinessException("车间编码不能为空");
        }
        MdWorkshop db = mdWorkshopMapper.selectByWorkshopCode(mdWorkshop.getWorkshopCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (mdWorkshop.getWorkshopId() != null && mdWorkshop.getWorkshopId().equals(db.getWorkshopId())) {
            return;
        }
        throw new BusinessException("车间编码已存在：" + mdWorkshop.getWorkshopCode());
    }
}
