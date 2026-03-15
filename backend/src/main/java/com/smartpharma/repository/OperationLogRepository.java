package com.smartpharma.repository;

import com.smartpharma.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 操作日志数据访问层（需求 F-Sys-04）
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Page<OperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<OperationLog> findByModuleOrderByCreatedAtDesc(String module, Pageable pageable);

    Page<OperationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
