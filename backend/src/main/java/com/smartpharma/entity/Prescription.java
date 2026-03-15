package com.smartpharma.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 处方主表实体（需求 F-Rx-01～F-Rx-05）
 * 对应表 prescription，记录患者、医生、状态；审核通过/不通过时更新状态与审核意见、时间。明细见 PrescriptionItem。
 */
@Data
@Entity
@Table(name = "prescription")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 患者姓名（可脱敏） */
    @Column(name = "patient_name", length = 50)
    private String patientName;

    /** 开方医生姓名 */
    @Column(name = "doctor_name", length = 50)
    private String doctorName;

    /** 患者病情信息（主诉、诊断等） */
    @Column(name = "patient_condition", length = 500)
    private String patientCondition;

    /** 状态：DRAFT 草稿，SUBMITTED 已提交，APPROVED 已通过，REJECTED 已驳回 */
    @Column(length = 20)
    private String status = "DRAFT";

    /** 审核意见（通过/不通过时的说明） */
    @Column(name = "review_remark", length = 500)
    private String reviewRemark;

    /** 创建时间 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 审核时间 */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
