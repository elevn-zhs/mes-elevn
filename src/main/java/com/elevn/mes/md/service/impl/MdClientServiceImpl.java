package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdClient;
import com.elevn.mes.md.mapper.MdClientMapper;
import com.elevn.mes.md.service.MdClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户 Service 实现
 *
 */
@Service
public class MdClientServiceImpl implements MdClientService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdClientMapper mdClientMapper;

    @Override
    public PageInfo<MdClient> page(int pageNum, int pageSize, MdClient mdClient) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdClient> list = mdClientMapper.selectByCondition(mdClient);
        return new PageInfo<>(list);
    }

    @Override
    public MdClient queryById(Long id) {
        return mdClientMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdClientMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdClientMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdClient mdClient) {
        if (mdClient.getClientCode() != null && !"".equals(mdClient.getClientCode())) {
            checkCodeUnique(mdClient);
        }
        mdClient.setUpdateBy(DEFAULT_OPERATOR);
        mdClient.setUpdateTime(LocalDateTime.now());
        return mdClientMapper.updateById(mdClient);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdClient mdClient) {
        checkCodeUnique(mdClient);
        mdClient.setCreateBy(DEFAULT_OPERATOR);
        mdClient.setUpdateBy(DEFAULT_OPERATOR);
        mdClient.setCreateTime(LocalDateTime.now());
        mdClient.setUpdateTime(LocalDateTime.now());
        if (mdClient.getEnableFlag() == null || "".equals(mdClient.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdClient.setEnableFlag("Y");
        }
        return mdClientMapper.insert(mdClient);
    }

    @Override
    public MdClient queryByClientCode(String clientCode) {
        return mdClientMapper.selectByClientCode(clientCode);
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     * 两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkCodeUnique(MdClient mdClient) {
        if (mdClient.getClientCode() == null || "".equals(mdClient.getClientCode())) {
            throw new BusinessException("客户编码不能为空");
        }
        MdClient db = mdClientMapper.selectByClientCode(mdClient.getClientCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (mdClient.getClientId() != null && mdClient.getClientId().equals(db.getClientId())) {
            return;
        }
        throw new BusinessException("客户编码已存在：" + mdClient.getClientCode());
    }
}
