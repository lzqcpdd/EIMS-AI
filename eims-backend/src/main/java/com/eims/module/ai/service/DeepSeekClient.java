package com.eims.module.ai.service;

import com.eims.module.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek 大模型 API 客户端
 * 负责把 Prompt 发给大模型，拿到回复
 * DeepSeek 的接口和 OpenAI 完全兼容，所以请求格式照着 OpenAI 来就行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    /**
     * 调用大模型进行对话
     *
     * @param systemPrompt 系统提示词（告诉大模型它是谁、要做什么）
     * @param userPrompt   用户提问（实际问题）
     * @return 大模型返回的文本内容
     */
    public String chat(String systemPrompt, String userPrompt) {
        try {
            // 1. 构造请求体（照着 OpenAI 的 messages 格式）
            Map<String, Object> body = new HashMap<>();
            body.put("model", aiProperties.getModel());       // 模型名：deepseek-chat
            body.put("temperature", aiProperties.getTemperature()); // 温度：0 最稳定

            // 构造 messages（Java 8 不支持 Map.of()，用 HashMap 手动构建）
            // messages 数组：system 角色设定 + user 实际提问
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            body.put("messages", Arrays.asList(systemMsg, userMsg));

            // 2. 构造请求头：带上 API Key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getApiKey());   // Authorization: Bearer sk-xxx

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            log.info("调用 DeepSeek API，用户问题：{}", userPrompt);

            ResponseEntity<String> resp = restTemplate.exchange(
                    aiProperties.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 4. 解析返回结果：choices[0].message.content
            JsonNode root = objectMapper.readTree(resp.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            log.info("DeepSeek 返回：{}", content);
            return content;

        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            throw new RuntimeException("AI 服务调用失败：" + e.getMessage(), e);
        }
    }
}
