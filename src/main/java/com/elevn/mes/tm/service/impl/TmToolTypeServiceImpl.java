package com.elevn.mes.tm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.tm.entity.TmToolType;
import com.elevn.mes.tm.mapper.TmToolTypeMapper;
import com.elevn.mes.tm.service.TmToolTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工装夹具类型表 Service 实现
 *
 */
@Service
public class TmToolTypeServiceImpl implements TmToolTypeService {

    @Autowired
    private TmToolTypeMapper tmToolTypeMapper;

    @Override
    public PageInfo<TmToolType> page(int pageNum, int pageSize, TmToolType tmToolType) {
        PageHelper.startPage(pageNum, pageSize);
        List<TmToolType> list = tmToolTypeMapper.selectByCondition(tmToolType);
        return new PageInfo<>(list);
    }

    @Override
    public TmToolType queryById(Long id) {
        return tmToolTypeMapper.selectById(id);
    }
}
