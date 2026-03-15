package com.smartpharma.repository;

import com.smartpharma.entity.StockIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** 入库记录数据访问层（需求 F-Stock-04 流水查询） */
public interface StockInRepository extends JpaRepository<StockIn, Long> {

    Page<StockIn> findByDrugId(Long drugId, Pageable pageable);

    Page<StockIn> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
