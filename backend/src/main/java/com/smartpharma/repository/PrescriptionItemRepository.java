package com.smartpharma.repository;

import com.smartpharma.entity.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 处方明细数据访问层
 */
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    /** 是否存在该药品的处方明细（删除药品前引用校验） */
    boolean existsByDrugId(Long drugId);
}
