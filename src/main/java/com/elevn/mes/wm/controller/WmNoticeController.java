package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmNotice;
import com.elevn.mes.wm.entity.WmNoticeLine;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import com.elevn.mes.wm.service.WmNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓储通知单 Controller
 *
 * 3 类通知（到货 / 发货 / 备料申请）共用这一套接口，用 noticeType 区分。
 *
 * 【通知单不动库存】
 *   它是预告 + 触发检验 + 下游单据的来源。整个控制器里没有一处会改 wm_material_stock：
 *   真正让库存变的，只有出入库单据的过账（POST /api/wm/doc/execute/{docId}）。
 *
 * 【谁负责填哪些字段由 noticeType 决定】
 *   到货通知 → 必须有供应商；发货通知 → 必须有客户；备料申请 → 必须有生产工单。
 *   后端校验（WmNoticeServiceImpl.checkBizRules），前端跟着显示。
 *
 */
@RestController
@RequestMapping("/api/wm/notice")
public class WmNoticeController {

    @Autowired
    private WmNoticeService wmNoticeService;

    /**
     * 新增通知单（含明细行）
     * 通知单编号由后端按编码规则生成
     */
    @PostMapping
    public Result<WmNotice> save(@RequestBody @Validated(CreateOption.class) WmNotice wmNotice) {
        return wmNoticeService.save(wmNotice) == 1 ? Result.success(wmNotice) : Result.error("保存失败");
    }

    /**
     * 修改通知单（含明细行，整批替换）。只有【待处理】状态能改。
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) WmNotice wmNotice) {
        return wmNoticeService.updateById(wmNotice) == 1 ? Result.success(wmNotice) : Result.error("修改失败");
    }

    /**
     * 根据ID查询通知单详情（含明细行 lineList）
     */
    @GetMapping("/{noticeId}")
    public Result<WmNotice> queryById(@PathVariable Long noticeId) {
        WmNotice wmNotice = wmNoticeService.queryById(noticeId);
        if (wmNotice == null) {
            return Result.error("通知单不存在或已被删除");
        }
        return Result.success(wmNotice);
    }

    /**
     * 分页 + 多条件查询通知单列表
     *
     * 必须带 noticeType —— 3 类通知混在一张表里，不带类型会把所有通知都倒出来。
     */
    @GetMapping("/page")
    public Result<PageInfo<WmNotice>> page(WmNotice wmNotice,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmNoticeService.page(pageNum, pageSize, wmNotice));
    }

    /**
     * 查询通知单的行列表
     */
    @GetMapping("/lines/{noticeId}")
    public Result<List<WmNoticeLine>> queryLines(@PathVariable Long noticeId) {
        return Result.success(wmNoticeService.queryLineList(noticeId));
    }

    /**
     * 【触发检验】通知单唯一的状态推进动作
     *
     * 到货通知 → IQC 来料检验；发货通知 → OQC 出货检验；备料申请不触发检验。
     * 触发后单据状态变为【已触发检验】，同时就不能再改、不能再删了。
     *
     * 注意：C 线（质量管理）的检验单接口目前还没就绪，
     * 服务端会打一条 WARN 日志并把明细标记为待检，等接口好了再接真实调用。
     */
    @PostMapping("/triggerQc/{noticeId}")
    public Result triggerQc(@PathVariable Long noticeId) {
        String qcType = wmNoticeService.triggerQc(noticeId);
        return Result.success("已提交" + qcType + "检验申请（C 线检验单接口待接入，"
                + "当前已把明细行标记为待检并记录日志）");
    }

    /**
     * 删除通知单（逻辑删除 + 删掉行）。只有【待处理】且没报过检的能删。
     */
    @DeleteMapping("/{noticeId}")
    public Result deleteById(@PathVariable Long noticeId) {
        return Result.success(wmNoticeService.deleteById(noticeId));
    }

    /**
     * 批量删除通知单
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] noticeIds) {
        return Result.success(wmNoticeService.deleteBatch(noticeIds));
    }
}
