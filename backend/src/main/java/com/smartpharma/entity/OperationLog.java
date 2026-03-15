package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体（需求 F-Sys-04）
 * 记录操作用户、时间、模块、操作类型、摘要、IP 等，支持安全审计与追溯。
 */
@Data
@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50)
    private String module;

    @Column(length = 100)
    private String action;

    @Column(columnDefinition = "text")
    private String detail;

    @Column(length = 50)
    private String ip;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
