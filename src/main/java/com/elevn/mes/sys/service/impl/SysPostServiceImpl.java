package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysDept;
import com.elevn.mes.sys.entity.SysPost;
import com.elevn.mes.sys.mapper.SysPostMapper;
import com.elevn.mes.sys.mapper.SysPostMapper;
import com.elevn.mes.sys.service.SysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysPostServiceImpl implements SysPostService {
    @Autowired
    private SysPostMapper postMapper;
    @Override
    public PageInfo<SysPost> page(int pageNum, int pageSize, SysPost post) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysPost> posts = postMapper.selectByCondition(post);
        return new PageInfo<>(posts);
    }

    @Override
    public SysPost queryById(Long id) {
        return postMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return postMapper.deleteById(id);
    }

    @Override
    public int updateById(SysPost post) {
        SysPost params = new SysPost();
        params.setPostName(post.getPostName());
        List<SysPost> posts = postMapper.selectByCondition(params);
        // 校验岗位名称和岗位编码不能重复
        if (posts != null && posts.size() > 0 && !posts.get(0).getPostId().equals(post.getPostId())){
            throw new BusinessException("岗位名称已经存在，请更换岗位名称");
        }
        params.setPostName(null);
        params.setPostCode(post.getPostCode());
        posts = postMapper.selectByCondition(params);
        // 校验岗位名称和岗位编码不能重复
        if (posts != null && posts.size() > 0 && !posts.get(0).getPostId().equals(post.getPostId())){
            throw new BusinessException("岗位编码已经存在，请更换岗位编码");
        }
        post.setUpdateTime(LocalDateTime.now());
        post.setUpdateBy("admin");
        return postMapper.updateById(post);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysPost post) {
        SysPost params = new SysPost();
        params.setPostName(post.getPostName());
        List<SysPost> posts = postMapper.selectByCondition(params);
        // 校验岗位名称和岗位编码不能重复
        if (posts != null && posts.size() > 0){
            throw new BusinessException("岗位名称已经存在，请更换岗位名称");
        }
        params.setPostName(null);
        params.setPostCode(post.getPostCode());
        posts = postMapper.selectByCondition(params);
        // 校验岗位名称和岗位编码不能重复
        if (posts != null && posts.size() > 0){
            throw new BusinessException("岗位编码已经存在，请更换岗位编码");
        }
        post.setCreateTime(LocalDateTime.now());
        post.setCreateBy("admin");
        post.setUpdateTime(LocalDateTime.now());
        post.setUpdateBy("admin");
        return postMapper.insert(post);
    }
}
