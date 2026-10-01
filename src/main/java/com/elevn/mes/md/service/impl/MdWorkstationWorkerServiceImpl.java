package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdWorkstationWorker;
import com.elevn.mes.md.entity.MdWorkstation;
import com.elevn.mes.md.mapper.MdWorkstationWorkerMapper;
import com.elevn.mes.md.service.MdWorkstationWorkerService;
import com.elevn.mes.md.service.MdWorkstationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作站人力资源 Service 实现
 *
 */
@Service
public class MdWorkstationWorkerServiceImpl implements MdWorkstationWorkerService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdWorkstationWorkerMapper mdWorkstationWorkerMapper;

    @Autowired
    private MdWorkstationService mdWorkstationService;

    @Override
    public PageInfo<MdWorkstationWorker> page(int pageNum, int pageSize, MdWorkstationWorker mdWorkstationWorker) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdWorkstationWorker> list = mdWorkstationWorkerMapper.selectByCondition(mdWorkstationWorker);
        return new PageInfo<>(list);
    }

    @Override
    public MdWorkstationWorker queryById(Long id) {
        return mdWorkstationWorkerMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdWorkstationWorkerMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdWorkstationWorkerMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdWorkstationWorker mdWorkstationWorker) {
        checkBeforeSave(mdWorkstationWorker, mdWorkstationWorker.getRecordId());
        mdWorkstationWorker.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstationWorker.setUpdateTime(LocalDateTime.now());
        return mdWorkstationWorkerMapper.updateById(mdWorkstationWorker);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdWorkstationWorker mdWorkstationWorker) {
        checkBeforeSave(mdWorkstationWorker, null);
        mdWorkstationWorker.setCreateBy(DEFAULT_OPERATOR);
        mdWorkstationWorker.setUpdateBy(DEFAULT_OPERATOR);
        mdWorkstationWorker.setCreateTime(LocalDateTime.now());
        mdWorkstationWorker.setUpdateTime(LocalDateTime.now());
        return mdWorkstationWorkerMapper.insert(mdWorkstationWorker);
    }

    /**
     * 保存 / 修改前的业务校验
     * @param mdWorkstationWorker 待保存的数据
     * @param excludeRecordId 修改时传自身主键（查重时排除自己），新增传 null
     */
    private void checkBeforeSave(MdWorkstationWorker mdWorkstationWorker, Long excludeRecordId) {
        // 父级校验：工作站必须真实存在
        if (mdWorkstationWorker.getWorkstationId() == null
                || mdWorkstationService.queryById(mdWorkstationWorker.getWorkstationId()) == null) {
            throw new BusinessException("工作站不存在，请刷新页面后重新选择");
        }
        // 岗位校验：岗位必须真实存在
        if (mdWorkstationWorker.getPostId() == null) {
            throw new BusinessException("请选择岗位");
        }
        // 重复绑定校验：同一工作站 + 同一岗位只能出现一次
        int dup = mdWorkstationWorkerMapper.selectDuplicate(
                mdWorkstationWorker.getWorkstationId(),
                mdWorkstationWorker.getPostId(),
                excludeRecordId);
        if (dup > 0) {
            throw new BusinessException("该岗位已配置到此工作站，请直接修改原记录的数量");
        }
    }

    @Override
    public List<MdWorkstationWorker> queryByWorkstationId(Long workstationId) {
        return mdWorkstationWorkerMapper.selectByWorkstationId(workstationId);
    }
}
