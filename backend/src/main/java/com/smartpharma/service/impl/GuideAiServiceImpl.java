package com.smartpharma.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpharma.config.GuideAiProperties;
import com.smartpharma.service.GuideAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 基于 OpenAI 兼容接口的用药指导 AI 实现
 * 支持 DeepSeek、智谱、通义、OpenAI、Ollama 等
 */
@Service
public class GuideAiServiceImpl implements GuideAiService {

    private static final Logger log = LoggerFactory.getLogger(GuideAiServiceImpl.class);
    private static final String SYSTEM_PROMPT = "你是一名用药指导助手。根据用户描述的症状或用药问题，给出简明、安全的用药与生活建议。"
            + "要求：1）建议遵医嘱、勿自行加减剂量；2）提醒过敏史需告知医生；3）明确说明本回复仅供参考，不能替代面对面诊疗；4）回答简洁，控制在 200 字以内。";

    private final GuideAiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GuideAiServiceImpl(GuideAiProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String ask(String question) {
        if (!properties.isActive()) {
            return null;
        }
        String url = properties.getBaseUrl().replaceAll("/$", "") + "/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!properties.getApiKey().isBlank()) {
            headers.setBearerAuth(properties.getApiKey().trim());
        }

        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "max_tokens", properties.getMaxTokens(),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", question)
                )
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<ChatResponseDto> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ChatResponseDto.class
            );
            if (resp.getBody() != null && resp.getBody().getContent() != null) {
                return resp.getBody().getContent().trim();
            }
        } catch (Exception e) {
            log.warn("用药指导 AI 调用失败，将使用规则兜底: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void streamAsk(String question, Consumer<String> onChunk, Runnable onDone) {
        try {
            if (!properties.isActive()) {
                String answer = ruleBasedAnswer(question);
                if (answer != null && !answer.isEmpty()) onChunk.accept(answer);
                onDone.run();
                return;
            }
            String url = properties.getBaseUrl().replaceAll("/$", "") + "/v1/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!properties.getApiKey().isBlank()) {
                headers.setBearerAuth(properties.getApiKey().trim());
            }
            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "max_tokens", properties.getMaxTokens(),
                    "stream", true,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", question)
                    )
            );
            byte[] bodyBytes = objectMapper.writeValueAsBytes(body);
            restTemplate.execute(
                    url,
                    HttpMethod.POST,
                    req -> {
                        req.getHeaders().addAll(headers);
                        req.getBody().write(bodyBytes);
                    },
                    response -> {
                        try (var reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (!line.startsWith("data: ")) continue;
                                String data = line.substring(6).trim();
                                if ("[DONE]".equals(data)) break;
                                if (data.isEmpty()) continue;
                                try {
                                    JsonNode node = objectMapper.readTree(data);
                                    JsonNode choices = node.get("choices");
                                    if (choices != null && choices.isArray() && choices.size() > 0) {
                                        JsonNode delta = choices.get(0).get("delta");
                                        if (delta != null && delta.has("content")) {
                                            String content = delta.get("content").asText("");
                                            if (!content.isEmpty()) onChunk.accept(content);
                                        }
                                    }
                                } catch (Exception ignored) { }
                            }
                        }
                        onDone.run();
                        return null;
                    }
            );
        } catch (Exception e) {
            log.warn("用药指导 AI 流式调用失败，将使用规则兜底: {}", e.getMessage());
            String answer = ruleBasedAnswer(question);
            if (answer != null && !answer.isEmpty()) onChunk.accept(answer);
            onDone.run();
        }
    }

    private String ruleBasedAnswer(String question) {
        if (question.contains("感冒") || question.contains("发烧")) {
            return "感冒发热时请注意休息、多饮水。退热可选用对乙酰氨基酚或布洛芬（遵说明书与医嘱）。若高热不退或症状加重请及时就医。本回复仅供参考。";
        }
        if (question.contains("过敏")) {
            return "如怀疑药物过敏，请立即停用该药并就医。就诊时请告知医生过敏药物名称。本回复仅供参考。";
        }
        return "根据您的描述，建议您：1. 遵医嘱用药，勿自行加减剂量；2. 如有过敏史请告知医生；3. 本回复仅供参考，不能替代面对面诊疗。如有不适请及时就医。";
    }
}
