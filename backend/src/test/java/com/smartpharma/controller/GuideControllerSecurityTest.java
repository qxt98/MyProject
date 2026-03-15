package com.smartpharma.controller;

import com.smartpharma.config.GuideLimitProperties;
import com.smartpharma.service.GuideAiService;
import com.smartpharma.service.GuideAskLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用药指导接口权限单元测试（患者/访客可匿名 POST /guide/ask；未登录限流由 GuideAskLimitService 控制）
 */
@WebMvcTest(controllers = GuideController.class)
@Import(com.smartpharma.config.SecurityConfig.class)
class GuideControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.smartpharma.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private GuideAiService guideAiService;

    @MockBean
    private GuideAskLimitService guideAskLimitService;

    @MockBean
    private GuideLimitProperties guideLimitProperties;

    @BeforeEach
    void setUp() {
        when(guideAskLimitService.getClientIp(any())).thenReturn("127.0.0.1");
        when(guideAskLimitService.tryRecordAndCheck(anyString())).thenReturn(true);
        when(guideAskLimitService.getRemainingAsks(anyString())).thenReturn(2);
        when(guideLimitProperties.getQuestionMaxLength()).thenReturn(500);
        when(guideLimitProperties.getWindowHours()).thenReturn(1);
        when(guideLimitProperties.getMaxAsksPerWindow()).thenReturn(3);
    }

    @Test
    void ask_withoutAuth_returns200() throws Exception {
        when(guideAiService.ask(anyString())).thenReturn(null);
        mockMvc.perform(post("/guide/ask")
                        .contentType("application/json")
                        .content("{\"question\":\"感冒怎么办\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.remainingAsks").value(2));
    }

    @Test
    void ask_withoutAuth_whenLimitExceeded_returns429() throws Exception {
        when(guideAskLimitService.tryRecordAndCheck(anyString())).thenReturn(false);
        mockMvc.perform(post("/guide/ask")
                        .contentType("application/json")
                        .content("{\"question\":\"感冒怎么办\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").exists());
    }
}
