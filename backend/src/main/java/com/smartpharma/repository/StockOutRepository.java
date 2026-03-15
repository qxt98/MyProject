package com.smartpharma.repository;

import com.smartpharma.entity.StockOut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** 出库记录数据访问层（需求 F-Stock-04 流水查询） */
public interface StockOutRepository extends JpaRepository<StockOut, Long> {

    Page<StockOut> findByDrugId(Long drugId, Pageable pageable);

    Page<StockOut> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
