package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购申请实体（需求 F-Pur-01～F-Pur-04）
 * 对应表 purchase_request，记录药品、数量、供应商、预算、理由、状态；审批通过/驳回后更新状态与审批人、时间。
 */
@Data
@Entity
@Table(name = "purchase_request")
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    private Integer quantity;

    @Column(length = 200)
    private String supplier;

    @Column(precision = 12, scale = 2)
    private BigDecimal budgetAmount;

    @Column(length = 500)
    private String reason;

    /** 状态：PENDING 待审批，APPROVED 已通过，REJECTED 已驳回 */
    @Column(length = 20)
    private String status = "PENDING";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
