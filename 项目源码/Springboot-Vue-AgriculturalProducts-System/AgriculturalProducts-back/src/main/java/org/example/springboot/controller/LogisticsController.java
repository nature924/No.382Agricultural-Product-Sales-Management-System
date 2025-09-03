package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Logistics;
import org.example.springboot.service.LogisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/logistics")
public class LogisticsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogisticsController.class);

    @Autowired
    private LogisticsService logisticsService;
    @PostMapping
    public Result<?> createLogistics(@RequestBody Logistics logistics) {
        return logisticsService.createLogistics(logistics);
    }
    @PutMapping("/{id}/status")
    public Result<?> updateLogisticsStatus(@PathVariable Long id, @RequestParam Integer status) {
        return logisticsService.updateLogisticsStatus(id, status);
    }
    @DeleteMapping("/{id}")
    public Result<?> deleteLogistics(@PathVariable Long id) {
        return logisticsService.deleteLogistics(id);
    }
    @GetMapping("/{id}")
    public Result<?> getLogisticsById(@PathVariable Long id) {
        return logisticsService.getLogisticsById(id);
    }
    @GetMapping("/order/{orderId}")
    public Result<?> getLogisticsByOrderId(@PathVariable Long orderId) {
        return logisticsService.getLogisticsByOrderId(orderId);
    }
    @GetMapping("/page")
    public Result<?> getLogisticsByPage(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return logisticsService.getLogisticsByPage(orderId, merchantId, status, currentPage, size);
    }
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Long> ids) {
        return logisticsService.deleteBatch(ids);
    }
    @PutMapping("/{id}/sign")
    public Result<?> signLogistics(@PathVariable Long id) {
        return logisticsService.signLogistics(id);
    }
} 