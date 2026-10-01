package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItem;
import com.elevn.mes.md.entity.MdUnitMeasure;
import com.elevn.mes.md.mapper.MdItemMapper;
import com.elevn.mes.md.mapper.MdUnitMeasureMapper;
import com.elevn.mes.md.service.MdItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料产品 Service 实现
 *
 */
@Service
public class MdItemServiceImpl implements MdItemService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdItemMapper mdItemMapper;

    @Autowired
    private MdUnitMeasureMapper unitMeasureMapper;

    @Override
    public PageInfo<MdItem> page(int pageNum, int pageSize, MdItem mdItem) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdItem> list = mdItemMapper.selectByCondition(mdItem);
        return new PageInfo<>(list);
    }

    @Override
    public MdItem queryById(Long id) {
        return mdItemMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdItemMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdItemMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdItem mdItem) {
        if (mdItem.getItemCode() != null && !"".equals(mdItem.getItemCode())) {
            checkCodeUnique(mdItem);
        }
        mdItem.setUpdateBy(DEFAULT_OPERATOR);
        mdItem.setUpdateTime(LocalDateTime.now());
        return mdItemMapper.updateById(mdItem);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdItem mdItem) {
        checkCodeUnique(mdItem);
        MdUnitMeasure mdUnitMeasure = unitMeasureMapper.selectByMeasureCode(mdItem.getUnitOfMeasure());
        mdItem.setUnitName(mdUnitMeasure.getMeasureName());
        mdItem.setCreateBy(DEFAULT_OPERATOR);
        mdItem.setUpdateBy(DEFAULT_OPERATOR);
        mdItem.setCreateTime(LocalDateTime.now());
        mdItem.setUpdateTime(LocalDateTime.now());
        if (mdItem.getEnableFlag() == null || "".equals(mdItem.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdItem.setEnableFlag("Y");
        }
        return mdItemMapper.insert(mdItem);
    }

    @Override
    public MdItem queryByItemCode(String itemCode) {
        return mdItemMapper.selectByItemCode(itemCode);
    }
    @Override
    public PageInfo<MdItem> pageForBom(int pageNum, int pageSize, MdItem mdItem, Long excludeItemId) {
        // excludeItemId 是这个接口的核心参数，缺了它就没法算排除范围，
        // 与其默默返回全量数据让前端选错，不如直接报错（Controller 里用的是 required=false，
        // 就是为了让参数缺失走到这里抛 BusinessException，而不是被 Spring 拦成 400 参数错误）
        if (excludeItemId == null) {
            throw new BusinessException("当前物料ID不能为空：无法确定需要排除的物料");
        }
        if (mdItemMapper.selectById(excludeItemId) == null) {
            throw new BusinessException("当前物料不存在或已被删除，物料ID：" + excludeItemId);
        }
        // 先算出排除集合（自身 + 祖先 + 子孙），结果一定至少包含 excludeItemId 自身
        List<Long> excludeIds = mdItemMapper.selectBomExcludeIds(excludeItemId);
        PageHelper.startPage(pageNum, pageSize);
        List<MdItem> list = mdItemMapper.selectForBom(mdItem, excludeIds);
        return new PageInfo<>(list);
    }

    @Override
    public List<MdItem> queryByName(String itemName) {
        return mdItemMapper.selectByName(itemName);
    }

    @Override
    public List<MdItem> queryByCategoryId(Long catId) {
        return mdItemMapper.selectByTypeId(catId);
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     * 两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkCodeUnique(MdItem mdItem) {
        if (mdItem.getItemCode() == null || "".equals(mdItem.getItemCode())) {
            throw new BusinessException("物料产品编码不能为空");
        }
        MdItem db = mdItemMapper.selectByItemCode(mdItem.getItemCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (mdItem.getItemId() != null && mdItem.getItemId().equals(db.getItemId())) {
            return;
        }
        throw new BusinessException("物料产品编码已存在：" + mdItem.getItemCode());
    }

}
