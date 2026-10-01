package com.elevn.mes.pro.service.impl;

import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.pro.entity.ProProcess;
import com.elevn.mes.pro.entity.ProRouteProcess;
import com.elevn.mes.pro.mapper.ProProcessMapper;
import com.elevn.mes.pro.mapper.ProRouteProcessMapper;
import com.elevn.mes.pro.service.ProRouteProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工艺路线工序明细 Service 实现
 *
 * 【为什么用「整批替换」而不是逐行增删改】
 * 明细行之间有强约束：顺序号要连续、下一道工序要能连成一条链。
 * 逐行改的话，中间任何一步都可能让链暂时处于"断"的状态，校验就没法做。
 * 整批替换 = 前端把整张表编辑好 → 一次提交 → 后端校验整条链 → 旧的全删、新的全插，
 * 校验永远面对的是一条完整的链，简单且永远一致。
 *
 */
@Service
public class ProRouteProcessServiceImpl implements ProRouteProcessService {

    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private ProRouteProcessMapper proRouteProcessMapper;

    @Autowired
    private ProProcessMapper proProcessMapper;

    @Override
    public List<ProRouteProcess> selectByRouteId(Long routeId) {
        return proRouteProcessMapper.selectByRouteId(routeId);
    }

    @Override
    @Transactional
    public int batchSave(Long routeId, List<ProRouteProcess> processList) {
        if (routeId == null) {
            throw new BusinessException("路线ID不能为空");
        }
        if (processList == null || processList.isEmpty()) {
            throw new BusinessException("至少保留一道工序");
        }

        // ===== 校验 1：工序必须存在且启用，顺便回填冗余字段 =====
        Map<Long, ProProcess> processMap = new HashMap<>();
        for (ProRouteProcess item : processList) {
            if (item.getProcessId() == null) {
                throw new BusinessException("第 " + safeOrder(item) + " 行没有选择工序");
            }
            ProProcess process = processMap.computeIfAbsent(item.getProcessId(),
                    id -> proProcessMapper.selectById(id));
            if (process == null) {
                throw new BusinessException("第 " + safeOrder(item) + " 行的工序不存在或已被删除");
            }
            if (!"Y".equals(process.getEnableFlag())) {
                throw new BusinessException("工序[" + process.getProcessCode() + "]已停用，不能加入路线");
            }
            item.setRouteId(routeId);
            item.setProcessCode(process.getProcessCode());
            item.setProcessName(process.getProcessName());
        }

        // ===== 校验 2：同一路线内工序不能重复 =====
        Set<Long> seen = new HashSet<>();
        for (ProRouteProcess item : processList) {
            if (!seen.add(item.getProcessId())) {
                throw new BusinessException("工序[" + item.getProcessCode() + " "
                        + item.getProcessName() + "]在路线中重复出现");
            }
        }

        // ===== 校验 3：顺序号必须从 1 开始连续 =====
        for (int i = 0; i < processList.size(); i++) {
            ProRouteProcess item = processList.get(i);
            if (item.getOrderNum() == null || item.getOrderNum() != i + 1) {
                throw new BusinessException("顺序号必须从 1 开始连续，第 " + (i + 1) + " 行的顺序号是 "
                        + (item.getOrderNum() == null ? "空" : item.getOrderNum()));
            }
        }

        // ===== 回填 next_process 冗余字段：前端按行序组织，下一道 = 下一行的工序，末行没有下一道 =====
        for (int i = 0; i < processList.size(); i++) {
            ProRouteProcess item = processList.get(i);
            if (i < processList.size() - 1) {
                ProRouteProcess next = processList.get(i + 1);
                item.setNextProcessId(next.getProcessId());
                item.setNextProcessCode(next.getProcessCode());
                item.setNextProcessName(next.getProcessName());
            } else {
                // 末工序：数据库此列 not null，约定用 0 表示"没有下一道"
                item.setNextProcessId(0L);
                item.setNextProcessCode(null);
                item.setNextProcessName(null);
            }
        }

        // ===== 校验 4：链不能成环 =====
        // next 链是按行序自动生成的（1→2→…→n→末），天然不会成环；
        // 但这个校验保留为防御：万一以后 next 改成手工指定，这里能兜底。
        checkNoCycle(processList);

        // ===== 整批替换：旧的全删、新的全插 =====
        LocalDateTime now = LocalDateTime.now();
        for (ProRouteProcess item : processList) {
            item.setCreateBy(DEFAULT_OPERATOR);
            item.setCreateTime(now);
        }
        proRouteProcessMapper.deleteByRouteId(routeId);
        return proRouteProcessMapper.insertBatch(processList);
    }

    /**
     * 环检测：从每一行沿 next 链走，最多走 n 步还没到末尾就是环。
     * n 步是关键上限——一条 n 个节点的合法链，最多 n-1 步就该走到末尾。
     */
    private void checkNoCycle(List<ProRouteProcess> processList) {
        Map<Long, Long> nextMap = new HashMap<>();
        for (ProRouteProcess item : processList) {
            nextMap.put(item.getProcessId(), item.getNextProcessId());
        }
        int n = processList.size();
        for (ProRouteProcess start : processList) {
            Long current = start.getProcessId();
            for (int step = 0; step < n; step++) {
                Long next = nextMap.get(current);
                // 走到末工序（0）说明这条链正常结束
                if (next == null || next == 0L) {
                    break;
                }
                current = next;
                if (step == n - 1) {
                    throw new BusinessException("工序流转链出现循环（" + start.getProcessCode()
                            + " 起步走不出去），请检查工序顺序");
                }
            }
        }
    }

    /** 顺序号可能还没填，报错信息里不能出现 null */
    private String safeOrder(ProRouteProcess item) {
        return item.getOrderNum() == null ? "?" : String.valueOf(item.getOrderNum());
    }
}
