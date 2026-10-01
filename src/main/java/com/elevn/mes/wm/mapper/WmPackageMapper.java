package com.elevn.mes.wm.mapper;

import com.elevn.mes.wm.entity.WmPackage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 装箱单 Mapper
 *
 */
public interface WmPackageMapper {

    /** 多条件分页查询（只查顶层树或按条件平铺，树形展示由前端用 parentId 组装） */
    List<WmPackage> selectByCondition(WmPackage query);

    /** 按主键查询 */
    WmPackage selectById(@Param("packageId") Long packageId);

    /** 编号是否已存在（排除自己，编辑时用） */
    int countByCode(@Param("packageCode") String packageCode,
                    @Param("excludeId") Long excludeId);

    /** 新增 */
    int insert(WmPackage wmPackage);

    /** 修改（不允许改编号/父级/祖链，状态由专门的语句推进） */
    int updateById(WmPackage wmPackage);

    /** 推进状态（where 带旧状态，防并发重复推进；返回 0 说明状态已被别人动过） */
    int updateStatus(@Param("packageId") Long packageId,
                     @Param("fromStatus") String fromStatus,
                     @Param("toStatus") String toStatus);

    /** 回填条码信息（条码模块生成后调用） */
    int updateBarcode(@Param("packageId") Long packageId,
                      @Param("barcodeId") Long barcodeId,
                      @Param("barcodeContent") String barcodeContent,
                      @Param("barcodeUrl") String barcodeUrl);

    /** 逻辑删除（含整棵子树一起删，调用前必须先校验没子箱） */
    int deleteById(@Param("packageId") Long packageId);

    /** 数某个箱下面还有没有未删除的子箱 */
    int countChild(@Param("parentId") Long parentId);

    /** 查一棵子树的全部箱（ancestors 前缀匹配，删除整树前预览用） */
    List<WmPackage> selectSubTree(@Param("packageId") Long packageId,
                                  @Param("ancestorsPrefix") String ancestorsPrefix);
}
