package com.elevn.mes.sys.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.sys.entity.SysLog;
import com.elevn.mes.sys.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志 Controller
 *
 * 【这个模块只提供查询和删除，不给新增和修改，原因不是偷懒】
 * 日志是系统行为的留痕，天生只能由程序自己写：
 *   - 将来做 AOP 日志切面，在方法执行完之后自动往 sys_log 插一条
 *   - 如果开放了新增和修改接口，那"谁改了日志"本身就成了一个说不清的问题，
 *     审计场景下这份证据就废了
 * 所以这里连 @PostMapping 都不写，从接口层面就把这条路堵死。
 * 真到需要清理历史数据的时候，走 delete 或者干脆由 DBA 定期归档。
 *
 */
@RestController
@RequestMapping("/api/log")
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    @GetMapping("/{id}")
    public Result<SysLog> queryById(@PathVariable Long id){
        SysLog sysLog = sysLogService.queryById(id);
        return sysLog == null ? Result.error("日志不存在或已被删除") : Result.success(sysLog);
    }

    /**
     * 分页查询日志
     * 常用筛选：logType（操作类型）/ status（成功失败）/ operName（操作人）/ operTime 区间
     * 注意 operTime 的区间查询目前 Mapper 里还没写，需要的话得到 SysLogMapper.xml 里补 <if> 条件
     */
    @GetMapping("/page")
    public Result<PageInfo<SysLog>> page(SysLog sysLog,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(sysLogService.page(pageNum, pageSize, sysLog));
    }

    /**
     * 删除单条日志（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        return Result.success(sysLogService.deleteById(id));
    }
}
