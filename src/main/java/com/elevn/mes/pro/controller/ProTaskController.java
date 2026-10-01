package com.elevn.mes.pro.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProTask;
import com.elevn.mes.pro.entity.ProTaskScheduleDTO;
import com.elevn.mes.pro.service.ProTaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 生产任务 Controller
 *
 * 路径说明：任务挂在 /api/pro/task 下。
 *
 * 排产刻意做成分步接口而不是一个 PUT：
 *   1. GET  /preview/{workorderId} —— 先让用户看到"要拆几道工序、各工序工时、能选哪些工作站"
 *   2. POST /schedule              —— 用户选完工作站再提交，后端一次性拆出全部任务
 * 这样界面能先给出决策依据，而不是让用户盲填。
 *
 */
@RestController
@RequestMapping("/api/pro/task")
public class ProTaskController {

    @Autowired
    private ProTaskService proTaskService;

    /**
     * 分页 + 多条件查询生产任务列表
     */
    @GetMapping("/page")
    public Result<PageInfo<ProTask>> page(ProTask proTask,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(proTaskService.page(pageNum, pageSize, proTask));
    }

    /**
     * 根据ID查询任务详情
     */
    @GetMapping("/{id}")
    public Result<ProTask> queryById(@PathVariable Long id) {
        ProTask proTask = proTaskService.queryById(id);
        if (proTask == null) {
            return Result.error("生产任务不存在或已被删除");
        }
        return Result.success(proTask);
    }

    /**
     * 按工单ID查询任务列表（工单详情页 Tab / 甘特图）
     */
    @GetMapping("/listByWorkorder/{workorderId}")
    public Result<List<ProTask>> listByWorkorder(@PathVariable Long workorderId) {
        return Result.success(proTaskService.queryByWorkorderId(workorderId));
    }

    /**
     * 条件查询任务列表（甘特图数据源，不分页）
     *
     * 和 /page 的区别：甘特图要一次拿到一个时间区间内的全部任务才能画，
     * 分页会把同一张工单的工序拆到不同页，画出来就断了。
     */
    @GetMapping("/list")
    public Result<List<ProTask>> list(ProTask proTask) {
        return Result.success(proTaskService.queryList(proTask));
    }

    // ============================================================
    // 排产
    // ============================================================

    /**
     * 排产预览：取该工单要拆的工序、各工序工时、每道工序可选的启用工作站
     *
     * 进排产弹窗时先调这个，让用户按实际产能选工作站，而不是盲选。
     */
    @GetMapping("/preview/{workorderId}")
    public Result<Map<String, Object>> preview(@PathVariable Long workorderId) {
        return Result.success(proTaskService.preview(workorderId));
    }

    /**
     * 执行排产：把工单按产品制程的工艺路线拆成工序任务
     *
     * @return data 为拆出的任务条数
     */
    @PostMapping("/schedule")
    public Result<Integer> schedule(@RequestBody @Valid ProTaskScheduleDTO dto) {
        return Result.success(proTaskService.schedule(dto));
    }

    /**
     * 撤销排产：物理删除该工单全部任务，并把工单已排产数量归零
     *
     * 只有"没有任何工序报过工"的工单能撤销 ——
     * 否则报工记录会变成孤儿数据。
     *
     * @return data 为删除的任务条数
     */
    @PostMapping("/cancelSchedule/{workorderId}")
    public Result<Integer> cancelSchedule(@PathVariable Long workorderId) {
        return Result.success(proTaskService.cancelSchedule(workorderId));
    }

    // ============================================================
    // 人工微调
    // ============================================================

    /**
     * 修改任务（换工作站 / 改计划时间 / 改备注；已完工或已取消的不能改）
     */
    @PutMapping
    public Result updateById(@RequestBody ProTask proTask) {
        return proTaskService.updateById(proTask) == 1 ? Result.success(proTask) : Result.error("修改失败");
    }

    /**
     * 删除单条任务（已报工的不允许删）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(proTaskService.deleteById(id));
    }
}
