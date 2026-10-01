package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysDictData;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SysDictDataService {

    /**
     * 根据字典类别查询字典数据列表
     * @param dictType
     * @return
     */
    List<SysDictData> queryByType(String dictType);

    /**
     *
     * @param pageNum
     * @param pageSize
     * @param DictData
     * @return
     */
    PageInfo<SysDictData> page(int pageNum,int pageSize,SysDictData DictData);
    /**
     *
     * @param id
     * @return
     */
    SysDictData queryById(Long id);
    /**
     *
     * @param id
     * @return
     */
    int deleteById(Long id);
    /**
     *
     * @param DictData
     * @return
     */
    int updateById(SysDictData DictData);
    /**
     *
     * @param DictData
     * @return
     */
    int save(SysDictData DictData);

}
