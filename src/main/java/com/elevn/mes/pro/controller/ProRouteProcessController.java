package com.elevn.mes.pro.controller;

import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProRouteProcess;
import com.elevn.mes.pro.service.ProRouteProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工艺路线工序明细 Controller
 *
 * 只有两个接口：按路线查明细、整批保存。
 * 没有单独的增/改/删 —— 明细行之间有顺序和链条约束，逐行改会把链改断，
 * 统一走「前端整表编辑 → 一次提交 → 后端校验整链 → 整批替换」。
 *
 */
@RestController
@RequestMapping("/api/pro/routeProcess")
public class ProRouteProcessController {

    @Autowired
    private ProRouteProcessService proRouteProcessService;

    /**
     * 按路线ID查询全部明细（按 order_num 升序）
     */
    @GetMapping("/byRouteId/{routeId}")
    public Result<List<ProRouteProcess>> selectByRouteId(@PathVariable Long routeId) {
        return Result.success(proRouteProcessService.selectByRouteId(routeId));
    }

    /**
     * 整批保存路线工序明细（校验整条链后旧删新插）
     * 前端把明细表编辑完，把完整列表放进请求体，routeId 走查询参数
     */
    @PostMapping("/batchSave")
    public Result<Integer> batchSave(@RequestParam Long routeId, @RequestBody List<ProRouteProcess> processList) {
        return Result.success(proRouteProcessService.batchSave(routeId, processList));
    }
}
