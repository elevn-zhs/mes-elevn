package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysDictType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SysDictTypeService {
    /**
     *
     * @param dictType
     * @return
     */
    SysDictType queryByType(String dictType);

    /**
     * 批量删除字典
     * @param ids
     * @return
     */
    int deleteBatch(Long [] ids);

    /**
     *
     * @param pageNum
     * @param pageSize
     * @param dictType
     * @return
     */
    PageInfo<SysDictType> page(int pageNum,int pageSize,SysDictType dictType);
    /**
     *
     * @param id
     * @return
     */
    SysDictType queryById(Long id);
    /**
     *
     * @param id
     * @return
     */
    int deleteById(Long id);
    /**
     *
     * @param dictType
     * @return
     */
    int updateById(SysDictType dictType);
    /**
     *
     * @param dictType
     * @return
     */
    int save(SysDictType dictType);

}
