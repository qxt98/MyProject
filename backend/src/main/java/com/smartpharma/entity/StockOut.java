package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出库记录实体（需求 F-Stock-02）
 * 对应表 stock_out，记录每次出库的药品、批次、数量，出库时扣减对应库存并写入本表。
 */
@Data
@Entity
@Table(name = "stock_out")
public class StockOut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 药品 ID */
    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    /** 批次号 */
    @Column(name = "batch_no", length = 50)
    private String batchNo;

    /** 出库数量 */
    private Integer quantity;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    /** 出库时间 */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
