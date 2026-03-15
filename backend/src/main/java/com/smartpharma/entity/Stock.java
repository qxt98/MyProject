package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存记录实体（需求 F-Stock：按药品+批次维度）
 * 对应表 stock，同一药品同一批次唯一，入库增加、出库扣减数量，支持生产日期与有效期（近效期预警）。
 */
@Data
@Entity
@Table(name = "stock", uniqueConstraints = @UniqueConstraint(columnNames = {"drug_id", "batch_no"}))
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 药品 ID，关联 drug 表 */
    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    /** 批次号，与 drug_id 联合唯一 */
    @Column(name = "batch_no", length = 50)
    private String batchNo;

    /** 当前库存数量，出库时扣减、退库时回加 */
    private Integer quantity;

    /** 生产日期 */
    private java.time.LocalDate productionDate;

    /** 有效期至，用于近效期预警 */
    private java.time.LocalDate expiryDate;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        updatedAt = LocalDateTime.now();
    }
}
