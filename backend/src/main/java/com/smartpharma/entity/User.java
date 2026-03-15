package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体（需求 F-Sys-01：用户账号）
 * 对应表 sys_user，用于登录与 RBAC 角色权限，密码需加密存储。
 */
@Data
@Entity
@Table(name = "sys_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录名，唯一 */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** 密码，存储时使用 BCrypt 加密 */
    @Column(nullable = false, length = 200)
    private String password;

    /** 真实姓名 */
    @Column(length = 50)
    private String realName;

    /** 角色：ADMIN/PHARMACIST/PURCHASER/REVIEWER/DOCTOR/NURSE 等，用于 RBAC */
    @Column(length = 20)
    private String role;

    /** 是否启用，禁用后无法登录 */
    private Boolean enabled = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
