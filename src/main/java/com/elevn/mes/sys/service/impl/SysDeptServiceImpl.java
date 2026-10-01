package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysDept;
import com.elevn.mes.sys.mapper.SysDeptMapper;
import com.elevn.mes.sys.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysDeptServiceImpl implements SysDeptService {
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Override
    public PageInfo<SysDept> page(int pageNum, int pageSize, SysDept dept) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysDept> sysDepts = sysDeptMapper.selectByCondition(dept);
        return new PageInfo<>(sysDepts);
    }

    @Override
    public SysDept queryById(Long id) {
        return sysDeptMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return sysDeptMapper.deleteById(id);
    }

    @Override
    public int updateById(SysDept dept) {
        // 校验type是否重复
        return sysDeptMapper.updateById(dept);
    }

    @Override
    public List<SysDept> queryAllForTree() {
        // 查询所有的顶层部门
        return queryDeptByParentId(0L);
    }

    private List<SysDept> queryDeptByParentId(Long parentId){
        List<SysDept> depts = sysDeptMapper.selectByParentId(parentId);
        if(depts != null && depts.size() > 0){
            // 确实查询到数据了
            // 遍历列表，递归的查询这个列表中所有部门的子部门
            for (SysDept dept : depts){
                // 继续查询detp的子部门
                List<SysDept> ds = queryDeptByParentId(dept.getDeptId());
                dept.setChildren(ds);
            }
        }
        // 通过parentId没有查询到数据
        return depts;// 直接返回 说明parentId没有子部
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysDept dept) {
        // 校验type是否重复
        return sysDeptMapper.insert(dept);
    }
}
