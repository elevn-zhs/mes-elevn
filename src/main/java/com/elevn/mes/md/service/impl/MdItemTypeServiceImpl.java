package com.elevn.mes.md.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.elevn.mes.exception.BusinessException;
import com.elevn.mes.md.entity.MdItemType;
import com.elevn.mes.md.mapper.MdItemTypeMapper;
import com.elevn.mes.md.service.MdItemTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料产品分类 Service 实现
 *
 */
@Service
public class MdItemTypeServiceImpl implements MdItemTypeService {

    /**
     * 登录模块还没做，先写死占位；后续改成从 ThreadLocal / SecurityContext 取当前登录人
     */
    private static final String DEFAULT_OPERATOR = "admin";

    @Autowired
    private MdItemTypeMapper mdItemTypeMapper;

    @Override
    public List<MdItemType> queryTree(String type) {
        return queryByParentId(0L,type);
    }

    private List<MdItemType> queryByParentId(Long parentTypeId,String type){
        List<MdItemType> types = mdItemTypeMapper.selectByParentTypeIdAndType(parentTypeId,type);
        if(types != null && types.size() > 0){
            for (MdItemType typeEntry : types){
                typeEntry.setChildren(queryByParentId(typeEntry.getItemTypeId(),type));
            }
        }
        return types;
    };

    @Override
    public PageInfo<MdItemType> page(int pageNum, int pageSize, MdItemType mdItemType) {
        PageHelper.startPage(pageNum, pageSize);
        List<MdItemType> list = mdItemTypeMapper.selectByCondition(mdItemType);
        return new PageInfo<>(list);
    }

    @Override
    public MdItemType queryById(Long id) {
        return mdItemTypeMapper.selectById(id);
    }

    @Override
    public int deleteById(Long id) {
        return mdItemTypeMapper.deleteById(id);
    }

    @Override
    public int deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new BusinessException("请选择要删除的数据");
        }
        return mdItemTypeMapper.deleteBatch(ids);
    }

    @Override
    public int updateById(MdItemType mdItemType) {
        if (mdItemType.getItemTypeCode() != null && !"".equals(mdItemType.getItemTypeCode())) {
            checkCodeUnique(mdItemType);
        }
        mdItemType.setUpdateBy(DEFAULT_OPERATOR);
        mdItemType.setUpdateTime(LocalDateTime.now());
        return mdItemTypeMapper.updateById(mdItemType);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int save(MdItemType mdItemType) {
        checkCodeUnique(mdItemType);
        mdItemType.setCreateBy(DEFAULT_OPERATOR);
        mdItemType.setUpdateBy(DEFAULT_OPERATOR);
        mdItemType.setCreateTime(LocalDateTime.now());
        mdItemType.setUpdateTime(LocalDateTime.now());
        if (mdItemType.getEnableFlag() == null || "".equals(mdItemType.getEnableFlag())) {
            // 与数据库默认值保持一致：不传就按"启用"处理
            mdItemType.setEnableFlag("Y");
        }
        return mdItemTypeMapper.insert(mdItemType);
    }

    @Override
    public MdItemType queryByItemTypeCode(String itemTypeCode) {
        return mdItemTypeMapper.selectByItemTypeCode(itemTypeCode);
    }

    @Override
    public List<MdItemType> queryByParentId(Long parentId) {
        List<MdItemType> types = mdItemTypeMapper.selectByParentId(parentId);
        // 分别查询，判断每一个类别是否有子节点
        for (MdItemType type : types){
            if(mdItemTypeMapper.selectCountByParentId(type.getItemTypeId()) > 0){
                type.setHasChildren(true);
            }else{
                type.setHasChildren(false);
            }
        }
        return types;
    }

    /**
     * 校验编码是否重复
     *
     * 【为什么不能在数据库加唯一索引了事】
     * 加了索引确实能挡住重复，但抛出来的是 SQLException，页面只会显示"服务器内部错误"，
     * 用户不知道自己错在哪。所以在 service 层先查一遍，用 BusinessException 抛出人话。
     * 两个都要做：索引兜底保数据，业务校验保体验。
     */
    private void checkCodeUnique(MdItemType mdItemType) {
        if (mdItemType.getItemTypeCode() == null || "".equals(mdItemType.getItemTypeCode())) {
            throw new BusinessException("物料产品分类编码不能为空");
        }
        MdItemType db = mdItemTypeMapper.selectByItemTypeCode(mdItemType.getItemTypeCode());
        if (db == null) {
            return;
        }
        // 修改场景：查出来的是自己，不算重复
        if (mdItemType.getItemTypeId() != null && mdItemType.getItemTypeId().equals(db.getItemTypeId())) {
            return;
        }
        throw new BusinessException("物料产品分类编码已存在：" + mdItemType.getItemTypeCode());
    }
}
