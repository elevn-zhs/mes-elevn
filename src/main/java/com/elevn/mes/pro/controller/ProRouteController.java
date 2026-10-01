package com.elevn.mes.pro.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProRoute;
import com.elevn.mes.pro.options.CreateOption;
import com.elevn.mes.pro.options.UpdateOption;
import com.elevn.mes.pro.service.ProRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工艺路线 Controller
 *
 * 路线主信息在这里维护；工序明细（子表）走 ProRouteProcessController 的整批保存接口。
 *
 */
@RestController
@RequestMapping("/api/pro/route")
public class ProRouteController {

    @Autowired
    private ProRouteService proRouteService;

    /**
     * 新增工艺路线
     */
    @PostMapping
    public Result<ProRoute> save(@RequestBody @Validated(CreateOption.class) ProRoute proRoute) {
        return proRouteService.save(proRoute) == 1 ? Result.success(proRoute) : Result.error("保存失败");
    }

    /**
     * 修改工艺路线
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) ProRoute proRoute) {
        return proRouteService.updateById(proRoute) == 1 ? Result.success(proRoute) : Result.error("修改失败");
    }

    /**
     * 根据ID查询路线详情
     */
    @GetMapping("/{id}")
    public Result<ProRoute> queryById(@PathVariable Long id) {
        ProRoute proRoute = proRouteService.queryById(id);
        if (proRoute == null) {
            return Result.error("工艺路线不存在或已被删除");
        }
        return Result.success(proRoute);
    }

    /**
     * 分页 + 多条件查询路线列表
     */
    @GetMapping("/page")
    public Result<PageInfo<ProRoute>> page(ProRoute proRoute,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(proRouteService.page(pageNum, pageSize, proRoute));
    }

    /**
     * 查询全部启用的路线（产品制程挂路线的下拉数据源）
     */
    @GetMapping("/all")
    public Result<List<ProRoute>> queryAllEnabled() {
        return Result.success(proRouteService.queryAllEnabled());
    }

    /**
     * 删除路线（逻辑删除；被产品制程引用时后端会拒绝）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(proRouteService.deleteById(id));
    }

    /**
     * 批量删除路线
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(proRouteService.deleteBatch(ids));
    }

    /**
     * 根据编码查询路线，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byRouteCode/{routeCode}")
    public Result<ProRoute> queryByRouteCode(@PathVariable String routeCode) {
        return Result.success(proRouteService.queryByRouteCode(routeCode));
    }
}
