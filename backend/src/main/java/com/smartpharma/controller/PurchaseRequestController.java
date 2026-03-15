package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.entity.PurchaseRequest;
import com.smartpharma.entity.User;
import com.smartpharma.service.OperationLogService;
import com.smartpharma.service.PurchaseRequestService;
import com.smartpharma.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/** 采购审批接口（需求 F-Pur-01～F-Pur-05） */
@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;
    private final UserService userService;
    private final OperationLogService operationLogService;

    @GetMapping
    public Result<Page<PurchaseRequest>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(purchaseRequestService.list(status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public Result<PurchaseRequest> getById(@PathVariable Long id) {
        return Result.ok(purchaseRequestService.getById(id));
    }

    /** 提交采购申请，申请人取自当前登录用户（需求 F-Pur-01） */
    @PostMapping
    public Result<PurchaseRequest> create(@RequestBody PurchaseRequest req, HttpServletRequest request) {
        Long userId = getCurrentUserId();
        if (userId != null) req.setCreatedBy(userId);
        PurchaseRequest saved = purchaseRequestService.create(req);
        operationLogService.record("采购审批", "提交申请", "申请ID:" + saved.getId() + " 药品ID:" + saved.getDrugId(), request);
        return Result.ok(saved);
    }

    /** 审批通过：仅管理员、审核员（7.3 改进建议：按动作细分权限） */
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @PostMapping("/{id}/approve")
    public Result<PurchaseRequest> approve(@PathVariable Long id, @RequestParam(required = false) Long approverId, HttpServletRequest request) {
        Long uid = approverId != null ? approverId : getCurrentUserId();
        if (uid == null) uid = 1L;
        PurchaseRequest r = purchaseRequestService.approve(id, uid);
        operationLogService.record("采购审批", "审批通过", "申请ID:" + id, request);
        return Result.ok(r);
    }

    /** 驳回：仅管理员、审核员（7.3 改进建议：按动作细分权限） */
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    @PostMapping("/{id}/reject")
    public Result<PurchaseRequest> reject(@PathVariable Long id, @RequestParam(required = false) Long approverId, @RequestParam String rejectReason, HttpServletRequest request) {
        Long uid = approverId != null ? approverId : getCurrentUserId();
        if (uid == null) uid = 1L;
        PurchaseRequest r = purchaseRequestService.reject(id, uid, rejectReason);
        operationLogService.record("采购审批", "驳回", "申请ID:" + id + " 原因:" + rejectReason, request);
        return Result.ok(r);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        try {
            User u = userService.getByUsername((String) auth.getPrincipal());
            return u.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
