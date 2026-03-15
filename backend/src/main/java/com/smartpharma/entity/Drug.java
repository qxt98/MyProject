package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品信息实体（需求 F-Drug：药品基础信息）
 * 对应表 drug，存储药品名称、类别、剂型规格、适应症、厂家、供应商、价格等，支持逻辑停用。
 */
@Data
@Entity
@Table(name = "drug")
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 药品名称 */
    @Column(nullable = false, length = 200)
    private String name;

    /** 药品类别（如：西药、中药） */
    @Column(length = 50)
    private String category;

    /** 剂型规格 */
    @Column(length = 100)
    private String dosageForm;

    /** 适应症描述 */
    @Column(columnDefinition = "text")
    private String indication;

    /** 生产厂家 */
    @Column(length = 200)
    private String manufacturer;

    /** 供应商信息 */
    @Column(length = 200)
    private String supplier;

    /** 采购价 */
    @Column(precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    /** 销售价 */
    @Column(precision = 12, scale = 2)
    private BigDecimal salePrice;

    /** 药品图片 URL */
    @Column(length = 500)
    private String imageUrl;

    /** 是否停用 */
    private Boolean disabled = false;

    /** 创建时间，插入时自动填充 */
    @Column(updatable = false)
    private LocalDateTime createdAt;
    /** 更新时间，插入与更新时自动刷新 */
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
