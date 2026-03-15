package com.smartpharma.repository;

import com.smartpharma.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 处方数据访问层（需求 F-Rx-05 按患者、医生、状态、时间查询）
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p WHERE (:patientName IS NULL OR :patientName = '' OR p.patientName LIKE CONCAT('%', :patientName, '%')) AND (:doctorName IS NULL OR :doctorName = '' OR p.doctorName LIKE CONCAT('%', :doctorName, '%')) AND (:status IS NULL OR :status = '' OR p.status = :status) ORDER BY p.createdAt DESC")
    Page<Prescription> findByCondition(@Param("patientName") String patientName, @Param("doctorName") String doctorName, @Param("status") String status, Pageable pageable);
}
