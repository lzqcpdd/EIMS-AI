package com.eims.module.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模块配置类
 * 对应 application.yml / application-dev.yml 中 eims.ai 下的配置项
 * 通过 @ConfigurationProperties 自动绑定配置文件的值
 */
@Data
@Component
@ConfigurationProperties(prefix = "eims.ai")
public class AiProperties {

    /**
     * DeepSeek API 密钥（从环境变量 DEEPSEEK_API_KEY 读取，不要硬编码到代码里）
     * 申请地址：https://platform.deepseek.com/
     */
    private String apiKey;

    /**
     * DeepSeek API 接口地址（OpenAI 兼容格式）
     */
    private String apiUrl = "https://api.deepseek.com/v1/chat/completions";

    /**
     * 使用的模型名称
     * deepseek-chat 是通用对话模型，便宜且效果好
     */
    private String model = "deepseek-chat";

    /**
     * 生成 SQL 时的温度参数（0~2）
     * 0 表示最稳定、最确定，适合生成 SQL（不希望大模型自由发挥）
     */
    private double temperature = 0.0;
}
