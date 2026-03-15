package com.smartpharma.service;

import com.smartpharma.entity.Prescription;
import com.smartpharma.entity.PrescriptionItem;
import com.smartpharma.repository.DrugRepository;
import com.smartpharma.repository.PrescriptionItemRepository;
import com.smartpharma.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方审核服务（需求 F-Rx-01～F-Rx-05）
 * 处方增删改查、提交、审核通过/驳回，明细校验药品存在、按患者/医生/状态查询。
 */
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final DrugRepository drugRepository;

    @Transactional(readOnly = true)
    public Page<Prescription> list(String patientName, String doctorName, String status, Pageable pageable) {
        return prescriptionRepository.findByCondition(patientName, doctorName, status, pageable);
    }

    @Transactional(readOnly = true)
    public Prescription getById(Long id) {
        return prescriptionRepository.findById(id).orElseThrow(() -> new RuntimeException("处方不存在"));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionItem> getItems(Long prescriptionId) {
        return prescriptionItemRepository.findByPrescriptionId(prescriptionId);
    }

    @Transactional
    public Prescription create(Prescription p) {
        p.setStatus("DRAFT");
        return prescriptionRepository.save(p);
    }

    /** 为处方添加一条明细；校验药品存在（需求 F-Rx-02） */
    @Transactional
    public PrescriptionItem addItem(PrescriptionItem item) {
        getById(item.getPrescriptionId());
        if (item.getDrugId() == null || !drugRepository.existsById(item.getDrugId())) {
            throw new RuntimeException("请选择有效药品");
        }
        return prescriptionItemRepository.save(item);
    }

    @Transactional
    public Prescription submit(Long id) {
        Prescription p = getById(id);
        if (!"DRAFT".equals(p.getStatus())) {
            throw new RuntimeException("仅草稿可提交");
        }
        p.setStatus("SUBMITTED");
        return prescriptionRepository.save(p);
    }

    @Transactional
    public Prescription approve(Long id, String remark) {
        Prescription p = getById(id);
        if (!"SUBMITTED".equals(p.getStatus())) {
            throw new RuntimeException("仅已提交处方可审核");
        }
        p.setStatus("APPROVED");
        p.setReviewRemark(remark);
        p.setReviewedAt(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }

    @Transactional
    public Prescription reject(Long id, String remark) {
        Prescription p = getById(id);
        if (!"SUBMITTED".equals(p.getStatus())) {
            throw new RuntimeException("仅已提交处方可审核");
        }
        p.setStatus("REJECTED");
        p.setReviewRemark(remark);
        p.setReviewedAt(LocalDateTime.now());
        return prescriptionRepository.save(p);
    }
}
