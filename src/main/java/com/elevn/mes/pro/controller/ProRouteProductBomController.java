package com.elevn.mes.pro.controller;

import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProRouteProductBom;
import com.elevn.mes.pro.service.ProRouteProductBomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 制程物料BOM Controller
 *
 * 只有两个接口：按产品+路线查明细、整批保存。
 * 与路线工序明细同一个思路：BOM 行之间要保证"同工序同物料不重复"，
 * 逐行改没法校验，整表提交整表校验才可靠。
 *
 */
@RestController
@RequestMapping("/api/pro/routeProductBom")
public class ProRouteProductBomController {

    @Autowired
    private ProRouteProductBomService proRouteProductBomService;

    /**
     * 按产品+路线查询全部BOM行
     */
    @GetMapping("/byProductAndRoute")
    public Result<List<ProRouteProductBom>> selectByProductAndRoute(@RequestParam Long productId,
                                                                    @RequestParam Long routeId) {
        return Result.success(proRouteProductBomService.selectByProductAndRoute(productId, routeId));
    }

    /**
     * 整批保存制程BOM
     * productId / routeId 走查询参数，完整BOM列表走请求体
     */
    @PostMapping("/batchSave")
    public Result<Integer> batchSave(@RequestParam Long productId,
                                     @RequestParam Long routeId,
                                     @RequestBody List<ProRouteProductBom> bomList) {
        return Result.success(proRouteProductBomService.batchSave(productId, routeId, bomList));
    }
}
