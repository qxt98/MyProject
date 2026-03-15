package com.smartpharma.service;

import com.smartpharma.entity.OperationLog;
import com.smartpharma.repository.OperationLogRepository;
import com.smartpharma.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志服务（需求 F-Sys-04）
 * 记录关键操作并支持按条件查询。
 */
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final UserRepository userRepository;

    /** 记录一条操作日志，用户与 IP 从当前请求与登录态获取 */
    @Transactional
    public void record(String module, String action, String detail, HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail != null && detail.length() > 500 ? detail.substring(0, 500) : detail);
        log.setIp(request != null ? getClientIp(request) : null);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            try {
                userRepository.findByUsername((String) auth.getPrincipal()).ifPresent(u -> log.setUserId(u.getId()));
            } catch (Exception ignored) {}
        }
        operationLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<OperationLog> list(String module, Long userId, Pageable pageable) {
        if (module != null && !module.isEmpty()) {
            return operationLogRepository.findByModuleOrderByCreatedAtDesc(module, pageable);
        }
        if (userId != null) {
            return operationLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return operationLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
