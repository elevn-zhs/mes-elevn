package com.elevn.mes.sys.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.util.DateTools;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.sys.entity.SysCodingRule;
import com.elevn.mes.sys.mapper.SysCodingRuleMapper;
import com.elevn.mes.sys.service.SysCodingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysCodingRuleServiceImpl implements SysCodingRuleService {
    @Autowired
    private SysCodingRuleMapper sysCodingRuleMapper;


    @Override
    public PageInfo<SysCodingRule> page(int pageNum, int pageSize, SysCodingRule codingRule) {
        PageHelper.startPage(pageNum,pageSize);
        List<SysCodingRule> sysDictDatas = sysCodingRuleMapper.selectByCondition(codingRule);
        return new PageInfo<>(sysDictDatas);
    }

    @Override
    public SysCodingRule queryById(Long id) {
        return sysCodingRuleMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return sysCodingRuleMapper.deleteById(id);
    }

    @Override
    public int updateById(SysCodingRule codingRule) {
        return sysCodingRuleMapper.updateById(codingRule);
    }

    @Override
    public SysCodingRule selectByRuleCode(String ruleCode) {
        return sysCodingRuleMapper.selectByRuleCode(ruleCode);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(SysCodingRule codingRule) {
        return sysCodingRuleMapper.insert(codingRule);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String autoCode(String ruleCode) {
        // 查询编码规则（前缀 / 是否带日期 / 流水号补几位，都从这里取）
        SysCodingRule rule = sysCodingRuleMapper.selectByRuleCode(ruleCode);
        if(rule == null){
            throw new BusinessException("编码规则不存在，请先添加规则再生成编码");
        }

        // 取号：交给数据库做 serial_number = serial_number + 1（一条语句，靠行锁防并发）
        //
        // 老写法是"查出来 -> +1 -> 写回去"，两步之间另一个请求可能查到同一个值，
        // 结果两张单拿到同一个流水号。仓库的 14 类单据编号全靠这里取号，
        // 撞号的后果是 uk_doc_code 唯一键报错、用户看到"保存失败"却不知道原因。
        sysCodingRuleMapper.incrementSerialNumber(ruleCode);
        // 自增完再读一次，拿到本次真正占用的号
        rule = sysCodingRuleMapper.selectByRuleCode(ruleCode);

        // 生成编码
        StringBuilder sb = new StringBuilder(rule.getPrefix());
        // 判断是否使用日期字符串
        if(rule.getDateStr() != null && rule.getDateStr() == 1){
            sb.append(DateTools.getDateStr("yyyyMMdd"));
        }
        // 设置序列号
        String sar = String.valueOf(rule.getSerialNumber() == null ? 0L : rule.getSerialNumber());
        // 判断长度，不够前面补 0
        int numberLength = rule.getNumberLength() == null ? 6 : rule.getNumberLength();
        while (sar.length() < numberLength) {
            sar = "0" + sar;
        }
        sb.append(sar);
        return sb.toString();
    }
}
