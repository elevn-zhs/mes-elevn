package com.elevn.mes.pro.service.impl;

import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.pro.entity.ProProcess;
import com.elevn.mes.pro.entity.ProProcessContent;
import com.elevn.mes.pro.mapper.ProProcessContentMapper;
import com.elevn.mes.pro.mapper.ProProcessMapper;
import com.elevn.mes.pro.service.ProProcessContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工序内容 Service 实现
 *
 * 子表的校验重心只有一条：processId 必须指向一个真实存在的工序。
 * 冗余校验交给数据库外键兜底，这里用 BusinessException 抛人话。
 *
 */
@Service
public class ProProcessContentServiceImpl implements ProProcessContentService {

    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private ProProcessContentMapper proProcessContentMapper;

    @Autowired
    private ProProcessMapper proProcessMapper;

    @Override
    public List<ProProcessContent> selectByProcessId(Long processId) {
        return proProcessContentMapper.selectByProcessId(processId);
    }

    @Override
    public int save(ProProcessContent proProcessContent) {
        checkProcessExists(proProcessContent.getProcessId());
        proProcessContent.setCreateBy(DEFAULT_OPERATOR);
        proProcessContent.setUpdateBy(DEFAULT_OPERATOR);
        proProcessContent.setCreateTime(LocalDateTime.now());
        proProcessContent.setUpdateTime(LocalDateTime.now());
        return proProcessContentMapper.insert(proProcessContent);
    }

    @Override
    public int updateById(ProProcessContent proProcessContent) {
        if (proProcessContent.getProcessId() != null) {
            checkProcessExists(proProcessContent.getProcessId());
        }
        proProcessContent.setUpdateBy(DEFAULT_OPERATOR);
        proProcessContent.setUpdateTime(LocalDateTime.now());
        return proProcessContentMapper.updateById(proProcessContent);
    }

    @Override
    public int deleteById(Long id) {
        return proProcessContentMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return proProcessContentMapper.deleteBatch(ids);
    }

    @Override
    public int deleteByProcessId(Long processId) {
        return proProcessContentMapper.deleteByProcessId(processId);
    }

    /**
     * 校验所属工序真实存在（L3）
     * 子表数据没有独立意义，挂在一个不存在的工序上等于脏数据
     */
    private void checkProcessExists(Long processId) {
        ProProcess db = proProcessMapper.selectById(processId);
        if (db == null) {
            throw new BusinessException("所属工序不存在或已被删除，请刷新后重试");
        }
    }
}
