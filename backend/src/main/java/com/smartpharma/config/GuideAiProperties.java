package com.smartpharma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 患者用药指导 AI 配置（OpenAI 兼容接口）
 * 可对接：DeepSeek、智谱、通义、OpenAI、Ollama 等
 */
@ConfigurationProperties(prefix = "guide.ai")
public class GuideAiProperties {

    /** 是否启用 AI（未配置 apiKey 时自动视为不启用） */
    private boolean enabled = true;
    /** API Key，不配置或为空时使用规则兜底 */
    private String apiKey = "";
    /** 兼容 OpenAI 的 base URL，如 https://api.deepseek.com */
    private String baseUrl = "https://api.deepseek.com";
    /** 模型名，如 deepseek-chat、gpt-3.5-turbo、glm-4-flash */
    private String model = "deepseek-chat";
    /** 最大生成 token 数 */
    private int maxTokens = 500;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey != null ? apiKey : "";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl != null ? baseUrl : "";
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model != null ? model : "deepseek-chat";
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    /** 是否实际使用 AI：启用 + baseUrl 已配置 +（本地如 Ollama 或已配置 apiKey） */
    public boolean isActive() {
        if (!enabled || getBaseUrl() == null || getBaseUrl().isBlank()) {
            return false;
        }
        String url = getBaseUrl().toLowerCase();
        boolean isLocal = url.contains("localhost") || url.startsWith("http://127.");
        return isLocal || (!getApiKey().isBlank());
    }
}
