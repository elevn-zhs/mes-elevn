package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdClient;
import com.elevn.mes.md.mapper.MdClientMapper;
import com.elevn.mes.sys.service.SysCodingRuleService;
import com.elevn.mes.wm.entity.WmMaterialStock;
import com.elevn.mes.wm.entity.WmPackage;
import com.elevn.mes.wm.entity.WmPackageLine;
import com.elevn.mes.wm.mapper.WmMaterialStockMapper;
import com.elevn.mes.wm.mapper.WmPackageLineMapper;
import com.elevn.mes.wm.mapper.WmPackageMapper;
import com.elevn.mes.wm.service.WmPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 装箱单 Service 实现
 *
 * 【和通知单一样的原则：不碰 wm_material_stock】
 *   装箱是"给货套包装"，不是"把货拿走"。货真正离开库位走出库单过账。
 *   明细行上的仓库/库位信息是快照，记录装箱那一刻货在哪。
 *
 * 【祖链(ancestors)的维护时机】
 *   只在 create 时算一次：ancestors = 父箱.ancestors + 父箱ID + ","。
 *   顶层箱 ancestors 固定写 "0,"（表默认值 '0'，插入时统一成带逗号的格式）。
 *   编辑不允许换父箱 —— 换父箱要重刷整棵子树的祖链，收益配不上复杂度，
 *   课上讲清这个取舍：真实系统里"挪箱"也通常是拆了重装。
 *
 */
@Service
public class WmPackageServiceImpl implements WmPackageService {

    @Autowired
    private WmPackageMapper packageMapper;

    @Autowired
    private WmPackageLineMapper packageLineMapper;

    @Autowired
    private WmMaterialStockMapper materialStockMapper;

    @Autowired
    private MdClientMapper clientMapper;

    @Autowired
    private SysCodingRuleService codingRuleService;

    private static final String ST_PREPARE = "PREPARE";
    private static final String ST_PACKED = "PACKED";
    /** 顶层箱的祖链根 */
    private static final String ROOT_ANCESTORS = "0,";

    @Override
    public PageInfo<WmPackage> page(int pageNum, int pageSize, WmPackage query) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(packageMapper.selectByCondition(query));
    }

    @Override
    public WmPackage getById(Long packageId) {
        WmPackage pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw new BusinessException("装箱单不存在或已被删除");
        }
        pkg.setLineList(packageLineMapper.selectByPackageId(packageId));
        pkg.setChildren(packageMapper.selectByCondition(
                childQuery(packageId)).stream().toList());
        return pkg;
    }

    /** 组一个"查某箱直接子箱"的查询条件（selectByCondition 按 parentId 过滤） */
    private WmPackage childQuery(Long parentId) {
        WmPackage q = new WmPackage();
        q.setParentId(parentId);
        return q;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmPackage create(WmPackage wmPackage) {
        // ---------- 1. 箱套箱：父箱校验 + 祖链维护 ----------
        Long parentId = wmPackage.getParentId() == null ? 0L : wmPackage.getParentId();
        wmPackage.setParentId(parentId);
        if (parentId != 0L) {
            WmPackage parent = packageMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException("父箱不存在或已被删除，不能往里套");
            }
            if (ST_PACKED.equals(parent.getStatus())) {
                throw new BusinessException("父箱「" + parent.getPackageCode() + "」已完成装箱，不能往里加子箱");
            }
            // 祖链 = 父箱的祖链 + 父箱自己。结尾带逗号，子树查询靠 like '前缀%' 走
            wmPackage.setAncestors(parent.getAncestors() + parent.getPackageId() + ",");
        } else {
            wmPackage.setAncestors(ROOT_ANCESTORS);
        }

        // ---------- 2. 取号 ----------
        wmPackage.setPackageCode(codingRuleService.autoCode("WM_PACKAGE"));

        // ---------- 3. 客户快照：给了 ID 就现查回填，不信前端传的名字 ----------
        fillClientSnapshot(wmPackage);

        if (wmPackage.getPackageDate() == null) {
            throw new BusinessException("装箱日期不能为空");
        }
        wmPackage.setStatus(ST_PREPARE);
        packageMapper.insert(wmPackage);
        return wmPackage;
    }

    /** 客户三件套冗余字段：现查 md_client 回填（跟工单 fillClientSnapshot 同一套路） */
    private void fillClientSnapshot(WmPackage wmPackage) {
        if (wmPackage.getClientId() == null) {
            return;
        }
        MdClient client = clientMapper.selectById(wmPackage.getClientId());
        if (client == null) {
            throw new BusinessException("客户不存在，ID=" + wmPackage.getClientId());
        }
        wmPackage.setClientCode(client.getClientCode());
        wmPackage.setClientName(client.getClientName());
        wmPackage.setClientNick(client.getClientNick());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmPackage wmPackage) {
        WmPackage db = packageMapper.selectById(wmPackage.getPackageId());
        if (db == null) {
            throw new BusinessException("装箱单不存在或已被删除");
        }
        if (!ST_PREPARE.equals(db.getStatus())) {
            throw new BusinessException("箱子已完成装箱，不能再改箱信息");
        }
        fillClientSnapshot(wmPackage);
        // 编号/父级/祖链/状态 都不在 updateById 的更新列里，想改也改不了
        int rows = packageMapper.updateById(wmPackage);
        if (rows == 0) {
            throw new BusinessException("箱信息保存失败：可能刚被别人完成装箱或删除，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long packageId) {
        WmPackage pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw new BusinessException("装箱单不存在或已被删除");
        }
        int rows = packageMapper.updateStatus(packageId, ST_PREPARE, ST_PACKED);
        if (rows == 0) {
            throw new BusinessException("完成装箱失败：箱子可能刚被别人处理过，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long packageId) {
        WmPackage pkg = packageMapper.selectById(packageId);
        if (pkg == null) {
            throw new BusinessException("装箱单不存在或已被删除");
        }
        // ---------- 1. 已完成的箱不能删：它已经是对外流转的凭证（可能贴了码发了货） ----------
        if (ST_PACKED.equals(pkg.getStatus())) {
            throw new BusinessException("箱子「" + pkg.getPackageCode() + "」已完成装箱，不能删除");
        }
        // ---------- 2. 有子箱不能删（顶层箱删整树是故意提供的口径，但子箱未完成才让删；
        //             这里简化为：有子箱就走整树删除，子树里有已完成的一律拦下） ----------
        List<WmPackage> subTree = packageMapper.selectSubTree(
                packageId, pkg.getAncestors() + packageId + ",");
        for (WmPackage node : subTree) {
            if (ST_PACKED.equals(node.getStatus())) {
                throw new BusinessException("子箱「" + node.getPackageCode() + "」已完成装箱，"
                        + "要先处理它才能删父箱「" + pkg.getPackageCode() + "」");
            }
        }
        // ---------- 3. 整树删除：先删明细行，再逻辑删箱 ----------
        for (WmPackage node : subTree) {
            packageLineMapper.deleteByPackageId(node.getPackageId());
            packageMapper.deleteById(node.getPackageId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmPackageLine addLine(WmPackageLine line) {
        // ---------- 1. 箱必须是"装箱中" ----------
        WmPackage pkg = packageMapper.selectById(line.getPackageId());
        if (pkg == null) {
            throw new BusinessException("装箱单不存在或已被删除");
        }
        if (!ST_PREPARE.equals(pkg.getStatus())) {
            throw new BusinessException("箱子已完成装箱，明细行已锁定，不能再加货");
        }

        // ---------- 2. 快照从库存行现查回填，前端只给 materialStockId + quantity ----------
        WmMaterialStock stock = materialStockMapper.selectById(line.getMaterialStockId());
        if (stock == null) {
            throw new BusinessException("来源库存行不存在，请重新在库存里选");
        }
        if (stock.getQuantityOnhand() == null
                || stock.getQuantityOnhand().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("该库存行现存数量为 0，没有货可装");
        }

        // ---------- 3. 装箱数量守恒：累计装箱（含本行）不得超过库存现存 ----------
        BigDecimal alreadyPacked = packageLineMapper.sumQuantityByStockId(line.getMaterialStockId());
        BigDecimal thisQty = line.getQuantity();
        if (thisQty == null || thisQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("装箱数量必须大于 0");
        }
        if (alreadyPacked.add(thisQty).compareTo(stock.getQuantityOnhand()) > 0) {
            throw new BusinessException("装箱数量超了：库存行现存 "
                    + stock.getQuantityOnhand().stripTrailingZeros().toPlainString()
                    + "，已装 " + alreadyPacked.stripTrailingZeros().toPlainString()
                    + "，本次最多还能装 "
                    + stock.getQuantityOnhand().subtract(alreadyPacked).stripTrailingZeros().toPlainString());
        }

        // ---------- 4. 回填快照并落库 ----------
        line.setItemId(stock.getItemId());
        line.setItemCode(stock.getItemCode());
        line.setItemName(stock.getItemName());
        line.setSpecification(stock.getSpecification());
        line.setUnitOfMeasure(stock.getUnitOfMeasure());
        line.setWorkorderId(stock.getWorkorderId());
        line.setWorkorderCode(stock.getWorkorderCode());
        line.setBatchCode(stock.getBatchCode());
        line.setWarehouseId(stock.getWarehouseId());
        line.setWarehouseCode(stock.getWarehouseCode());
        line.setWarehouseName(stock.getWarehouseName());
        line.setLocationId(stock.getLocationId());
        line.setLocationCode(stock.getLocationCode());
        line.setLocationName(stock.getLocationName());
        line.setAreaId(stock.getAreaId());
        line.setAreaCode(stock.getAreaCode());
        line.setAreaName(stock.getAreaName());
        line.setExpireDate(stock.getExpireDate());
        packageLineMapper.insert(line);
        return line;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLine(WmPackageLine line) {
        WmPackageLine target = findLine(line.getLineId());
        WmPackage pkg = packageMapper.selectById(target.getPackageId());
        if (pkg == null || !ST_PREPARE.equals(pkg.getStatus())) {
            throw new BusinessException("箱子已完成装箱，明细行已锁定，不能修改");
        }
        if (line.getQuantity() == null || line.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("装箱数量必须大于 0");
        }
        // 数量守恒：改大后累计装箱不能超现存
        WmMaterialStock stock = materialStockMapper.selectById(target.getMaterialStockId());
        BigDecimal already = packageLineMapper.sumQuantityByStockId(target.getMaterialStockId());
        if (stock != null && already.subtract(target.getQuantity()).add(line.getQuantity())
                .compareTo(stock.getQuantityOnhand()) > 0) {
            throw new BusinessException("装箱数量超了：库存行现存 "
                    + stock.getQuantityOnhand().stripTrailingZeros().toPlainString()
                    + "，去掉本行后已装 "
                    + already.subtract(target.getQuantity()).stripTrailingZeros().toPlainString());
        }
        packageLineMapper.updateById(line);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLine(Long lineId) {
        WmPackageLine target = findLine(lineId);
        WmPackage pkg = packageMapper.selectById(target.getPackageId());
        if (pkg == null || !ST_PREPARE.equals(pkg.getStatus())) {
            throw new BusinessException("箱子已完成装箱，明细行已锁定，不能删除");
        }
        packageLineMapper.deleteById(lineId);
    }

    /** 按主键查明细行 */
    private WmPackageLine findLine(Long lineId) {
        if (lineId == null) {
            throw new BusinessException("明细行ID不能为空");
        }
        WmPackageLine hit = packageLineMapper.selectById(lineId);
        if (hit == null) {
            throw new BusinessException("明细行不存在或已被删除");
        }
        return hit;
    }
}
