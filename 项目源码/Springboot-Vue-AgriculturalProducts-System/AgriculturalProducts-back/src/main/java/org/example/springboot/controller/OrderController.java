package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Order;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/order")
public class OrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    @PostMapping
    public Result<?> createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }
    @PutMapping("/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateOrderStatus(id, status);
    }
    @PutMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id) {
        return orderService.payOrder(id);
    }
    @DeleteMapping("/{id}")
    public Result<?> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }
    @GetMapping("/{id}")
    public Result<?> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
    @GetMapping("/user/{userId}")
    public Result<?> getOrdersByUserId(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }
    @GetMapping("/page")
    public Result<?> getOrdersByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String status,

            @RequestParam(required = false) Long merchantId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return orderService.getOrdersByPage(userId, id,status,merchantId, currentPage, size);
    }
    @PostMapping("/{id}/refund")
    public Result<?> refundOrder(@PathVariable Long id, @RequestParam String reason) {
        return orderService.refundOrder(id, reason);
    }
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Long> ids) {
        return orderService.deleteBatch(ids);
    }
    @PutMapping("/{id}/address")
    public Result<?> updateOrderAddress(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String phone) {
        return orderService.updateOrderAddress(name,id, address, phone);
    }
    // 更新订单信息
    @PutMapping("/{id}")
    public Result<?> updateOrder(
            @PathVariable Long id,
            @RequestBody Order order) {
        return orderService.updateOrder(id, order);
    }
    @GetMapping("/{id}/logistics")
    public Result<?> getOrderLogistics(@PathVariable Long id) {
        return orderService.getOrderLogistics(id);
    }
    @PutMapping("/{id}/handle-refund")
    public Result<?> handleRefund(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam String remark) {
        return orderService.handleRefund(id, status, remark);
    }

    /**
     * 获取订单状态
     */
    @GetMapping("/{id}/status")
    public Result<Integer> getOrderStatus(@PathVariable Long id) {
        // 查询订单
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("-1","订单不存在");
        }
        // 返回订单状态
        return Result.success(order.getStatus());
    }
} 