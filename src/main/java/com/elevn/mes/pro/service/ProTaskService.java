package com.elevn.mes.pro.service;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProTaskScheduleDTO;

import java.util.List;
import java.util.Map;

/**
 * 生产任务 Service 接口
 *
 */
public interface ProTaskService {

    /**
     * 分页查询生产任务
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param proTask 查询条件
     * @return 分页结果
     */
    PageInfo<ProTask> page(int pageNum, int pageSize, ProTask proTask);

    /**
     * 按主键查询任务详情
     * @param id 任务ID
     * @return 任务对象
     */
    ProTask queryById(Long id);

    /**
     * 按工单ID查询任务列表（甘特图 / 工单详情页用，不分页）
     * @param workorderId 工单ID
     * @return 任务列表
     */
    List<ProTask> queryByWorkorderId(Long workorderId);

    /**
     * 条件查询任务列表（甘特图数据源，不分页）
     * @param proTask 查询条件
     * @return 任务列表
     */
    List<ProTask> queryList(ProTask proTask);

    /**
     * 排产：把工单按产品制程的工艺路线拆成工序任务
     *
     * 这是本模块最核心的方法，业务规则见实现类的注释。
     *
     * @param dto 排产请求（工单、本次数量、计划开工时间、每道工序的工作站）
     * @return 拆出的任务条数
     */
    int schedule(ProTaskScheduleDTO dto);

    /**
     * 撤销排产：物理删除该工单的全部任务，并回滚工单的已排产数量
     * @param workorderId 工单ID
     * @return 删除的任务条数
     */
    int cancelSchedule(Long workorderId);

    /**
     * 取某工单的排产预览信息（进排产弹窗时用，让用户先看到要拆几道工序、各工序工时）
     * @param workorderId 工单ID
     * @return 预览数据，含工单信息、路线信息、逐道工序及其候选工作站
     */
    Map<String, Object> preview(Long workorderId);

    /**
     * 修改任务（只用于人工微调，如换工作站、改备注、改计划时间）
     * @param proTask 任务对象（taskId 必填）
     * @return 影响行数
     */
    int updateById(ProTask proTask);

    /**
     * 逻辑删除单条任务
     * @param id 任务ID
     * @return 影响行数
     */
    int deleteById(Long id);
}
