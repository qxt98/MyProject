package com.smartpharma.service;

import com.smartpharma.entity.PurchaseRequest;
import com.smartpharma.repository.PurchaseRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 采购审批服务（需求 F-Pur）：提交、审批通过/驳回 */
@Service
@RequiredArgsConstructor
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;

    public Page<PurchaseRequest> list(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return purchaseRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }
        return purchaseRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public PurchaseRequest getById(Long id) {
        return purchaseRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("采购申请不存在"));
    }

    @Transactional
    public PurchaseRequest create(PurchaseRequest req) {
        req.setStatus("PENDING");
        return purchaseRequestRepository.save(req);
    }

    @Transactional
    public PurchaseRequest approve(Long id, Long approverId) {
        PurchaseRequest r = getById(id);
        if (!"PENDING".equals(r.getStatus())) throw new RuntimeException("当前状态不可审批");
        r.setStatus("APPROVED");
        r.setApprovedAt(LocalDateTime.now());
        r.setApprovedBy(approverId);
        return purchaseRequestRepository.save(r);
    }

    @Transactional
    public PurchaseRequest reject(Long id, Long approverId, String rejectReason) {
        PurchaseRequest r = getById(id);
        if (!"PENDING".equals(r.getStatus())) throw new RuntimeException("当前状态不可审批");
        r.setStatus("REJECTED");
        r.setApprovedAt(LocalDateTime.now());
        r.setApprovedBy(approverId);
        r.setRejectReason(rejectReason);
        return purchaseRequestRepository.save(r);
    }
}
