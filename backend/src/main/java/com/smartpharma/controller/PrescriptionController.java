package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.entity.Prescription;
import com.smartpharma.entity.PrescriptionItem;
import com.smartpharma.service.OperationLogService;
import com.smartpharma.service.PrescriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 处方审核接口（需求 3.4 处方审核模块） */
@RestController
@RequestMapping("/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final OperationLogService operationLogService;

    /** 处方列表，支持按患者、医生、状态筛选（需求 F-Rx-05） */
    @GetMapping
    public Result<Page<Prescription>> list(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(prescriptionService.list(patientName, doctorName, status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public Result<Prescription> getById(@PathVariable Long id) {
        return Result.ok(prescriptionService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<PrescriptionItem>> getItems(@PathVariable Long id) {
        return Result.ok(prescriptionService.getItems(id));
    }

    @PostMapping
    public Result<Prescription> create(@RequestBody Prescription p, HttpServletRequest request) {
        Prescription saved = prescriptionService.create(p);
        operationLogService.record("处方审核", "新建处方", "处方ID:" + saved.getId(), request);
        return Result.ok(saved);
    }

    @PostMapping("/item")
    public Result<PrescriptionItem> addItem(@RequestBody PrescriptionItem item) {
        return Result.ok(prescriptionService.addItem(item));
    }

    @PostMapping("/{id}/submit")
    public Result<Prescription> submit(@PathVariable Long id, HttpServletRequest request) {
        Prescription p = prescriptionService.submit(id);
        operationLogService.record("处方审核", "提交", "处方ID:" + id, request);
        return Result.ok(p);
    }

    /** 审核通过：仅管理员、审核员（7.3 改进建议：按动作细分权限） */
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @PostMapping("/{id}/approve")
    public Result<Prescription> approve(@PathVariable Long id, @RequestParam(required = false) String remark, HttpServletRequest request) {
        Prescription p = prescriptionService.approve(id, remark);
        operationLogService.record("处方审核", "审核通过", "处方ID:" + id, request);
        return Result.ok(p);
    }

    /** 驳回：仅管理员、审核员（7.3 改进建议：按动作细分权限） */
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @PostMapping("/{id}/reject")
    public Result<Prescription> reject(@PathVariable Long id, @RequestParam(required = false) String remark, HttpServletRequest request) {
        Prescription p = prescriptionService.reject(id, remark);
        operationLogService.record("处方审核", "驳回", "处方ID:" + id, request);
        return Result.ok(p);
    }
}
