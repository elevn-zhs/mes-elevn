package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.sys.entity.SysUser;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SysUserService {
    /**
     *
     * @param pageNum
     * @param pageSize
     * @param sysUser
     * @return
     */
    PageInfo<SysUser> page(int pageNum,int pageSize,SysUser sysUser);
    /**
     *
     * @param id
     * @return
     */
    SysUser queryById(Long id);
    /**
     *
     * @param id
     * @return
     */
    int deleteById(Long id);
    /**
     *
     * @param sysUser
     * @return
     */
    int updateById(SysUser sysUser);
    /**
     *
     * @param sysUser
     * @return
     */
    int save(SysUser sysUser);

    /**
     * 根据登录账号查询用户
     * 这个方法现在暂时没人调用，是给后面的登录模块预留的——
     * 登录时需要根据页面传过来的 userName 把整条用户记录捞出来，再比对密码。
     * @param userName 登录账号
     * @return 用户对象（连同密码一起查出），账号不存在返回 null
     */
    SysUser queryByUserName(String userName);

}
