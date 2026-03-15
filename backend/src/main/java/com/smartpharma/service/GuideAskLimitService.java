package com.smartpharma.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用药指导匿名用户提问限流：按 IP 在时间窗口内限制次数。
 */
public interface GuideAskLimitService {

    String getClientIp(HttpServletRequest request);

    /** 匿名用户尝试记录一次提问并检查是否超限；允许返回 true 并已记录，超限返回 false。 */
    boolean tryRecordAndCheck(String clientIp);

    /** 当前 IP 在本窗口内剩余可提问次数（不记录）。 */
    int getRemainingAsks(String clientIp);
}
