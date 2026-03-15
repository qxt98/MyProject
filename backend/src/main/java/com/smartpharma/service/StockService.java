package com.smartpharma.service;

import com.smartpharma.entity.Stock;
import com.smartpharma.entity.StockIn;
import com.smartpharma.entity.StockOut;
import com.smartpharma.repository.StockInRepository;
import com.smartpharma.repository.StockOutRepository;
import com.smartpharma.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存管理服务（需求 F-Stock-01～F-Stock-06）
 * 入库增加库存并记流水，出库扣减库存并记流水，查询库存与流水、低库存与近效期预警。
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockInRepository stockInRepository;
    private final StockOutRepository stockOutRepository;

    @Transactional
    public StockIn in(StockIn in) {
        stockInRepository.save(in);
        Stock s = stockRepository.findByDrugIdAndBatchNo(in.getDrugId(), in.getBatchNo())
                .orElseGet(() -> {
                    Stock n = new Stock();
                    n.setDrugId(in.getDrugId());
                    n.setBatchNo(in.getBatchNo());
                    n.setQuantity(0);
                    n.setProductionDate(in.getProductionDate());
                    n.setExpiryDate(in.getExpiryDate());
                    return n;
                });
        s.setQuantity(s.getQuantity() + in.getQuantity());
        s.setProductionDate(in.getProductionDate());
        s.setExpiryDate(in.getExpiryDate());
        stockRepository.save(s);
        return in;
    }

    @Transactional
    public StockOut out(StockOut out) {
        Stock s = stockRepository.findByDrugIdAndBatchNo(out.getDrugId(), out.getBatchNo())
                .orElseThrow(() -> new RuntimeException("该批次库存不存在"));
        if (s.getQuantity() < out.getQuantity()) {
            throw new RuntimeException("库存不足");
        }
        s.setQuantity(s.getQuantity() - out.getQuantity());
        stockRepository.save(s);
        return stockOutRepository.save(out);
    }

    @Transactional(readOnly = true)
    public Page<StockIn> listIn(Long drugId, Pageable pageable) {
        if (drugId != null) {
            return stockInRepository.findByDrugId(drugId, pageable);
        }
        return stockInRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<StockOut> listOut(Long drugId, Pageable pageable) {
        if (drugId != null) {
            return stockOutRepository.findByDrugId(drugId, pageable);
        }
        return stockOutRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public List<Stock> listByDrugId(Long drugId) {
        return stockRepository.findByDrugIdOrderByExpiryDateAsc(drugId);
    }

    @Transactional(readOnly = true)
    public List<Stock> listLowStock(int threshold) {
        return stockRepository.findAll().stream()
                .filter(s -> s.getQuantity() < threshold)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Stock> listNearExpiry(int days) {
        LocalDate limit = LocalDate.now().plusDays(days);
        return stockRepository.findAll().stream()
                .filter(s -> s.getExpiryDate() != null && !s.getExpiryDate().isAfter(limit))
                .toList();
    }
}
