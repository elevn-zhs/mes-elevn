package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.wm.entity.WmWarehouse;
import com.elevn.mes.wm.mapper.WmWarehouseMapper;
import com.elevn.mes.wm.service.WmWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓库 Service 实现
 *
 * 仓储模块的第一个页面，也是整条仓储链路的根：仓库 → 库区 → 库位 → 库存 → 单据。
 *
 * L3 业务校验：
 *   1. 编码唯一 —— 仓库编码是全厂对账的通用语言，重复了库位和库存就对不上号
 *   2. 编码不可改 —— 编码冗余写在 wm_location / wm_material_stock / wm_doc 里，
 *      放开了改就得批量刷这些冗余列，漏一处账就错。要做就直接拦下并说明原因
 *   3. 删除前查引用 —— 下挂库位或还有库存的仓库删掉，
 *      会让库位和库存变成指向空气的孤儿数据
 *
 */
@Service
public class WmWarehouseServiceImpl implements WmWarehouseService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private WmWarehouseMapper wmWarehouseMapper;

    @Override
    public PageInfo<WmWarehouse> page(int pageNum, int pageSize, WmWarehouse wmWarehouse) {
        PageHelper.startPage(pageNum, pageSize);
        List<WmWarehouse> list = wmWarehouseMapper.selectByCondition(wmWarehouse);
        return new PageInfo<>(list);
    }

    @Override
    public WmWarehouse queryById(Long id) {
        // 详情页带统计：库区数 / 库位数 / 库存总量 / 库存物料种数
        return wmWarehouseMapper.selectDetailById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(WmWarehouse wmWarehouse) {
        checkCodeUnique(wmWarehouse);
        wmWarehouse.setCreateBy(DEFAULT_OPERATOR);
        wmWarehouse.setUpdateBy(DEFAULT_OPERATOR);
        wmWarehouse.setCreateTime(LocalDateTime.now());
        wmWarehouse.setUpdateTime(LocalDateTime.now());
        if (wmWarehouse.getEnableFlag() == null || "".equals(wmWarehouse.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            wmWarehouse.setEnableFlag("Y");
        }
        return wmWarehouseMapper.insert(wmWarehouse);
    }

    @Override
    public int updateById(WmWarehouse wmWarehouse) {
        WmWarehouse db = checkExists(wmWarehouse.getWarehouseId());
        // 编码只要传了、且和库里不一致，直接拒绝 —— 不允许静默忽略，用户得知道为什么改不动
        checkCodeNotChanged(db, wmWarehouse.getWarehouseCode());
        wmWarehouse.setUpdateBy(DEFAULT_OPERATOR);
        wmWarehouse.setUpdateTime(LocalDateTime.now());
        return wmWarehouseMapper.updateById(wmWarehouse);
    }

    @Override
    public int deleteById(Long id) {
        checkCanDelete(id);
        return wmWarehouseMapper.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        // 先全部校验通过再删；只要有一条不合格就整批不动，避免删一半留一半
        for (Long id : ids) {
            checkCanDelete(id);
        }
        return wmWarehouseMapper.deleteBatch(ids);
    }

    @Override
    public List<WmWarehouse> queryAllEnabled() {
        return wmWarehouseMapper.selectAllEnabled();
    }

    @Override
    public WmWarehouse queryByWarehouseCode(String warehouseCode) {
        return wmWarehouseMapper.selectByWarehouseCode(warehouseCode);
    }

    // ==================== 以下为 L3 业务校验 ====================

    /**
     * 校验仓库编码是否重复
     *
     * 【为什么不在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     */
    private void checkCodeUnique(WmWarehouse wmWarehouse) {
        if (wmWarehouse.getWarehouseCode() == null || "".equals(wmWarehouse.getWarehouseCode())) {
            throw new BusinessException("仓库编码不能为空");
        }
        WmWarehouse db = wmWarehouseMapper.selectByWarehouseCode(wmWarehouse.getWarehouseCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (wmWarehouse.getWarehouseId() != null && wmWarehouse.getWarehouseId().equals(db.getWarehouseId())) {
            return;
        }
        throw new BusinessException("仓库编码已存在：" + wmWarehouse.getWarehouseCode());
    }

    /**
     * 校验编码有没有被改动（L3 规则拒绝）
     *
     * 不传编码 = 用户没打算改，放过；
     * 传了并且和库里不一样 = 明确在改编码，拒绝并说明原因，别让用户以为"保存成功了"。
     */
    private void checkCodeNotChanged(WmWarehouse db, String newCode) {
        if (newCode == null || "".equals(newCode)) {
            return;
        }
        if (!newCode.equals(db.getWarehouseCode())) {
            throw new BusinessException("仓库编码不允许修改（当前为 " + db.getWarehouseCode()
                    + "）。编码已冗余到库位、库存与单据中，如需更换请新建仓库");
        }
    }

    /**
     * 删除前的引用检查（L3）
     *
     * 仓库是仓储的根节点，下面挂着库区库位和库存：
     *   - 有库区/库位 → 删了库位就成了找不到仓库的孤儿
     *   - 还有在库数量 → 删了库存记录就查不到货在哪了
     * 两种情况都拒绝，并告诉用户先处理什么。
     */
    private void checkCanDelete(Long warehouseId) {
        WmWarehouse db = checkExists(warehouseId);
        int locationCount = wmWarehouseMapper.countLocationByWarehouseId(warehouseId);
        if (locationCount > 0) {
            throw new BusinessException("仓库[" + db.getWarehouseName() + "]下还有 "
                    + locationCount + " 个库区/库位，不能删除。请先删除库区库位");
        }
        int stockCount = wmWarehouseMapper.countStockByWarehouseId(warehouseId);
        if (stockCount > 0) {
            throw new BusinessException("仓库[" + db.getWarehouseName() + "]下还有 "
                    + stockCount + " 项库存，不能删除。请先将库存清零或调拨出去");
        }
    }

    /**
     * 校验仓库是否存在
     */
    private WmWarehouse checkExists(Long warehouseId) {
        if (warehouseId == null) {
            throw new BusinessException("仓库ID不能为空");
        }
        WmWarehouse db = wmWarehouseMapper.selectById(warehouseId);
        if (db == null) {
            throw new BusinessException("仓库不存在或已被删除");
        }
        return db;
    }
}
