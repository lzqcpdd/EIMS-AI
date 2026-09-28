package com.eims.module.ai.controller;

import com.eims.common.result.Result;
import com.eims.module.ai.service.AiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 智能数据查询接口
 * 前端传一段中文问题，后端让大模型生成 SQL 并执行，返回结果
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiQueryController {

    private final AiQueryService aiQueryService;

    /**
     * 智能查询接口
     * 请求体示例：{ "question": "上个月采购金额最高的供应商是谁" }
     */
    @PostMapping("/query")
    public Result<Map<String, Object>> query(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Result.fail("问题不能为空");
        }
        try {
            Map<String, Object> result = aiQueryService.ask(question.trim());
            return Result.ok(result);
        } catch (Exception e) {
            log.error("AI 查询失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    // 日志对象（Controller 里也打日志方便调试）
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AiQueryController.class);
}
