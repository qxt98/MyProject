package com.smartpharma.repository;

import com.smartpharma.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 库存数据访问层（需求 F-Stock）
 * 按药品 ID、药品+批次查询，用于入库/出库时更新或查询库存。
 */
public interface StockRepository extends JpaRepository<Stock, Long> {

    /** 按药品 ID 查所有批次库存，用于列表与出库选择 */
    List<Stock> findByDrugIdOrderByExpiryDateAsc(Long drugId);

    /** 按药品 ID + 批次号查唯一记录，用于入库合并或出库扣减 */
    Optional<Stock> findByDrugIdAndBatchNo(Long drugId, String batchNo);

    /** 是否存在该药品的库存记录（删除药品前引用校验） */
    boolean existsByDrugId(Long drugId);
}
