package com.smartpharma.service.impl;

import com.smartpharma.config.GuideLimitProperties;
import com.smartpharma.service.GuideAskLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按 IP 滑动窗口限制匿名用户提问次数（内存存储，单机有效）。
 */
@Service
public class GuideAskLimitServiceImpl implements GuideAskLimitService {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_X_REAL_IP = "X-Real-IP";

    private final GuideLimitProperties properties;
    private final ConcurrentHashMap<String, List<Long>> ipTimestamps = new ConcurrentHashMap<>();

    public GuideAskLimitServiceImpl(GuideLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (StringUtils.hasText(ip)) {
            int i = ip.indexOf(',');
            ip = (i > 0) ? ip.substring(0, i).trim() : ip.trim();
        }
        if (!StringUtils.hasText(ip)) {
            ip = request.getHeader(HEADER_X_REAL_IP);
        }
        if (!StringUtils.hasText(ip)) {
            ip = request.getRemoteAddr();
        }
        return StringUtils.hasText(ip) ? ip : "unknown";
    }

    @Override
    public synchronized boolean tryRecordAndCheck(String clientIp) {
        long now = System.currentTimeMillis();
        long windowMs = properties.getWindowHours() * 3600L * 1000;
        int max = properties.getMaxAsksPerWindow();
        List<Long> list = ipTimestamps.computeIfAbsent(clientIp, k -> new ArrayList<>());
        list.removeIf(t -> now - t > windowMs);
        if (list.size() >= max) {
            return false;
        }
        list.add(now);
        return true;
    }

    @Override
    public synchronized int getRemainingAsks(String clientIp) {
        long now = System.currentTimeMillis();
        long windowMs = properties.getWindowHours() * 3600L * 1000;
        int max = properties.getMaxAsksPerWindow();
        List<Long> list = ipTimestamps.get(clientIp);
        if (list == null) {
            return max;
        }
        list.removeIf(t -> now - t > windowMs);
        int used = list.size();
        return Math.max(0, max - used);
    }
}
