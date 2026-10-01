package com.elevn.mes.pro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.pro.entity.ProRoute;
import com.elevn.mes.pro.mapper.ProRouteMapper;
import com.elevn.mes.pro.mapper.ProRouteProcessMapper;
import com.elevn.mes.pro.service.ProRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工艺路线 Service 实现
 *
 * L3 业务校验：
 *   1. 编码唯一
 *   2. 删除引用检查 —— 路线被产品制程（pro_route_product）挂接时不允许删除，
 *      否则产品制程的路线ID就是悬空引用
 *   3. 删除路线时联动清理工序明细
 *
 */
@Service
public class ProRouteServiceImpl implements ProRouteService {

    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private ProRouteMapper proRouteMapper;

    @Autowired
    private ProRouteProcessMapper proRouteProcessMapper;

    @Override
    public PageInfo<ProRoute> page(int pageNum, int pageSize, ProRoute proRoute) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProRoute> list = proRouteMapper.selectByCondition(proRoute);
        return new PageInfo<>(list);
    }

    @Override
    public ProRoute queryById(Long id) {
        // 明细由前端 Tab 单独拉取（selectByRouteId），这里只回主表信息
        return proRouteMapper.selectById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(ProRoute proRoute) {
        checkCodeUnique(proRoute);
        proRoute.setCreateBy(DEFAULT_OPERATOR);
        proRoute.setUpdateBy(DEFAULT_OPERATOR);
        proRoute.setCreateTime(LocalDateTime.now());
        proRoute.setUpdateTime(LocalDateTime.now());
        if (proRoute.getEnableFlag() == null || "".equals(proRoute.getEnableFlag())) {
            proRoute.setEnableFlag("Y");
        }
        return proRouteMapper.insert(proRoute);
    }

    @Override
    public int updateById(ProRoute proRoute) {
        if (proRoute.getRouteCode() != null && !"".equals(proRoute.getRouteCode())) {
            checkCodeUnique(proRoute);
        }
        proRoute.setUpdateBy(DEFAULT_OPERATOR);
        proRoute.setUpdateTime(LocalDateTime.now());
        return proRouteMapper.updateById(proRoute);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteById(Long id) {
        checkReference(id);
        // 主表删除时联动清理明细，避免留下查不出来的幽灵工序行
        proRouteProcessMapper.deleteByRouteId(id);
        return proRouteMapper.deleteById(id);
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
        int rows = proRouteMapper.deleteBatch(ids);
        for (Long id : ids) {
            proRouteProcessMapper.deleteByRouteId(id);
        }
        return rows;
    }

    @Override
    public List<ProRoute> queryAllEnabled() {
        return proRouteMapper.selectAllEnabled();
    }

    @Override
    public ProRoute queryByRouteCode(String routeCode) {
        return proRouteMapper.selectByRouteCode(routeCode);
    }

    /**
     * 校验编码是否重复
     */
    private void checkCodeUnique(ProRoute proRoute) {
        if (proRoute.getRouteCode() == null || "".equals(proRoute.getRouteCode())) {
            throw new BusinessException("路线编码不能为空");
        }
        ProRoute db = proRouteMapper.selectByRouteCode(proRoute.getRouteCode());
        if (db == null) {
            return;
        }
        if (proRoute.getRouteId() != null && proRoute.getRouteId().equals(db.getRouteId())) {
            return;
        }
        throw new BusinessException("路线编码已存在：" + proRoute.getRouteCode());
    }

    /**
     * 删除前的引用检查（L3）
     * 路线被产品制程挂接后，删了会让产品制程指向一个不存在的路线
     */
    private void checkReference(Long routeId) {
        ProRoute db = proRouteMapper.selectById(routeId);
        if (db == null) {
            throw new BusinessException("工艺路线不存在或已被删除");
        }
        int count = proRouteMapper.selectReferenceCount(routeId);
        if (count > 0) {
            throw new BusinessException("工艺路线[" + db.getRouteCode() + " " + db.getRouteName()
                    + "]已被产品制程引用，不能删除。请先在产品制程中解除挂接");
        }
    }
}
