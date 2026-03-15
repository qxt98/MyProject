package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.entity.User;
import com.smartpharma.security.JwtUtil;
import com.smartpharma.service.OperationLogService;
import com.smartpharma.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证接口：登录（返回 JWT + 用户信息）、获取当前登录用户。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final OperationLogService operationLogService;

    /** 登录：校验用户名密码，返回 token 与用户信息（密码已脱敏） */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) return Result.fail("请输入用户名和密码");
        User user = userService.login(username, password);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        operationLogService.record("认证", "登录", "用户 " + username, request);
        Map<String, Object> data = Map.of(
                "token", token,
                "user", user
        );
        return Result.ok(data);
    }

    /** 获取当前登录用户信息（需携带有效 token） */
    @GetMapping("/me")
    public Result<User> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return Result.fail("未登录");
        }
        String username = (String) auth.getPrincipal();
        User user = userService.getByUsername(username);
        user.setPassword(null);
        return Result.ok(user);
    }
}
