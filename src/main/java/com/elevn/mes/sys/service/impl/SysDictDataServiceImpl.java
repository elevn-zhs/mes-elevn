package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysDictData;
import com.elevn.mes.sys.mapper.SysDictDataMapper;
import com.elevn.mes.sys.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDictDataServiceImpl implements SysDictDataService {
    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @Override
    public List<SysDictData> queryByType(String dictType) {
        return sysDictDataMapper.selectByDictType(dictType);
    }

    @Override
    public PageInfo<SysDictData> page(int pageNum, int pageSize, SysDictData dictData) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysDictData> sysDictDatas = sysDictDataMapper.selectByCondition(dictData);
        return new PageInfo<>(sysDictDatas);
    }

    @Override
    public SysDictData queryById(Long id) {
        return sysDictDataMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return sysDictDataMapper.deleteById(id);
    }

    @Override
    public int updateById(SysDictData dictData) {
        return sysDictDataMapper.updateById(dictData);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysDictData dictData) {
        return sysDictDataMapper.insert(dictData);
    }
}
