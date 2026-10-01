package com.elevn.mes.common.controller;

import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    // 注入chatClient
    @Autowired
    private ChatClient chatClient;

    /**
     * 非流式回答接口
     * @param message 用户的问题
     * @param conversationId 会话的ID
     * @return
     */
    @GetMapping("/chat")
    public Result<String> chat(String message,String conversationId){
        // 检查客户端是否携带了会话的ID
        if(conversationId == null || "".equals(conversationId)){
            // 如果客户端没有conversationId，说明是第一次请求
            // 创建一个conversationId返回
            conversationId = UUID.randomUUID().toString().replace("-","");
        }
        final String conId = conversationId;
        String content = chatClient.prompt(message)
                .advisors(
                        advisorSpec-> advisorSpec.param(ChatMemory.CONVERSATION_ID,conId)
                ).call().content();
        Result result = Result.success();
        result.setMsg(conId); // mes中存储的是会话ID
        result.setData(content);// data存储的是相应的内容；
        return result;
    }

    // 使用Stream的方式，我们将会话ID的处理和存储交给客户端
    @GetMapping("/stream")
    public Flux<String> stream(String message, String conversationId, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        if(conversationId == null || "".equals(conversationId)){
            throw new BusinessException("错误信息：stream请求方式中必须携带会话ID;");
        }
        return chatClient.prompt(message)
                .advisors(
                        advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)
                ).stream().content();
    }
}
