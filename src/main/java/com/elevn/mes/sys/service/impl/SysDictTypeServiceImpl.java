package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysDictType;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.mapper.SysDictTypeMapper;
import com.elevn.mes.sys.service.SysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class SysDictTypeServiceImpl implements SysDictTypeService {
    @Autowired
    private SysDictTypeMapper sysDictTypeMapper;

    @Override
    public SysDictType queryByType(String dictType) {
        return sysDictTypeMapper.selectByType(dictType);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        int result = 0;
        // 批量处理注意事项： 一次不能处理太多
        if (ids.length > 100){
            throw new BusinessException("批量删除字典数据一次不能超过100条");
        }
        // 执行SQL，一次不要超过20条；【20条只是一个示例】
        int batchSize = 20;
        int endIndex = 0;
        // 循环处理
        int count = (int)Math.ceil(ids.length * 1.0 / batchSize);
        for (int i = 0;i < count;i ++){
            // 获取数组的子数组
            Long[] subArray = getSubArray(ids, i * batchSize, endIndex = (i + 1) * batchSize > ids.length ? ids.length : endIndex);
            result += sysDictTypeMapper.deleteBatch(subArray);
        }
        return result;
    }

    private Long [] getSubArray(Long [] ids,int startIndex,int endIndex){
        Long [] arr = new Long[endIndex - startIndex];
        for (int index = startIndex,i = 0;index < endIndex && i < arr.length;index ++,i++){
            arr[i] = ids[index];
        }
        return arr;
    }

    @Override
    public PageInfo<SysDictType> page(int pageNum, int pageSize, SysDictType dictType) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysDictType> sysDictTypes = sysDictTypeMapper.selectByCondition(dictType);
        return new PageInfo<>(sysDictTypes);
    }

    @Override
    public SysDictType queryById(Long id) {
        return sysDictTypeMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return sysDictTypeMapper.deleteById(id);
    }

    @Override
    public int updateById(SysDictType dictType) {
        // 校验type是否重复
        SysDictType dbDictType = sysDictTypeMapper.selectByDictType(dictType.getDictType());
        if( dbDictType != null && !dbDictType.getDictId().equals(dictType.getDictId())){
            throw new BusinessException("字典类型重复:" + dictType.getDictType());
        }
        // 给updateTime和updateBy
        dictType.setUpdateTime(LocalDateTime.now());
        dictType.setUpdateBy("admin");
        return sysDictTypeMapper.updateById(dictType);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysDictType dictType) {
        // 校验type是否重复
        if(sysDictTypeMapper.selectByDictType(dictType.getDictType()) != null){
            throw new BusinessException("字典类型重复:" + dictType.getDictType());
        }
        // 默认值的设置
        dictType.setCreateBy("admin");
        dictType.setUpdateBy("admin");
        dictType.setCreateTime(LocalDateTime.now());
        dictType.setUpdateTime(LocalDateTime.now());
        return sysDictTypeMapper.insert(dictType);
    }
}
