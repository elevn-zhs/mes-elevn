package com.elevn.mes.dv.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.dv.entity.DvMachinery;
import com.elevn.mes.dv.mapper.DvMachineryMapper;
import com.elevn.mes.dv.service.DvMachineryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 设备表 Service 实现
 *
 */
@Service
public class DvMachineryServiceImpl implements DvMachineryService {

    @Autowired
    private DvMachineryMapper dvMachineryMapper;

    @Override
    public PageInfo<DvMachinery> page(int pageNum, int pageSize, DvMachinery dvMachinery) {
        PageHelper.startPage(pageNum, pageSize);
        List<DvMachinery> list = dvMachineryMapper.selectByCondition(dvMachinery);
        return new PageInfo<>(list);
    }

    @Override
    public DvMachinery queryById(Long id) {
        return dvMachineryMapper.selectById(id);
    }
}
