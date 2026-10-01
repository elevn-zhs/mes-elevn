package com.elevn.mes.wm.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.wm.entity.WmLocation;
import com.elevn.mes.wm.entity.WmWarehouse;
import com.elevn.mes.wm.mapper.WmLocationMapper;
import com.elevn.mes.wm.mapper.WmWarehouseMapper;
import com.elevn.mes.wm.service.WmLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 库区库位 Service 实现
 *
 * 【一张表两级，靠 location_type 区分】
 *   AREA     库区，父级，parent_id 固定 0
 *   LOCATION 库位，子级，parent_id 指向库区
 *
 * L3 业务校验：
 *   1. 库位必须挂在库区下 —— 不能挂在库位下（那就变成三层了，设计不支持）
 *   2. 库位的仓库跟随父库区 —— 前端传了别的仓库也按父库区算，避免出现
 *      "库位在 A 仓库、它所属库区在 B 仓库"这种账都算不清的数据
 *   3. 编码按类型唯一 —— 唯一键是 (location_type, location_code)
 *   4. 类型与所属库区不可改 —— 改类型整棵树的意义就没了；改父库区会让
 *      库存记录里冗余的 area_id 全部对不上
 *   5. 删除前查引用 —— 有下级库位或还有库存的，一律拒绝
 *
 */
@Service
public class WmLocationServiceImpl implements WmLocationService {

    private static final String DEFAULT_OPERATOR = "admin";

    /** 库区 */
    private static final String TYPE_AREA = "AREA";

    /** 库位 */
    private static final String TYPE_LOCATION = "LOCATION";

    @Autowired
    private WmLocationMapper wmLocationMapper;

    @Autowired
    private WmWarehouseMapper wmWarehouseMapper;

    @Override
    public PageInfo<WmLocation> page(int pageNum, int pageSize, WmLocation wmLocation) {
        PageHelper.startPage(pageNum, pageSize);
        List<WmLocation> list = wmLocationMapper.selectByCondition(wmLocation);
        return new PageInfo<>(list);
    }

    @Override
    public List<WmLocation> queryTree(WmLocation wmLocation) {
        List<WmLocation> all = wmLocationMapper.selectAllForTree(wmLocation);
        // 先收集库区当骨架，再把库位挂上去 —— 两层结构不值得写递归
        Map<Long, WmLocation> areaMap = new LinkedHashMap<>();
        List<WmLocation> locations = new ArrayList<>();
        for (WmLocation node : all) {
            if (TYPE_AREA.equals(node.getLocationType())) {
                areaMap.put(node.getLocationId(), node);
            } else {
                locations.add(node);
            }
        }
        for (WmLocation child : locations) {
            WmLocation parent = areaMap.get(child.getParentId());
            if (parent == null) {
                // 父库区被过滤掉了（停用/已删），这个库位就是孤儿，不展示
                continue;
            }
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(child);
        }
        return new ArrayList<>(areaMap.values());
    }

    @Override
    public WmLocation queryById(Long id) {
        WmLocation wmLocation = wmLocationMapper.selectById(id);
        if (wmLocation != null && TYPE_LOCATION.equals(wmLocation.getLocationType())) {
            // 详情页多带一块：这个库位上现在放着哪些物料、哪些批次、各多少
            wmLocation.setStockList(wmLocationMapper.selectStockByLocationId(id));
        }
        return wmLocation;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(WmLocation wmLocation) {
        if (TYPE_AREA.equals(wmLocation.getLocationType())) {
            // 库区是顶级节点，父级固定 0 —— 前端传啥都忽略，别让脏数据进来
            wmLocation.setParentId(0L);
        } else if (TYPE_LOCATION.equals(wmLocation.getLocationType())) {
            wmLocation.setParentId(checkParentArea(wmLocation));
        } else {
            throw new BusinessException("类型只能是 AREA(库区) 或 LOCATION(库位)");
        }
        checkCodeUnique(wmLocation.getLocationType(), wmLocation.getLocationCode(), null);
        fillWarehouseSnapshot(wmLocation);
        applyDefaults(wmLocation);
        wmLocation.setCreateBy(DEFAULT_OPERATOR);
        wmLocation.setUpdateBy(DEFAULT_OPERATOR);
        wmLocation.setCreateTime(LocalDateTime.now());
        wmLocation.setUpdateTime(LocalDateTime.now());
        return wmLocationMapper.insert(wmLocation);
    }

    @Override
    public int updateById(WmLocation wmLocation) {
        WmLocation db = checkExists(wmLocation.getLocationId());

        // 类型不可改：库区改成库位，它的子库位就全成了孤儿
        if (wmLocation.getLocationType() != null
                && !wmLocation.getLocationType().equals(db.getLocationType())) {
            throw new BusinessException("库区库位类型不允许修改（当前为 " + db.getLocationType()
                    + "）。如需调整请新建一个" + ("AREA".equals(db.getLocationType()) ? "库位" : "库区"));
        }
        // 所属库区不可改：库存记录里冗余了 area_id，库位换库区那些冗余就全错了
        if (wmLocation.getParentId() != null && !wmLocation.getParentId().equals(db.getParentId())) {
            throw new BusinessException("所属库区不允许修改。如需把库位挪到别的库区，请新建库位");
        }
        // 所属仓库不可改：同理，库存记录里冗余了仓库信息
        if (wmLocation.getWarehouseId() != null && !db.getWarehouseId().equals(wmLocation.getWarehouseId())) {
            throw new BusinessException("所属仓库不允许修改。如需调整请新建库区库位");
        }
        // 编码改了要重新查重（用库里的类型，防止前端不传 type 时漏掉校验）
        if (wmLocation.getLocationCode() != null && !wmLocation.getLocationCode().equals(db.getLocationCode())) {
            checkCodeUnique(db.getLocationType(), wmLocation.getLocationCode(), db.getLocationId());
        }

        wmLocation.setUpdateBy(DEFAULT_OPERATOR);
        wmLocation.setUpdateTime(LocalDateTime.now());
        return wmLocationMapper.updateById(wmLocation);
    }

    @Override
    public int deleteById(Long id) {
        checkCanDelete(id);
        return wmLocationMapper.deleteById(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        // 先全部校验通过再删，避免删一半留一半
        for (Long id : ids) {
            checkCanDelete(id);
        }
        return wmLocationMapper.deleteBatch(ids);
    }

    @Override
    public WmLocation queryByCodeAndType(String locationType, String locationCode) {
        return wmLocationMapper.selectByCodeAndType(locationType, locationCode);
    }

    // ==================== 以下为 L3 业务校验 ====================

    /**
     * 校验库位的父节点必须是库区，并返回父库区的ID
     *
     * 顺带做了一件"替前端做主"的事：库位的所属仓库一律跟随父库区。
     * 前端如果传了别的仓库，直接覆盖 —— 因为库存表里 area_id 和 warehouse_id
     * 会被一起冗余下来，两者不一致的话，按仓库和按库区统计出来的数永远对不上。
     */
    private Long checkParentArea(WmLocation wmLocation) {
        Long parentId = wmLocation.getParentId();
        if (parentId == null || parentId == 0L) {
            throw new BusinessException("新增库位必须指定所属库区");
        }
        WmLocation parent = wmLocationMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("所属库区不存在或已被删除");
        }
        if (!TYPE_AREA.equals(parent.getLocationType())) {
            throw new BusinessException("库位只能挂在库区下面，不能挂在另一个库位下面");
        }
        // 仓库跟随父库区
        wmLocation.setWarehouseId(parent.getWarehouseId());
        return parentId;
    }

    /**
     * 校验编码是否重复（唯一键是 类型 + 编码 的组合）
     *
     * 库区可以有叫 A01 的，库位也可以有叫 A01 的，两者不冲突 —— 这是表设计时就定好的口径。
     */
    private void checkCodeUnique(String locationType, String locationCode, Long excludeId) {
        if (locationCode == null || "".equals(locationCode)) {
            throw new BusinessException("库区库位编码不能为空");
        }
        WmLocation db = wmLocationMapper.selectByCodeAndType(locationType, locationCode);
        if (db == null) {
            return;
        }
        if (excludeId != null && excludeId.equals(db.getLocationId())) {
            return;
        }
        throw new BusinessException("该" + ("AREA".equals(locationType) ? "库区" : "库位")
                + "编码已存在：" + locationCode);
    }

    /**
     * 回填所属仓库的快照（编码 / 名称）
     *
     * 不信前端传的仓库编码，现查一次 wm_warehouse —— 仓库改名或前端伪造时，
     * 库位上显示的还是对的信息。
     */
    private void fillWarehouseSnapshot(WmLocation wmLocation) {
        if (wmLocation.getWarehouseId() == null) {
            throw new BusinessException("所属仓库不能为空");
        }
        WmWarehouse warehouse = wmWarehouseMapper.selectById(wmLocation.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("所属仓库不存在或已被删除");
        }
        wmLocation.setWarehouseCode(warehouse.getWarehouseCode());
        wmLocation.setWarehouseName(warehouse.getWarehouseName());
    }

    /**
     * 补默认值，与数据库的 default 保持一致
     */
    private void applyDefaults(WmLocation wmLocation) {
        if (isBlank(wmLocation.getEnableFlag())) {
            wmLocation.setEnableFlag("Y");
        }
        if (isBlank(wmLocation.getFrozenFlag())) {
            wmLocation.setFrozenFlag("N");
        }
        if (isBlank(wmLocation.getProductMixing())) {
            wmLocation.setProductMixing("Y");
        }
        if (isBlank(wmLocation.getBatchMixing())) {
            wmLocation.setBatchMixing("Y");
        }
        if (isBlank(wmLocation.getAreaFlag())) {
            // 库区默认不开启库位管理（本身也不放货）
            wmLocation.setAreaFlag("N");
        }
    }

    /**
     * 删除前的引用检查（L3）
     *
     *   - 库区下面还挂着库位 → 删了库位就找不到自己在哪个库区了
     *   - 库位上还有在库数量 → 删了货就"人间蒸发"，账实不符
     * 两种情况都拒绝，并说清楚先去处理什么。
     */
    private void checkCanDelete(Long locationId) {
        WmLocation db = checkExists(locationId);
        int childCount = wmLocationMapper.countChildren(locationId);
        if (childCount > 0) {
            throw new BusinessException(describe(db) + "下还有 " + childCount
                    + " 个库位，不能删除。请先删除库位");
        }
        int stockCount = wmLocationMapper.countStockByLocationId(locationId);
        if (stockCount > 0) {
            throw new BusinessException(describe(db) + "上还有 " + stockCount
                    + " 项库存，不能删除。请先把库存移走或出库");
        }
    }

    /**
     * 校验是否存在
     */
    private WmLocation checkExists(Long locationId) {
        if (locationId == null) {
            throw new BusinessException("库区库位ID不能为空");
        }
        WmLocation db = wmLocationMapper.selectById(locationId);
        if (db == null) {
            throw new BusinessException("库区库位不存在或已被删除");
        }
        return db;
    }

    /** 拼一个人能看懂的描述，报错信息里直接用 */
    private String describe(WmLocation location) {
        String typeName = TYPE_AREA.equals(location.getLocationType()) ? "库区" : "库位";
        return typeName + "[" + location.getLocationCode() + " " + location.getLocationName() + "]";
    }

    private boolean isBlank(String str) {
        return str == null || "".equals(str);
    }
}
