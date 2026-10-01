package com.elevn.mes.pro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.pro.entity.ProProcess;
import com.elevn.mes.pro.mapper.ProProcessMapper;
import com.elevn.mes.pro.service.ProProcessContentService;
import com.elevn.mes.pro.service.ProProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工序 Service 实现
 *
 * L3 业务校验：
 *   1. 编码唯一 —— 工序编码是全厂通用语言，重复了路线和工作站就没法对
 *   2. 删除引用检查 —— 工序被工作站挂接、被工艺路线引用时，删了会让下游数据变悬空引用，
 *      所以删之前先数一遍引用次数，> 0 直接拒绝
 *
 */
@Service
public class ProProcessServiceImpl implements ProProcessService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private ProProcessMapper proProcessMapper;

    @Autowired
    private ProProcessContentService proProcessContentService;

    @Override
    public PageInfo<ProProcess> page(int pageNum, int pageSize, ProProcess proProcess) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProProcess> list = proProcessMapper.selectByCondition(proProcess);
        return new PageInfo<>(list);
    }

    @Override
    public ProProcess queryById(Long id) {
        ProProcess proProcess = proProcessMapper.selectById(id);
        if (proProcess != null) {
            // 详情页一次性把工序内容也带出去，前端少发一次请求
            proProcess.setContentList(proProcessContentService.selectByProcessId(id));
        }
        return proProcess;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(ProProcess proProcess) {
        checkCodeUnique(proProcess);
        proProcess.setCreateBy(DEFAULT_OPERATOR);
        proProcess.setUpdateBy(DEFAULT_OPERATOR);
        proProcess.setCreateTime(LocalDateTime.now());
        proProcess.setUpdateTime(LocalDateTime.now());
        if (proProcess.getEnableFlag() == null || "".equals(proProcess.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            proProcess.setEnableFlag("Y");
        }
        return proProcessMapper.insert(proProcess);
    }

    @Override
    public int updateById(ProProcess proProcess) {
        if (proProcess.getProcessCode() != null && !"".equals(proProcess.getProcessCode())) {
            checkCodeUnique(proProcess);
        }
        proProcess.setUpdateBy(DEFAULT_OPERATOR);
        proProcess.setUpdateTime(LocalDateTime.now());
        return proProcessMapper.updateById(proProcess);
    }

    @Override
    public int deleteById(Long id) {
        checkReference(id);
        // 主表是逻辑删除，子表内容留不留都查不出来了；一起删干净，语义才完整
        proProcessContentService.deleteByProcessId(id);
        return proProcessMapper.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        for (Long id : ids) {
            checkReference(id);
        }
        int rows = proProcessMapper.deleteBatch(ids);
        for (Long id : ids) {
            proProcessContentService.deleteByProcessId(id);
        }
        return rows;
    }

    @Override
    public List<ProProcess> queryAllEnabled() {
        // 复用多条件查询，只过滤启用状态；工艺路线选工序时用
        ProProcess condition = new ProProcess();
        condition.setEnableFlag("Y");
        return proProcessMapper.selectByCondition(condition);
    }

    @Override
    public ProProcess queryByProcessCode(String processCode) {
        return proProcessMapper.selectByProcessCode(processCode);
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     */
    private void checkCodeUnique(ProProcess proProcess) {
        if (proProcess.getProcessCode() == null || "".equals(proProcess.getProcessCode())) {
            throw new BusinessException("工序编码不能为空");
        }
        ProProcess db = proProcessMapper.selectByProcessCode(proProcess.getProcessCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (proProcess.getProcessId() != null && proProcess.getProcessId().equals(db.getProcessId())) {
            return;
        }
        throw new BusinessException("工序编码已存在：" + proProcess.getProcessCode());
    }

    /**
     * 删除前的引用检查（L3）
     *
     * 工序是字典级数据，被工作站（md_workstation）和工艺路线明细（pro_route_process）引用。
     * 直接删会留下悬空引用：工作站详情页显示一个查不到的工序ID。
     * 所以先数引用，被引用就拒绝，并告诉用户去处理哪些地方。
     */
    private void checkReference(Long processId) {
        ProProcess db = proProcessMapper.selectById(processId);
        if (db == null) {
            throw new BusinessException("工序不存在或已被删除");
        }
        int count = proProcessMapper.selectReferenceCount(processId);
        if (count > 0) {
            throw new BusinessException("工序[" + db.getProcessCode() + " " + db.getProcessName()
                    + "]已被工作站或工艺路线引用，不能删除。请先解除引用");
        }
    }
}
