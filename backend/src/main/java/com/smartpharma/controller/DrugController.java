package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.entity.Drug;
import com.smartpharma.service.DrugService;
import com.smartpharma.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 药品信息管理接口（需求文档 3.1 药品信息管理模块）
 * 提供药品的分页查询、详情、新增、修改、删除。
 */
@RestController
@RequestMapping("/drugs")
@RequiredArgsConstructor
public class DrugController {

    private final DrugService drugService;
    private final OperationLogService operationLogService;

    /** 分页查询药品列表，支持按名称、类别筛选，可选包含已停用 */
    @GetMapping
    public Result<Page<Drug>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean includeDisabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Drug> data = drugService.list(name, category, includeDisabled,
                PageRequest.of(page, size, Sort.by("id").descending()));
        return Result.ok(data);
    }

    /** 根据 ID 获取药品详情 */
    @GetMapping("/{id}")
    public Result<Drug> getById(@PathVariable Long id) {
        return Result.ok(drugService.getById(id));
    }

    /** 新增药品 */
    @PostMapping
    public Result<Drug> create(@RequestBody Drug drug, HttpServletRequest request) {
        Drug saved = drugService.create(drug);
        operationLogService.record("药品信息", "新增", "药品 " + saved.getName() + " ID:" + saved.getId(), request);
        return Result.ok(saved);
    }

    /** 修改药品信息 */
    @PutMapping("/{id}")
    public Result<Drug> update(@PathVariable Long id, @RequestBody Drug drug, HttpServletRequest request) {
        Drug updated = drugService.update(id, drug);
        operationLogService.record("药品信息", "修改", "药品 ID:" + id, request);
        return Result.ok(updated);
    }

    /** 删除药品（物理删除，若被引用需在业务层校验） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        drugService.delete(id);
        operationLogService.record("药品信息", "删除", "药品 ID:" + id, request);
        return Result.ok();
    }
}
