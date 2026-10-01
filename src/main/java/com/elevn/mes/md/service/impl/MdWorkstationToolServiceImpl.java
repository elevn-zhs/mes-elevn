package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdWorkstationTool;
import com.elevn.mes.md.entity.MdWorkstation;
import com.elevn.mes.md.mapper.MdWorkstationToolMapper;
import com.elevn.mes.md.service.MdWorkstationToolService;
import com.elevn.mes.md.service.MdWorkstationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作站工装夹具 Service 实现
 *
 * 保存 / 修改前做两层业务校验：
 *   1. 父级校验 —— workstationId 必须是真实存在的工作站
 *   2. 重复绑定校验 —— 同一工作站 + 同一工装类型只能有一条记录
 *
 */
@Service
public class MdWorkstationToolServiceImpl implements MdWorkstationToolService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdWorkstationToolMapper mdWorkstationToolMapper;

    @Autowired
    private MdWorkstationService mdWorkstationService;

    @Override
    public PageInfo<MdWorkstationTool> page(int pageNum, int pageSize, MdWorkstationTool mdWorkstationTool) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdWorkstationTool> list = mdWorkstationToolMapper.selectByCondition(mdWorkstationTool);
        return new PageInfo<>(list);
    }

    @Override
    public MdWorkstationTool queryById(Long id) {
        return mdWorkstationToolMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdWorkstationToolMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdWorkstationToolMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdWorkstationTool mdWorkstationTool) {
        checkBeforeSave(mdWorkstationTool, mdWorkstationTool.getRecordId());
        mdWorkstationTool.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstationTool.setUpdateTime(LocalDateTime.now());
        return mdWorkstationToolMapper.updateById(mdWorkstationTool);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdWorkstationTool mdWorkstationTool) {
        checkBeforeSave(mdWorkstationTool, null);
        mdWorkstationTool.setCreateBy(DEFAULT_OPERATOR);
        mdWorkstationTool.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstationTool.setCreateTime(LocalDateTime.now());
        mdWorkstationTool.setUpdateTime(LocalDateTime.now());
        return mdWorkstationToolMapper.insert(mdWorkstationTool);
    }

    /**
     * 保存 / 修改前的业务校验
     * @param mdWorkstationTool 待保存的数据
     * @param excludeRecordId 修改时传自身主键（查重时排除自己），新增传 null
     */
    private void checkBeforeSave(MdWorkstationTool mdWorkstationTool, Long excludeRecordId) {
        // 父级校验：工作站必须真实存在
        if (mdWorkstationTool.getWorkstationId() == null
                || mdWorkstationService.queryById(mdWorkstationTool.getWorkstationId()) == null) {
            throw new BusinessException("工作站不存在，请刷新页面后重新选择");
        }
        // 工装类型校验：必须真实存在
        if (mdWorkstationTool.getToolTypeId() == null) {
            throw new BusinessException("请选择工装夹具类型");
        }
        // 重复绑定校验：同一工作站 + 同一工装类型只能出现一次
        int dup = mdWorkstationToolMapper.selectDuplicate(
                mdWorkstationTool.getWorkstationId(),
                mdWorkstationTool.getToolTypeId(),
                excludeRecordId);
        if (dup > 0) {
            throw new BusinessException("该工装类型已绑定此工作站，请直接修改原记录的数量");
        }
    }

    @Override
    public List<MdWorkstationTool> queryByWorkstationId(Long workstationId) {
        return mdWorkstationToolMapper.selectByWorkstationId(workstationId);
    }
}
