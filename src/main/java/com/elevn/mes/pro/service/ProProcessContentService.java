package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProProcessContent;

import java.util.List;

/**
 * 工序内容 Service 接口
 *
 */
public interface ProProcessContentService {

    /**
     * 按工序ID查询全部内容（详情页 Tab 用）
     * @param processId 工序ID
     * @return 内容列表
     */
    List<ProProcessContent> selectByProcessId(Long processId);

    /**
     * 新增工序内容（校验所属工序存在）
     * @param proProcessContent 工序内容对象
     * @return 影响行数
     */
    int save(ProProcessContent proProcessContent);

    /**
     * 修改工序内容
     * @param proProcessContent 工序内容对象
     * @return 影响行数
     */
    int updateById(ProProcessContent proProcessContent);

    /**
     * 删除工序内容
     * @param id 内容ID
     * @return 影响行数
     */
    int deleteById(Long id);

    /**
     * 批量删除工序内容
     * @param ids 主键数组
     * @return 影响行数
     */
    int deleteBatch(Long[] ids);

    /**
     * 按工序ID删除全部内容（主表删除时的子表联动清理）
     * @param processId 工序ID
     * @return 影响行数
     */
    int deleteByProcessId(Long processId);
}
