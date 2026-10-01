package com.elevn.mes.wm.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.wm.entity.WmDoc;
import com.elevn.mes.wm.entity.WmDocDetail;
import com.elevn.mes.wm.entity.WmDocLine;
import com.elevn.mes.wm.options.CreateOption;
import com.elevn.mes.wm.options.UpdateOption;
import com.elevn.mes.wm.service.WmDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出入库单据 Controller
 *
 * 对应 wm_doc / wm_doc_line / wm_doc_detail。
 * 14 类单据（采购入库、生产领料、销售出库、调拨……）共用这一套接口，
 * 用 docType 区分 —— 新增一类单据只要加字典和编码规则，接口不用动。
 *
 * 【本模块只有一个接口会改动库存：POST /api/wm/doc/execute/{docId}（过账）】
 *   其余接口都只动单据本身。前端上那个"过账"按钮，
 *   按下去就等于"这批货真的动了"，页面文案要说清楚。
 *
 * 【为什么没有"修改状态"的接口】
 *   状态是业务动作的结果，不是可以让前端随手改的字段：
 *     PREPARE --过账--> CONFIRMED --送达确认--> FINISHED
 *     PREPARE --取消--> CANCELED
 *   每个动作都有各自的校验（比如已过账不能取消、取消必须填原因），
 *   所以按动作拆成 execute / cancel / finish 三个接口。
 *
 */
@RestController
@RequestMapping("/api/wm/doc")
public class WmDocController {

    @Autowired
    private WmDocService wmDocService;

    /**
     * 新增单据（含明细行）
     *
     * 单据编号由后端按 sys_coding_rules 里的规则生成，前端不用传 docCode；
     * io_flag 也不传 —— 它由 doc_type 推导，前端传了也会被覆盖。
     */
    @PostMapping
    public Result<WmDoc> save(@RequestBody @Validated(CreateOption.class) WmDoc wmDoc) {
        return wmDocService.save(wmDoc) == 1 ? Result.success(wmDoc) : Result.error("保存失败");
    }

    /**
     * 修改单据（含明细行，整批替换）。只有【待过账】状态能改。
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) WmDoc wmDoc) {
        return wmDocService.updateById(wmDoc) == 1 ? Result.success(wmDoc) : Result.error("修改失败");
    }

    /**
     * 根据ID查询单据详情（一次带出行列表 lineList 与落位明细 detailList）
     */
    @GetMapping("/{docId}")
    public Result<WmDoc> queryById(@PathVariable Long docId) {
        WmDoc wmDoc = wmDocService.queryById(docId);
        if (wmDoc == null) {
            return Result.error("单据不存在或已被删除");
        }
        return Result.success(wmDoc);
    }

    /**
     * 分页 + 多条件查询单据列表
     *
     * 【必须带 docType】14 类单据混在一张表里，不带类型会把所有单据都倒出来。
     * 前端每个单据页面固定传自己的 docType。
     */
    @GetMapping("/page")
    public Result<PageInfo<WmDoc>> page(WmDoc wmDoc,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmDocService.page(pageNum, pageSize, wmDoc));
    }

    /**
     * 查询单据的行列表
     */
    @GetMapping("/lines/{docId}")
    public Result<List<WmDocLine>> queryLines(@PathVariable Long docId) {
        return Result.success(wmDocService.queryLineList(docId));
    }

    /**
     * 查询单据的落位明细（只有过账后才有数据）
     */
    @GetMapping("/details/{docId}")
    public Result<List<WmDocDetail>> queryDetails(@PathVariable Long docId) {
        return Result.success(wmDocService.queryDetailList(docId));
    }

    // ====================================================================
    // 单据行的单行维护
    //
    // 新增/编辑整单（POST/PUT /api/wm/doc）时行是随表头一起提交的，
    // 这里的四个接口是"表头已经存在、再单独加一行/改一行"的场景。
    // 两种方式都遵守同一条规矩：只有【待过账】的单据能改行。
    // ====================================================================

    /**
     * 单行新增（行号自动接着往下编）
     */
    @PostMapping("/line")
    public Result<WmDocLine> saveLine(@RequestBody @Validated(CreateOption.class) WmDocLine wmDocLine) {
        return wmDocService.saveLine(wmDocLine) == 1 ? Result.success(wmDocLine) : Result.error("保存失败");
    }

    /**
     * 按单据ID分页查询明细行
     */
    @GetMapping("/line/page/{docId}")
    public Result<PageInfo<WmDocLine>> pageLine(@PathVariable Long docId,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(wmDocService.pageLine(pageNum, pageSize, docId));
    }

    /**
     * 单行修改
     */
    @PutMapping("/line")
    public Result updateLine(@RequestBody @Validated(UpdateOption.class) WmDocLine wmDocLine) {
        return wmDocService.updateLine(wmDocLine) == 1 ? Result.success(wmDocLine) : Result.error("修改失败");
    }

    /**
     * 单行删除（逻辑删除；单据至少要保留一行）
     */
    @DeleteMapping("/line/{lineId}")
    public Result deleteLine(@PathVariable Long lineId) {
        return Result.success(wmDocService.deleteLine(lineId));
    }

    /**
     * 【过账】唯一会改动库存的接口
     *
     * 过账 = 这批货真的动了。服务端在一个事务里做四件事：
     *   写落位明细 → 改库存 → 记流水 → 生产领料单回写投料。
     * 库存不足、单据已被别人过账，都会在这里被拒绝并整体回滚。
     */
    @PostMapping("/execute/{docId}")
    public Result execute(@PathVariable Long docId) {
        int rows = wmDocService.execute(docId);
        return rows == 1 ? Result.success("过账成功，库存已更新") : Result.error("过账失败");
    }

    /**
     * 取消单据（待过账 → 已取消）。已过账的单据不能取消，只能开反向单红冲。
     *
     * @param docId 单据ID
     * @param reason 取消原因（必填，留痕）
     */
    @PostMapping("/cancel/{docId}")
    public Result cancel(@PathVariable Long docId, @RequestParam String reason) {
        int rows = wmDocService.cancel(docId, reason);
        return rows == 1 ? Result.success("单据已取消") : Result.error("取消失败");
    }

    /**
     * 调拨送达确认（已过账 → 已完成），只有调拨单需要
     */
    @PostMapping("/finish/{docId}")
    public Result finish(@PathVariable Long docId) {
        int rows = wmDocService.finish(docId);
        return rows == 1 ? Result.success("送达确认完成") : Result.error("确认失败");
    }

    /**
     * 删除单据（逻辑删除，同时删掉明细行）。只有【待过账】状态能删。
     */
    @DeleteMapping("/{docId}")
    public Result deleteById(@PathVariable Long docId) {
        return Result.success(wmDocService.deleteById(docId));
    }

    /**
     * 批量删除单据（只有全部处于待过账状态才会执行）
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] docIds) {
        return Result.success(wmDocService.deleteBatch(docIds));
    }
}
