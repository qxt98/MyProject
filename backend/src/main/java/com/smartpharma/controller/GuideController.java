package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.config.GuideLimitProperties;
import com.smartpharma.service.GuideAiService;
import com.smartpharma.service.GuideAskLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 患者用药指导接口（需求 F-Guide-01～F-Guide-04）
 * 未登录用户每 IP 每时间窗口内限 3 次提问；已登录不限。优先 AI，未配置或失败时规则兜底；仅供参考、请遵医嘱。
 */
@RestController
@RequestMapping("/guide")
public class GuideController {

    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final GuideAiService guideAiService;
    private final GuideAskLimitService guideAskLimitService;
    private final GuideLimitProperties limitProperties;

    public GuideController(GuideAiService guideAiService, GuideAskLimitService guideAskLimitService, GuideLimitProperties limitProperties) {
        this.guideAiService = guideAiService;
        this.guideAskLimitService = guideAskLimitService;
        this.limitProperties = limitProperties;
    }

    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String question = body != null ? body.get("question") : null;
        if (question == null || question.trim().isEmpty()) {
            return Result.fail("请输入您的问题或症状描述");
        }
        question = question.trim();
        int maxLen = limitProperties.getQuestionMaxLength();
        if (question.length() > maxLen) {
            return Result.fail("问题长度不能超过 " + maxLen + " 个字符");
        }

        boolean anonymous = !isAuthenticated();
        if (anonymous) {
            String clientIp = guideAskLimitService.getClientIp(request);
            if (!guideAskLimitService.tryRecordAndCheck(clientIp)) {
                return Result.fail(HTTP_TOO_MANY_REQUESTS,
                        "未登录用户每 " + limitProperties.getWindowHours() + " 小时内仅可提问 " + limitProperties.getMaxAsksPerWindow() + " 次，请登录后继续使用或稍后再试。");
            }
        }

        String answer = guideAiService.ask(question);
        if (answer == null || answer.isBlank()) {
            answer = ruleBasedAnswer(question);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("answer", answer);
        data.put("disclaimer", DISCLAIMER);
        if (anonymous) {
            String clientIp = guideAskLimitService.getClientIp(request);
            data.put("remainingAsks", guideAskLimitService.getRemainingAsks(clientIp));
        }
        return Result.ok(data);
    }

    private static final String DISCLAIMER = "以上内容仅供参考，不能替代专业医疗建议，请遵医嘱。";

    /** 流式回答：边生成边推送，避免长时间等待与超时 */
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askStream(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String question = body != null ? body.get("question") : null;
        if (question == null || question.trim().isEmpty()) {
            SseEmitter emitter = new SseEmitter(5000L);
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data("请输入您的问题或症状描述"));
                } catch (IOException ignored) { }
                emitter.complete();
            });
            return emitter;
        }
        question = question.trim();
        int maxLen = limitProperties.getQuestionMaxLength();
        if (question.length() > maxLen) {
            SseEmitter emitter = new SseEmitter(5000L);
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event().name("error").data("问题长度不能超过 " + maxLen + " 个字符"));
                } catch (IOException ignored) { }
                emitter.complete();
            });
            return emitter;
        }
        boolean anonymous = !isAuthenticated();
        if (anonymous) {
            String clientIp = guideAskLimitService.getClientIp(request);
            if (!guideAskLimitService.tryRecordAndCheck(clientIp)) {
                SseEmitter emitter = new SseEmitter(5000L);
                CompletableFuture.runAsync(() -> {
                    try {
                        emitter.send(SseEmitter.event().name("error").data("未登录用户每 " + limitProperties.getWindowHours() + " 小时内仅可提问 " + limitProperties.getMaxAsksPerWindow() + " 次，请登录后继续使用或稍后再试。"));
                    } catch (IOException ignored) { }
                    emitter.complete();
                });
                return emitter;
            }
        }
        final String questionFinal = question;
        final boolean anonymousFinal = anonymous;
        SseEmitter emitter = new SseEmitter(60_000L);
        CompletableFuture.runAsync(() -> {
            try {
                emitter.send(SseEmitter.event().name("start").data("ok", MediaType.TEXT_PLAIN));
            } catch (IOException e) {
                emitter.completeWithError(e);
                return;
            }
            try {
                guideAiService.streamAsk(
                        questionFinal,
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event().name("content").data(chunk, MediaType.TEXT_PLAIN));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("disclaimer").data(DISCLAIMER, MediaType.TEXT_PLAIN));
                                if (anonymousFinal) {
                                    String clientIp = guideAskLimitService.getClientIp(request);
                                    emitter.send(SseEmitter.event().name("remainingAsks").data(String.valueOf(guideAskLimitService.getRemainingAsks(clientIp)), MediaType.TEXT_PLAIN));
                                }
                                emitter.complete();
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        }
                );
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage() != null ? e.getMessage() : "服务异常", MediaType.TEXT_PLAIN));
                } catch (IOException ignored) { }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
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
