package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdWorkstationMachine;
import com.elevn.mes.md.entity.MdWorkstation;
import com.elevn.mes.md.mapper.MdWorkstationMachineMapper;
import com.elevn.mes.md.service.MdWorkstationMachineService;
import com.elevn.mes.md.service.MdWorkstationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作站设备资源 Service 实现
 *
 * 保存 / 修改前做两层业务校验：
 *   1. 父级校验 —— workstationId 必须是真实存在的工作站，防止前端传脏 ID
 *   2. 重复绑定校验 —— 同一工作站 + 同一设备只能有一条记录，
 *      数据库层没有唯一索引兜底（逻辑删除下建唯一索引会被 del_flag 干扰），所以在代码层拦截
 *
 */
@Service
public class MdWorkstationMachineServiceImpl implements MdWorkstationMachineService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdWorkstationMachineMapper mdWorkstationMachineMapper;

    @Autowired
    private MdWorkstationService mdWorkstationService;

    @Override
    public PageInfo<MdWorkstationMachine> page(int pageNum, int pageSize, MdWorkstationMachine mdWorkstationMachine) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdWorkstationMachine> list = mdWorkstationMachineMapper.selectByCondition(mdWorkstationMachine);
        return new PageInfo<>(list);
    }

    @Override
    public MdWorkstationMachine queryById(Long id) {
        return mdWorkstationMachineMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdWorkstationMachineMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdWorkstationMachineMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdWorkstationMachine mdWorkstationMachine) {
        checkBeforeSave(mdWorkstationMachine, mdWorkstationMachine.getRecordId());
        mdWorkstationMachine.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstationMachine.setUpdateTime(LocalDateTime.now());
        return mdWorkstationMachineMapper.updateById(mdWorkstationMachine);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdWorkstationMachine mdWorkstationMachine) {
        checkBeforeSave(mdWorkstationMachine, null);
        mdWorkstationMachine.setCreateBy(DEFAULT_OPERATOR);
        mdWorkstationMachine.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstationMachine.setCreateTime(LocalDateTime.now());
        mdWorkstationMachine.setUpdateTime(LocalDateTime.now());
        return mdWorkstationMachineMapper.insert(mdWorkstationMachine);
    }

    /**
     * 保存 / 修改前的业务校验
     * @param mdWorkstationMachine 待保存的数据
     * @param excludeRecordId 修改时传自身主键（查重时排除自己），新增传 null
     */
    private void checkBeforeSave(MdWorkstationMachine mdWorkstationMachine, Long excludeRecordId) {
        // 父级校验：工作站必须真实存在
        if (mdWorkstationMachine.getWorkstationId() == null
                || mdWorkstationService.queryById(mdWorkstationMachine.getWorkstationId()) == null) {
            throw new BusinessException("工作站不存在，请刷新页面后重新选择");
        }
        // 设备校验：设备必须真实存在
        if (mdWorkstationMachine.getMachineryId() == null) {
            throw new BusinessException("请选择设备");
        }
        // 重复绑定校验：同一工作站 + 同一设备只能出现一次
        int dup = mdWorkstationMachineMapper.selectDuplicate(
                mdWorkstationMachine.getWorkstationId(),
                mdWorkstationMachine.getMachineryId(),
                excludeRecordId);
        if (dup > 0) {
            throw new BusinessException("该设备已绑定此工作站，请直接修改原记录的数量");
        }
    }

    @Override
    public List<MdWorkstationMachine> queryByWorkstationId(Long workstationId) {
        return mdWorkstationMachineMapper.selectByWorkstationId(workstationId);
    }
}
