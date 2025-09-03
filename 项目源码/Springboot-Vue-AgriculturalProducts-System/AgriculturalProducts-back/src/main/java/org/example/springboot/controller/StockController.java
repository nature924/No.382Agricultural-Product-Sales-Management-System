package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.StockIn;
import org.example.springboot.entity.StockOut;
import org.example.springboot.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private StockService stockService;
    @PostMapping("/in")
    public Result<?> createStockIn(@RequestBody StockIn stockIn) {
        return stockService.createStockIn(stockIn);
    }
    @PostMapping("/out")
    public Result<?> createStockOut(@RequestBody StockOut stockOut) {
        return stockService.createStockOut(stockOut);
    }
    @GetMapping("/in/list")
    public Result<?> getStockInList(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(stockService.getStockInList(productId, supplier, status, operatorId, currentPage, size));
    }
    @GetMapping("/out/list")
    public Result<?> getStockOutList(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(stockService.getStockOutList(productId, type, status, operatorId, customerName, orderNo, currentPage, size));
    }
    @PutMapping("/in/{id}/invalidate")
    public Result<?> invalidateStockIn(@PathVariable Long id) {
        return stockService.invalidateStockIn(id);
    }
    @PutMapping("/out/{id}/invalidate")
    public Result<?> invalidateStockOut(@PathVariable Long id) {
        return stockService.invalidateStockOut(id);
    }
    @DeleteMapping("/in/{id}")
    public Result<?> deleteStockIn(@PathVariable Long id) {
        return stockService.deleteStockIn(id);
    }
    @DeleteMapping("/out/{id}")
    public Result<?> deleteStockOut(@PathVariable Long id) {
        return stockService.deleteStockOut(id);
    }
} 