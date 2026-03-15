package com.smartpharma.repository;

import com.smartpharma.entity.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 采购申请数据访问层（需求 F-Pur-04 按状态、时间等查询）
 */
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    Page<PurchaseRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<PurchaseRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 是否存在该药品的采购申请（删除药品前引用校验） */
    boolean existsByDrugId(Long drugId);
}
