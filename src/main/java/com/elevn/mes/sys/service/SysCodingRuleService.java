package com.elevn.mes.sys.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysCodingRule;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SysCodingRuleService {


    /**
     *
     * @param pageNum
     * @param pageSize
     * @param codingRule
     * @return
     */
    PageInfo<SysCodingRule> page(int pageNum,int pageSize,SysCodingRule codingRule);
    /**
     *
     * @param id
     * @return
     */
    SysCodingRule queryById(Long id);
    /**
     *
     * @param id
     * @return
     */
    int deleteById(Long id);
    /**
     *
     * @param codingRule
     * @return
     */
    int updateById(SysCodingRule codingRule);

    SysCodingRule selectByRuleCode(String ruleCode);
    /**
     *
     * @param codingRule
     * @return
     */
    int save(SysCodingRule codingRule);

    String autoCode(String ruleCode);
}
