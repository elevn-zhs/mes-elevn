package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProProcess;

import java.util.List;

/**
 * 工序 Service 接口
 *
 */
public interface ProProcessService {

    /**
     * 分页 + 多条件查询工序
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param proProcess 查询条件
     * @return 分页结果
     */
    PageInfo<ProProcess> page(int pageNum, int pageSize, ProProcess proProcess);

    /**
     * 根据ID查询工序详情（含工序内容列表）
     * @param id 工序ID
     * @return 工序对象
     */
    ProProcess queryById(Long id);

    /**
     * 新增工序
     * @param proProcess 工序对象
     * @return 影响行数
     */
    int save(ProProcess proProcess);

    /**
     * 修改工序
     * @param proProcess 工序对象
     * @return 影响行数
     */
    int updateById(ProProcess proProcess);

    /**
     * 删除工序（被工作站/工艺路线引用时拒绝删除）
     * @param id 工序ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 批量删除工序
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(Long[] ids);

    /**
     * 查询全部启用的工序（下拉选择用）
     * @return 工序列表
     */
    List<ProProcess> queryAllEnabled();

    /**
     * 根据工序编码查询（编码占用实时校验用）
     * @param processCode 工序编码
     * @return 工序对象，不存在返回 null
     */
    ProProcess queryByProcessCode(String processCode);
}
