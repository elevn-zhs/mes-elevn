package com.elevn.mes.pro.controller;

import com.github.pagehelper.PageInfo;
import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.pro.entity.ProCard;
import com.elevn.mes.pro.entity.ProCardProcess;
import com.elevn.mes.pro.service.ProCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工序流转卡 Controller
 * 路由：/api/pro/card
 *
 * 只暴露查询接口 —— 建卡 / 推进 / 拆卡都由排产与报工的事务自动完成，
 * 不给前端手动干预的口子（流转是系统行为，不是人工操作）。
 *
 */
@RestController
@RequestMapping("/api/pro/card")
public class ProCardController {

    @Autowired
    private ProCardService proCardService;

    /** 分页查询流转卡列表 */
    @GetMapping("/page")
    public Result<PageInfo<ProCard>> page(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) String cardCode,
                                          @RequestParam(required = false) String workorderCode,
                                          @RequestParam(required = false) String itemName,
                                          @RequestParam(required = false) String status) {
        return Result.success(proCardService.page(pageNum, pageSize,
                cardCode, workorderCode, itemName, status));
    }

    /** 流转卡详情（卡 + 全部过站行） */
    @GetMapping("/{cardId}")
    public Result<Map<String, Object>> queryById(@PathVariable Long cardId) {
        ProCard card = proCardService.queryById(cardId);
        if (card == null) {
            return Result.error("流转卡不存在或已被删除");
        }
        List<ProCardProcess> processes = proCardService.queryProcessesByCardId(cardId);
        Map<String, Object> data = new HashMap<>();
        data.put("card", card);
        data.put("processes", processes);
        return Result.success(data);
    }

    /** 按工单查卡（工单详情页要用） */
    @GetMapping("/byWorkorder/{workorderId}")
    public Result<ProCard> queryByWorkorderId(@PathVariable Long workorderId) {
        return Result.success(proCardService.queryByWorkorderId(workorderId));
    }
}
