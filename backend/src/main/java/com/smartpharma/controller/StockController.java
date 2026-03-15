package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.entity.Stock;
import com.smartpharma.entity.StockIn;
import com.smartpharma.entity.StockOut;
import com.smartpharma.service.OperationLogService;
import com.smartpharma.service.StockService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存管理接口（需求 3.2 库存管理模块）
 * 提供入库、出库、入库/出库流水查询、按药品查库存、低库存与近效期预警。
 */
@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final OperationLogService operationLogService;

    /** 入库登记 */
    @PostMapping("/in")
    public Result<StockIn> in(@RequestBody StockIn body, HttpServletRequest request) {
        StockIn saved = stockService.in(body);
        operationLogService.record("库存管理", "入库", "药品ID:" + body.getDrugId() + " 数量:" + body.getQuantity() + (body.getRemark() != null ? " " + body.getRemark() : ""), request);
        return Result.ok(saved);
    }

    /** 出库登记 */
    @PostMapping("/out")
    public Result<StockOut> out(@RequestBody StockOut body, HttpServletRequest request) {
        StockOut saved = stockService.out(body);
        operationLogService.record("库存管理", "出库", "药品ID:" + body.getDrugId() + " 数量:" + body.getQuantity(), request);
        return Result.ok(saved);
    }

    /** 入库流水分页，可选 drugId */
    @GetMapping("/in/list")
    public Result<Page<StockIn>> listIn(
            @RequestParam(required = false) Long drugId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(stockService.listIn(drugId, PageRequest.of(page, size)));
    }

    /** 出库流水分页，可选 drugId */
    @GetMapping("/out/list")
    public Result<Page<StockOut>> listOut(
            @RequestParam(required = false) Long drugId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(stockService.listOut(drugId, PageRequest.of(page, size)));
    }

    /** 按药品查库存列表（多批次） */
    @GetMapping("/list")
    public Result<List<Stock>> listByDrug(@RequestParam Long drugId) {
        return Result.ok(stockService.listByDrugId(drugId));
    }

    /** 低库存预警列表，默认阈值 10 */
    @GetMapping("/warn/low")
    public Result<List<Stock>> warnLow(@RequestParam(defaultValue = "10") int threshold) {
        return Result.ok(stockService.listLowStock(threshold));
    }

    /** 近效期预警，默认 180 天内 */
    @GetMapping("/warn/expiry")
    public Result<List<Stock>> warnExpiry(@RequestParam(defaultValue = "180") int days) {
        return Result.ok(stockService.listNearExpiry(days));
    }
}
