package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProSnProcess;
import com.elevn.mes.wm.entity.WmSn;
import com.elevn.mes.wm.service.WmSnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SN Controller
 * 路由 /api/wm/sn（任务表要求的 GET /wm/sn/page 等）
 *
 */
@RestController
@RequestMapping("/api/wm/sn")
public class WmSnController {

    @Autowired
    private WmSnService snService;

    /** 分页查询（按产品/批次/状态/工单筛选） */
    @GetMapping("/page")
    public Result<PageInfo<WmSn>> page(WmSn query,
                                       @RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(snService.page(pageNum, pageSize, query));
    }

    /** SN 详情 */
    @GetMapping("/{snId}")
    public Result<WmSn> getById(@PathVariable Long snId) {
        return Result.success(snService.getById(snId));
    }

    /**
     * 批量生成 SN 并绑定
     * body: batchCode(必填) + itemId(必填) + workorderId(选填) + count(必填)
     */
    @PostMapping("/generate")
    public Result<List<WmSn>> generate(@RequestBody WmSn body) {
        return Result.success(snService.generate(body.getBatchCode(), body.getItemId(),
                body.getWorkorderId(), body.getCount(), "admin"));
    }

    /** 重新绑定批次/工单（仅"在库"） */
    @PutMapping("/bind")
    public Result<Void> updateBind(@RequestBody WmSn sn) {
        snService.updateBind(sn);
        return Result.success();
    }

    /** 改状态（发货 / 冻结 / 解冻） */
    @PutMapping("/status/{snId}")
    public Result<Void> updateStatus(@PathVariable Long snId,
                                     @RequestParam String status) {
        snService.updateStatus(snId, status);
        return Result.success();
    }

    /** 删除（仅"在库"） */
    @DeleteMapping("/{snId}")
    public Result<Void> delete(@PathVariable Long snId) {
        snService.delete(snId);
        return Result.success();
    }

    /**
     * 单件全流程追溯：过站记录按工序顺序排列
     * （A 线报工写入 pro_sn_process，当前可能为空 —— 页面提示"暂无过站记录"）
     */
    @GetMapping("/{snId}/trace")
    public Result<List<ProSnProcess>> trace(@PathVariable Long snId) {
        return Result.success(snService.trace(snId));
    }
}
