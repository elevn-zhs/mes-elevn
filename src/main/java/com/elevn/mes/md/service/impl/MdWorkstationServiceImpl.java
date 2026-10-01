package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdWorkstation;
import com.elevn.mes.md.mapper.MdWorkstationMapper;
import com.elevn.mes.md.service.MdWorkstationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作站 Service 实现
 *
 */
@Service
public class MdWorkstationServiceImpl implements MdWorkstationService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdWorkstationMapper mdWorkstationMapper;

    @Override
    public PageInfo<MdWorkstation> page(int pageNum, int pageSize, MdWorkstation mdWorkstation) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdWorkstation> list = mdWorkstationMapper.selectByCondition(mdWorkstation);
        return new PageInfo<>(list);
    }

    @Override
    public MdWorkstation queryById(Long id) {
        return mdWorkstationMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdWorkstationMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdWorkstationMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdWorkstation mdWorkstation) {
        if (mdWorkstation.getWorkstationCode() != null && !"".equals(mdWorkstation.getWorkstationCode())) {
            checkCodeUnique(mdWorkstation);
        }
        mdWorkstation.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstation.setUpdateTime(LocalDateTime.now());
        return mdWorkstationMapper.updateById(mdWorkstation);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdWorkstation mdWorkstation) {
        checkCodeUnique(mdWorkstation);
        mdWorkstation.setCreateBy(DEFAULT_OPERATOR);
        mdWorkstation.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstation.setCreateTime(LocalDateTime.now());
        mdWorkstation.setUpdateTime(LocalDateTime.now());
        if (mdWorkstation.getEnableFlag() == null || "".equals(mdWorkstation.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdWorkstation.setEnableFlag("Y");
        }
        return mdWorkstationMapper.insert(mdWorkstation);
    }

    @Override
    public MdWorkstation queryByWorkstationCode(String workstationCode) {
        return mdWorkstationMapper.selectByWorkstationCode(workstationCode);
    }

    @Override
    public List<MdWorkstation> queryByProcessId(Long processId) {
        return mdWorkstationMapper.selectByProcessId(processId);
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     * 两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkCodeUnique(MdWorkstation mdWorkstation) {
        if (mdWorkstation.getWorkstationCode() == null || "".equals(mdWorkstation.getWorkstationCode())) {
            throw new BusinessException("工作站编码不能为空");
        }
        MdWorkstation db = mdWorkstationMapper.selectByWorkstationCode(mdWorkstation.getWorkstationCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (mdWorkstation.getWorkstationId() != null && mdWorkstation.getWorkstationId().equals(db.getWorkstationId())) {
            return;
        }
        throw new BusinessException("工作站编码已存在：" + mdWorkstation.getWorkstationCode());
    }
}
