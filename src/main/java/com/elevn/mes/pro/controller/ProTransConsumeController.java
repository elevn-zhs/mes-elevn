package com.elevn.mes.pro.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProMaterialRequire;
import com.elevn.mes.pro.entity.ProTransConsume;
import com.elevn.mes.pro.service.ProTransConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料消耗 Controller
 * 路由：/api/pro/consume
 *
 * 和流转卡一样，写入侧不给前端口子 —— 消耗记录是报工倒冲自动生成的，
 * 前端只能查（列表 / 按任务 / 按工单汇总 / 工序用料需求），
 * 只有"误录单条删除"是人工动作。
 *
 */
@RestController
@RequestMapping("/api/pro/consume")
public class ProTransConsumeController {

    @Autowired
    private ProTransConsumeService proTransConsumeService;

    /** 分页查询消耗记录 */
    @GetMapping("/page")
    public Result<PageInfo<ProTransConsume>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long workorderId,
            @RequestParam(required = false) Long processId,
            @RequestParam(required = false) String itemCode,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String workorderCode,
            @RequestParam(required = false) String taskCode,
            @RequestParam(required = false) String transOrderCode,
            @RequestParam(required = false) String batchCode,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime consumeDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime consumeDateTo) {
        ProTransConsume condition = new ProTransConsume();
        condition.setTaskId(taskId);
        condition.setWorkorderId(workorderId);
        condition.setProcessId(processId);
        condition.setItemCode(itemCode);
        condition.setItemName(itemName);
        condition.setWorkorderCode(workorderCode);
        condition.setTaskCode(taskCode);
        condition.setTransOrderCode(transOrderCode);
        condition.setBatchCode(batchCode);
        condition.setConsumeDateFrom(consumeDateFrom);
        condition.setConsumeDateTo(consumeDateTo);
        return Result.success(proTransConsumeService.page(pageNum, pageSize, condition));
    }

    /** 详情 */
    @GetMapping("/{recordId}")
    public Result<ProTransConsume> queryById(@PathVariable Long recordId) {
        return Result.success(proTransConsumeService.queryById(recordId));
    }

    /** 按任务查消耗明细（任务页展开用） */
    @GetMapping("/listByTask/{taskId}")
    public Result<List<ProTransConsume>> listByTask(@PathVariable Long taskId) {
        return Result.success(proTransConsumeService.queryByTaskId(taskId));
    }

    /** 按工单查消耗明细（工单详情"用料明细"用） */
    @GetMapping("/listByWorkorder/{workorderId}")
    public Result<List<ProTransConsume>> listByWorkorder(@PathVariable Long workorderId) {
        return Result.success(proTransConsumeService.queryByWorkorderId(workorderId));
    }

    /** 按工单汇总各物料消耗（工单详情"用料汇总"用） */
    @GetMapping("/summaryByWorkorder/{workorderId}")
    public Result<List<ProTransConsume>> summaryByWorkorder(@PathVariable Long workorderId) {
        return Result.success(proTransConsumeService.summaryByWorkorder(workorderId));
    }

    /**
     * 工序用料需求：这道工序该用哪些料、应耗多少、已耗多少
     * 报工弹窗打开时调这个，让报工人先看清"这一批要吃掉什么料"
     */
    @GetMapping("/materialRequire/{taskId}")
    public Result<List<ProMaterialRequire>> materialRequire(@PathVariable Long taskId) {
        return Result.success(proTransConsumeService.queryMaterialRequire(taskId));
    }

    /**
     * 工单用料比对：工单BOM 预计使用量（应耗） vs 实际累计消耗（实耗）
     * 「投入产出比对」的工单级视图，超耗的料会带 overFlag=true
     */
    @GetMapping("/compareByWorkorder/{workorderId}")
    public Result<List<ProMaterialRequire>> compareByWorkorder(@PathVariable Long workorderId) {
        return Result.success(proTransConsumeService.compareByWorkorder(workorderId));
    }

    /** 删除单条误录的消耗记录（逻辑删除） */
    @DeleteMapping("/{recordId}")
    public Result<Integer> delete(@PathVariable Long recordId) {
        return Result.success(proTransConsumeService.deleteById(recordId));
    }
}
