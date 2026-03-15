package com.smartpharma.config;

import com.smartpharma.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置：JWT 认证 + 基于角色的接口权限（RBAC），符合中国医院操作流程。
 * - /auth/** 放行（登录、获取当前用户需 token 时在 Controller 内校验）。
 * - /auth/me 需认证，其余 /api 业务接口按角色控制见下方注释。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(a -> a
                        // 登录放行；获取当前用户需认证
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/me").authenticated()
                        .requestMatchers("/auth/**").permitAll()
                        // Swagger 文档（开发/调试用）；context-path 为 /api 时部分环境需带前缀
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v3/api-docs/**", "/api/swagger-ui/**", "/api/swagger-ui.html").permitAll()
                        // 系统管理、操作日志：仅管理员
                        .requestMatchers("/user/**", "/operation-log/**").hasRole("ADMIN")
                        // 药品信息：管理员、药师可增删改；采购员、审核员、医生仅可读（下拉选药品、处方开药等）
                        .requestMatchers(HttpMethod.GET, "/drugs", "/drugs/**", "/api/drugs", "/api/drugs/**").hasAnyRole("ADMIN", "PHARMACIST", "PURCHASER", "REVIEWER", "DOCTOR")
                        .requestMatchers("/drugs/**", "/api/drugs/**").hasAnyRole("ADMIN", "PHARMACIST")
                        // 库存管理：管理员、药师、采购员
                        .requestMatchers("/stock/**").hasAnyRole("ADMIN", "PHARMACIST", "PURCHASER")
                        // 采购审批：管理员、采购员与药剂科（提交/查询）、审核员（审批）
                        .requestMatchers("/purchase/**", "/api/purchase/**").hasAnyRole("ADMIN", "PHARMACIST", "PURCHASER", "REVIEWER")
                        // 处方审核：管理员、审核员、医生可读写；护士仅可读（7.3 改进：护士处方查询）
                        .requestMatchers(HttpMethod.GET, "/prescription", "/prescription/**", "/api/prescription", "/api/prescription/**").hasAnyRole("ADMIN", "REVIEWER", "DOCTOR", "NURSE")
                        // POST 创建/提交等（含 /api 前缀，避免经代理时 403）
                        .requestMatchers(HttpMethod.POST, "/prescription", "/prescription/**", "/api/prescription", "/api/prescription/**").hasAnyRole("ADMIN", "REVIEWER", "DOCTOR")
                        .requestMatchers("/prescription", "/prescription/**", "/api/prescription", "/api/prescription/**").hasAnyRole("ADMIN", "REVIEWER", "DOCTOR")
                        // 用药指导：已登录用户；患者/访客可匿名咨询（7.3 改进）
                        .requestMatchers(HttpMethod.POST, "/guide/ask", "/guide/ask/stream", "/api/guide/ask", "/api/guide/ask/stream").permitAll()
                        .requestMatchers("/guide/**").authenticated()
                        // 首页、根路径等（不能使用空字符串 ""，AntPathRequestMatcher 会报 Pattern cannot be null or empty）
                        .requestMatchers("/", "/error", "/api", "/api/").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
