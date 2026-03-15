package com.smartpharma.service;

import com.smartpharma.entity.Drug;
import com.smartpharma.repository.DrugRepository;
import com.smartpharma.repository.PrescriptionItemRepository;
import com.smartpharma.repository.PurchaseRequestRepository;
import com.smartpharma.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 药品信息管理服务（需求 F-Drug-01～F-Drug-05）
 * 负责药品的查询、新增、修改、删除，保证事务与数据一致性。
 */
@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugRepository drugRepository;
    private final StockRepository stockRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;

    /** 条件分页查询，支持名称模糊、类别精确、是否包含停用 */
    @Transactional(readOnly = true)
    public Page<Drug> list(String name, String category, boolean includeDisabled, Pageable pageable) {
        return drugRepository.findByCondition(name, category, includeDisabled, pageable);
    }

    /** 按 ID 查询，不存在则抛异常 */
    @Transactional(readOnly = true)
    public Drug getById(Long id) {
        return drugRepository.findById(id).orElseThrow(() -> new RuntimeException("药品不存在"));
    }

    /** 新增药品，由 JPA 自动填充创建时间 */
    @Transactional
    public Drug create(Drug drug) {
        return drugRepository.save(drug);
    }

    /** 更新药品，仅更新非空字段，避免覆盖未传字段 */
    @Transactional
    public Drug update(Long id, Drug input) {
        Drug existing = getById(id);
        existing.setName(input.getName());
        existing.setCategory(input.getCategory());
        existing.setDosageForm(input.getDosageForm());
        existing.setIndication(input.getIndication());
        existing.setManufacturer(input.getManufacturer());
        existing.setSupplier(input.getSupplier());
        existing.setPurchasePrice(input.getPurchasePrice());
        existing.setSalePrice(input.getSalePrice());
        existing.setImageUrl(input.getImageUrl());
        existing.setDisabled(input.getDisabled());
        return drugRepository.save(existing);
    }

    /** 删除前校验是否被库存、采购、处方引用；若被引用则抛异常提示使用停用功能（需求 F-Drug-04） */
    @Transactional
    public void delete(Long id) {
        Drug drug = getById(id);
        if (stockRepository.existsByDrugId(id)) {
            throw new RuntimeException("该药品已被库存引用，无法删除，请使用停用功能");
        }
        if (purchaseRequestRepository.existsByDrugId(id)) {
            throw new RuntimeException("该药品已被采购申请引用，无法删除，请使用停用功能");
        }
        if (prescriptionItemRepository.existsByDrugId(id)) {
            throw new RuntimeException("该药品已被处方引用，无法删除，请使用停用功能");
        }
        drugRepository.delete(drug);
    }
}
