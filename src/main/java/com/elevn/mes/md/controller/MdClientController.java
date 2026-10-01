package com.elevn.mes.md.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.md.entity.MdClient;
import com.elevn.mes.md.options.CreateOption;
import com.elevn.mes.md.options.UpdateOption;
import com.elevn.mes.md.service.MdClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 客户 Controller
 *
 * 客户主数据，销售出库的收货方。
 *
 * 【关于 @Validated 的分组】
 * 新增用 CreateOption、修改用 UpdateOption，实体上的约束注解都标了 groups，
 * 这里必须指定分组才会生效 —— 只写裸 @Validated 的话走的是 Default 分组，约束会被全部跳过。
 *
 */
@RestController
@RequestMapping("/api/md/client")
public class MdClientController {

    @Autowired
    private MdClientService mdClientService;

    /**
     * 新增客户
     */
    @PostMapping
    public Result<MdClient> save(@RequestBody @Validated(CreateOption.class) MdClient mdClient) {
        return mdClientService.save(mdClient) == 1 ? Result.success(mdClient) : Result.error("保存失败");
    }

    /**
     * 修改客户
     */
    @PutMapping
    public Result updateById(@RequestBody @Validated(UpdateOption.class) MdClient mdClient) {
        return mdClientService.updateById(mdClient) == 1 ? Result.success(mdClient) : Result.error("修改失败");
    }

    /**
     * 根据ID查询客户详情
     */
    @GetMapping("/{id}")
    public Result<MdClient> queryById(@PathVariable Long id) {
        MdClient mdClient = mdClientService.queryById(id);
        if (mdClient == null) {
            return Result.error("客户不存在或已被删除");
        }
        return Result.success(mdClient);
    }

    /**
     * 分页 + 多条件查询客户列表
     */
    @GetMapping("/page")
    public Result<PageInfo<MdClient>> page(MdClient mdClient,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(mdClientService.page(pageNum, pageSize, mdClient));
    }

    /**
     * 删除客户（逻辑删除，del_flag = '1'，记录还留在库里）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return Result.success(mdClientService.deleteById(id));
    }

    /**
     * 批量删除客户
     */
    @PostMapping("/deleteBatch")
    public Result deleteBatch(@RequestBody Long[] ids) {
        return Result.success(mdClientService.deleteBatch(ids));
    }

    /**
     * 根据编码查询客户，可用于"编码是否已占用"的实时校验
     */
    @GetMapping("/byClientCode/{clientCode}")
    public Result<MdClient> queryByClientCode(@PathVariable String clientCode) {
        return Result.success(mdClientService.queryByClientCode(clientCode));
    }

}
