package com.elevn.mes.pro.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProWorkorder;
import com.elevn.mes.pro.entity.ProWorkorderBom;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import com.elevn.mes.pro.service.ProWorkorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 生产工单 Controller
 *
 * 路径说明：工单是生产模块的主单据，路由挂在 /api/pro/workorder 下。
 *
 * 状态流转用四个独立的动词接口（confirm/finish/cancel），
 * 而不是让前端拼一个 PUT 改 status —— 因为每次流转都有不同的前置校验和时间字段要写，
 * 独立接口能让后端的业务规则收得更紧。
 *
 */
@RestController
@RequestMapping("/api/pro/workorder")
public class ProWorkorderController {

    @Autowired
    private ProWorkorderService proWorkorderService;

    /**
     * 新增生产工单（状态固定为 PREPARE 待下达）
     */
    @PostMapping
    public Result<ProWorkorder> save(@RequestBody @Validated(CreateOption.class) ProWorkorder proWorkorder) {
        return proWorkorderService.save(proWorkorder) == 1 ? Result.success(proWorkorder) : Result.error("保存失败");
    }

    /**
     * 修改生产工单（只有待下达状态可改）
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) ProWorkorder proWorkorder) {
        return proWorkorderService.updateById(proWorkorder) == 1 ? Result.success(proWorkorder) : Result.error("修改失败");
    }

    /**
     * 根据ID查询工单
     */
    @GetMapping("/{id}")
    public Result<ProWorkorder> queryById(@PathVariable Long id) {
        ProWorkorder proWorkorder = proWorkorderService.queryById(id);
        if (proWorkorder == null) {
            return Result.error("生产工单不存在或已被删除");
        }
        return Result.success(proWorkorder);
    }

    /**
     * 分页 + 多条件查询工单列表
     */
    @GetMapping("/page")
    public Result<PageInfo<ProWorkorder>> page(ProWorkorder proWorkorder,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(proWorkorderService.page(pageNum, pageSize, proWorkorder));
    }

    /**
     * 删除工单（只有待下达可删）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(proWorkorderService.deleteById(id));
    }

    /**
     * 批量删除工单
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(proWorkorderService.deleteBatch(ids));
    }

    /**
     * 根据编码查询，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byWorkorderCode/{workorderCode}")
    public Result<ProWorkorder> queryByWorkorderCode(@PathVariable String workorderCode) {
        return Result.success(proWorkorderService.queryByWorkorderCode(workorderCode));
    }

    // ============================================================
    // 状态流转
    // ============================================================

    /**
     * 下达工单：PREPARE → CONFIRMED，同时展开工单用料
     * （有制程按制程BOM 展开，没挂制程才退回产品BOM）
     * @return data 为展开出的 BOM 行数
     */
    @PostMapping("/confirm/{id}")
    public Result<Integer> confirm(@PathVariable("id") Long id) {
        return Result.success(proWorkorderService.confirm(id));
    }

    /**
     * 完工：CONFIRMED → FINISHED
     */
    @PostMapping("/finish/{id}")
    public Result finish(@PathVariable("id") Long id) {
        return proWorkorderService.finish(id) == 1 ? Result.success("已完工") : Result.error("操作失败");
    }

    /**
     * 取消：PREPARE / CONFIRMED → CANCELED
     */
    @PostMapping("/cancel/{id}")
    public Result cancel(@PathVariable("id") Long id) {
        return proWorkorderService.cancel(id) == 1 ? Result.success("已取消") : Result.error("操作失败");
    }

    // ============================================================
    // 工单用料（BOM）
    // ============================================================

    /**
     * 查询工单的用料明细（详情页 Tab）
     */
    @GetMapping("/bomList/{workorderId}")
    public Result<List<ProWorkorderBom>> queryBomList(@PathVariable Long workorderId) {
        return Result.success(proWorkorderService.queryBomList(workorderId));
    }

    /**
     * 重新展开工单用料（制程BOM/产品BOM 调整后手工同步；已完工/已取消不能重算）
     */
    @PostMapping("/rebuildBom/{id}")
    public Result<Integer> rebuildBom(@PathVariable("id") Long id) {
        return Result.success(proWorkorderService.rebuildBom(id));
    }
}
