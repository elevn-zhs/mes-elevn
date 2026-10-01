package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdVendor;
import com.elevn.mes.md.mapper.MdVendorMapper;
import com.elevn.mes.md.service.MdVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商 Service 实现
 *
 */
@Service
public class MdVendorServiceImpl implements MdVendorService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdVendorMapper mdVendorMapper;

    @Override
    public PageInfo<MdVendor> page(int pageNum, int pageSize, MdVendor mdVendor) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdVendor> list = mdVendorMapper.selectByCondition(mdVendor);
        return new PageInfo<>(list);
    }

    @Override
    public MdVendor queryById(Long id) {
        return mdVendorMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdVendorMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdVendorMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdVendor mdVendor) {
        if (mdVendor.getVendorCode() != null && !"".equals(mdVendor.getVendorCode())) {
            checkCodeUnique(mdVendor);
        }
        mdVendor.setUpdateBy(DEFAULT_OPERATOR);
        mdVendor.setUpdateTime(LocalDateTime.now());
        return mdVendorMapper.updateById(mdVendor);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdVendor mdVendor) {
        checkCodeUnique(mdVendor);
        mdVendor.setCreateBy(DEFAULT_OPERATOR);
        mdVendor.setUpdateBy(DEFAULT_OPERATOR);
        mdVendor.setCreateTime(LocalDateTime.now());
        mdVendor.setUpdateTime(LocalDateTime.now());
        if (mdVendor.getEnableFlag() == null || "".equals(mdVendor.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdVendor.setEnableFlag("Y");
        }
        return mdVendorMapper.insert(mdVendor);
    }

    @Override
    public MdVendor queryByVendorCode(String vendorCode) {
        return mdVendorMapper.selectByVendorCode(vendorCode);
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     * 两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkCodeUnique(MdVendor mdVendor) {
        if (mdVendor.getVendorCode() == null || "".equals(mdVendor.getVendorCode())) {
            throw new BusinessException("供应商编码不能为空");
        }
        MdVendor db = mdVendorMapper.selectByVendorCode(mdVendor.getVendorCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (mdVendor.getVendorId() != null && mdVendor.getVendorId().equals(db.getVendorId())) {
            return;
        }
        throw new BusinessException("供应商编码已存在：" + mdVendor.getVendorCode());
    }
}
