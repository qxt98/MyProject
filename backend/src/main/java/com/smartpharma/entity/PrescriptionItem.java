package com.smartpharma.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 处方明细实体（需求 F-Rx：处方明细）
 * 对应表 prescription_item，一条处方可包含多种药品，每条记录为药品、剂量、给药途径、用药时长等。
 */
@Data
@Entity
@Table(name = "prescription_item")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 处方 ID，关联 prescription */
    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    /** 药品 ID */
    @Column(name = "drug_id", nullable = false)
    private Long drugId;

    /** 剂量规格（如 10mg/次） */
    @Column(length = 100)
    private String dosage;

    /** 给药途径（口服、注射等） */
    @Column(length = 50)
    private String route;

    /** 用药时长（如 7 天） */
    @Column(length = 50)
    private String duration;
}
