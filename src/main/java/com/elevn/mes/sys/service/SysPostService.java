package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysPost;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SysPostService {
    /**
     *
     * @param pageNum
     * @param pageSize
     * @param sysPost
     * @return
     */
    PageInfo<SysPost> page(int pageNum,int pageSize,SysPost sysPost);
    /**
     *
     * @param id
     * @return
     */
    SysPost queryById(Long id);
    /**
     *
     * @param id
     * @return
     */
    int deleteById(Long id);
    /**
     *
     * @param sysPost
     * @return
     */
    int updateById(SysPost sysPost);
    /**
     *
     * @param sysPost
     * @return
     */
    int save(SysPost sysPost);

}
