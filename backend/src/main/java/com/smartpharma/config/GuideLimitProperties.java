package com.smartpharma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 患者用药指导限流与安全配置（未登录用户）
 */
@ConfigurationProperties(prefix = "guide.limit")
public class GuideLimitProperties {

    /** 未登录用户每时间窗口内允许的提问次数 */
    private int maxAsksPerWindow = 3;
    /** 时间窗口（小时），滑动窗口 */
    private int windowHours = 1;
    /** 问题内容最大长度（字符） */
    private int questionMaxLength = 500;

    public int getMaxAsksPerWindow() {
        return maxAsksPerWindow;
    }

    public void setMaxAsksPerWindow(int maxAsksPerWindow) {
        this.maxAsksPerWindow = maxAsksPerWindow;
    }

    public int getWindowHours() {
        return windowHours;
    }

    public void setWindowHours(int windowHours) {
        this.windowHours = windowHours;
    }

    public int getQuestionMaxLength() {
        return questionMaxLength;
    }

    public void setQuestionMaxLength(int questionMaxLength) {
        this.questionMaxLength = questionMaxLength;
    }
}
