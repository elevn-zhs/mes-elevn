package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysUser;
import com.elevn.mes.sys.mapper.SysUserMapper;
import com.elevn.mes.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public PageInfo<SysUser> page(int pageNum, int pageSize, SysUser sysUser) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> sysUsers = sysUserMapper.selectByCondition(sysUser);
        return new PageInfo<>(sysUsers);
    }

    @Override
    public SysUser queryById(Long id) {
        return sysUserMapper.selectById(id);
    }

    /**
     * 根据登录账号查询用户，给后续的登录模块用
     */
    @Override
    public SysUser queryByUserName(String userName) {
        return sysUserMapper.selectByUserName(userName);
    }

    @Override
    public int deleteById(Long id) {
        return sysUserMapper.deleteById(id);
    }

    @Override
    public int updateById(SysUser sysUser) {
        // 如果本次修改了登录账号，要保证改完之后的账号在库里不重复
        if (sysUser.getUserName() != null && !"".equals(sysUser.getUserName())){
            checkUserNameUnique(sysUser);
        }
        sysUser.setUpdateBy(DEFAULT_OPERATOR);
        sysUser.setUpdateTime(LocalDateTime.now());
        return sysUserMapper.updateById(sysUser);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysUser sysUser) {
        checkUserNameUnique(sysUser);
        sysUser.setCreateBy(DEFAULT_OPERATOR);
        sysUser.setUpdateBy(DEFAULT_OPERATOR);
        sysUser.setCreateTime(LocalDateTime.now());
        sysUser.setUpdateTime(LocalDateTime.now());

        // TODO 密码加密：目前直接把明文写进了库。
        //  等引入 BCrypt（Spring Security 自带）后，这里要改成
        //  sysUser.setPassword(new BCryptPasswordEncoder().encode(sysUser.getPassword()));
        //  另外 queryById / page 返回用户信息时，记得把 password 字段清掉，不要返回给前端。
        if (sysUser.getStatus() == null || "".equals(sysUser.getStatus())){
            sysUser.setStatus("0");     // 0 正常，往下不用 dictionary 表也能看懂
        }
        return sysUserMapper.insert(sysUser);
    }

    /**
     * 校验登录账号是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话：
     * "登录账号 zhangsan 已存在"。两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkUserNameUnique(SysUser sysUser) {
        if (sysUser.getUserName() == null || "".equals(sysUser.getUserName())){
            throw new BusinessException("登录账号不能为空");
        }
        SysUser dbUser = sysUserMapper.selectByUserName(sysUser.getUserName());
        if (dbUser == null){
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (sysUser.getUserId() != null && sysUser.getUserId().equals(dbUser.getUserId())){
            return;
        }
        throw new BusinessException("登录账号已存在：" + sysUser.getUserName());
    }
}
