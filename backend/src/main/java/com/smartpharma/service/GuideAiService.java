package com.smartpharma.service;

import java.util.function.Consumer;

/**
 * 患者用药指导 AI 服务（OpenAI 兼容接口）
 * 未配置或调用失败时返回 null，由调用方使用规则兜底。
 */
public interface GuideAiService {

    /**
     * 根据用户问题生成用药建议（一次性返回）
     */
    String ask(String question);

    /**
     * 流式生成用药建议：每收到一段内容调用 onChunk，结束时调用 onDone。
     * 规则兜底时会将整段内容作为一次 onChunk 再 onDone。
     */
    void streamAsk(String question, Consumer<String> onChunk, Runnable onDone);
}
