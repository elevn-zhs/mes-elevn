package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysLog;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SysLogService {
    /**
     *
     * @param pageNum
     * @param pageSize
     * @param sysLog
     * @return
     */
    PageInfo<SysLog> page(int pageNum,int pageSize,SysLog sysLog);
    /**
     *
     * @param id
     * @return
     */
    SysLog queryById(Long id);
    /**
     *
     * @param id
     * @return
     */
    int deleteById(Long id);
    /**
     *
     * @param sysLog
     * @return
     */
    int updateById(SysLog sysLog);
    /**
     *
     * @param sysLog
     * @return
     */
    int save(SysLog sysLog);

}
