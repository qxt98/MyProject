package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.entity.OperationLog;
import com.smartpharma.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志接口（需求 F-Sys-04）
 * 仅管理员可访问，支持按模块、用户、分页查询。
 */
@RestController
@RequestMapping("/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public Result<Page<OperationLog>> list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(operationLogService.list(module, userId, PageRequest.of(page, size)));
    }
}
