package com.elevn.mes.pro.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProFeedback;
import com.elevn.mes.pro.entity.ProFeedbackDTO;
import com.elevn.mes.pro.service.ProFeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 生产报工 Controller
 *
 * 路径说明：报工挂在 /api/pro/feedback 下。
 *
 * 报工刻意做成分步接口而不是一个 POST：
 *   1. GET  /preview/{taskId} —— 先让用户看到"这道工序排了多少、已经报了多少、还能报多少"
 *   2. POST /                 —— 用户填完数量再提交，后端做三方数量校验后一次落库
 * 这样界面能先给出上限，而不是让用户盲填一个数字再被打回。
 *
 */
@RestController
@RequestMapping("/api/pro/feedback")
public class ProFeedbackController {

    @Autowired
    private ProFeedbackService proFeedbackService;

    /**
     * 分页 + 多条件查询报工列表
     */
    @GetMapping("/page")
    public Result<PageInfo<ProFeedback>> page(ProFeedback proFeedback,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(proFeedbackService.page(pageNum, pageSize, proFeedback));
    }

    /**
     * 根据ID查询报工详情
     */
    @GetMapping("/{id}")
    public Result<ProFeedback> queryById(@PathVariable Long id) {
        ProFeedback feedback = proFeedbackService.queryById(id);
        if (feedback == null) {
            return Result.error("报工记录不存在或已被删除");
        }
        return Result.success(feedback);
    }

    /**
     * 按任务ID查询报工明细（任务行展开看"报过几次工"）
     */
    @GetMapping("/listByTask/{taskId}")
    public Result<List<ProFeedback>> listByTask(@PathVariable Long taskId) {
        return Result.success(proFeedbackService.queryByTaskId(taskId));
    }

    /**
     * 条件查询报工列表（不分页，用于导出或看板）
     */
    @GetMapping("/list")
    public Result<List<ProFeedback>> list(ProFeedback proFeedback) {
        return Result.success(proFeedbackService.queryList(proFeedback));
    }

    // ============================================================
    // 报工
    // ============================================================

    /**
     * 报工预览：取该任务已报工数量、剩余可报数量与历史报工明细
     *
     * 进报工弹窗时先调这个，用来设置输入框上限与默认值。
     * 注意前端必须用返回的 quantityRemain 当 max 的上界，
     * 且要处理 remain = 0 的情况（否则 el-input-number 的 min > max 会直接报错）。
     */
    @GetMapping("/preview/{taskId}")
    public Result<Map<String, Object>> preview(@PathVariable Long taskId) {
        return Result.success(proFeedbackService.preview(taskId));
    }

    /**
     * 执行报工（报工总线）
     *
     * 一次事务内完成：写报工记录 → 回写任务数量与状态 → 任务报满则置完工
     * → 工单全部任务完工则累加工单已生产数量。
     *
     * @return data 为报工结果摘要（报工编号、任务与工单的最新数量与状态）
     */
    @PostMapping
    public Result<Map<String, Object>> feedback(@RequestBody @Valid ProFeedbackDTO dto) {
        return Result.success(proFeedbackService.feedback(dto));
    }

    /**
     * 冲销报工（红冲）—— 报错了怎么撤
     *
     * 不删行，而是把报工置为 REVERSED 并留痕，同时在一个事务里回退：
     * 任务数量、工单产量与状态、流转卡过站、物料消耗。
     *
     * 拒绝的情形：报工不存在 / 已冲销过 / 下游工序已产出成品 / 账对不上。
     *
     * @param id   报工ID
     * @param body 请求体，取 reason（冲销原因，必填）
     */
    @PostMapping("/{id}/reverse")
    public Result<Map<String, Object>> reverse(@PathVariable Long id,
                                               @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return Result.success(proFeedbackService.reverse(id, reason));
    }

    /**
     * 删除报工 —— 已封禁，统一走冲销
     *
     * 报工提交后必然回写过数量并生成过消耗，直接删会留下对不上账的数据。
     * 保留这个接口只为给出明确提示，实现里一律抛出业务异常引导走「冲销」。
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(proFeedbackService.deleteById(id));
    }
}
