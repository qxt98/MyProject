package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库记录实体（需求 F-Stock-01）
 * 对应表 stock_in，记录每次入库的药品、批次、数量、生产/效期、备注，用于流水查询与库存增加。
 */
@Data
@Entity
@Table(name = "stock_in")
public class StockIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 药品 ID */
    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    /** 批次号 */
    @Column(name = "batch_no", length = 50)
    private String batchNo;

    /** 入库数量 */
    private Integer quantity;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 有效期至 */
    private LocalDate expiryDate;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    /** 入库时间 */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
