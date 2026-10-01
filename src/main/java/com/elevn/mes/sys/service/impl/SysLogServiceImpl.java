package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysLog;
import com.elevn.mes.sys.mapper.SysLogMapper;
import com.elevn.mes.sys.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysLogServiceImpl implements SysLogService {
    @Autowired
    private SysLogMapper SysLogMapper;
    @Override
    public PageInfo<SysLog> page(int pageNum, int pageSize, SysLog sysLog) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysLog> SysLogs = SysLogMapper.selectByCondition(sysLog);
        return new PageInfo<>(SysLogs);
    }

    @Override
    public SysLog queryById(Long id) {
        return SysLogMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return SysLogMapper.deleteById(id);
    }

    @Override
    public int updateById(SysLog sysLog) {
        // 校验type是否重复
        return SysLogMapper.updateById(sysLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysLog sysLog) {
        // 校验type是否重复
        return SysLogMapper.insert(sysLog);
    }
}
