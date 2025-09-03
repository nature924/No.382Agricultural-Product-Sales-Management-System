package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.Product;
import org.example.springboot.entity.Logistics;
import org.example.springboot.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private AddressMapper addressMapper;



    public Result<?> createOrder(Order order) {
        try {
            // 检查商品库存
            Product product = productMapper.selectById(order.getProductId());
            if (product == null) {
                return Result.error("-1", "商品不存在");
            }
            if (product.getStock() < order.getQuantity()) {
                return Result.error("-1", "库存不足");
            }

            // 计算总价
            order.setTotalPrice(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

            int result = orderMapper.insert(order);

            if (result > 0) {
                // 更新商品库存

                LOGGER.info("创建订单成功，订单ID：{}", order.getId());
                return Result.success(order);
            }
            return Result.error("-1", "创建订单失败");
        } catch (Exception e) {
            LOGGER.error("创建订单失败：{}", e.getMessage());
            return Result.error("-1", "创建订单失败：" + e.getMessage());
        }
    }

    public Result<?> updateOrderStatus(Long id, Integer status) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("-1", "未找到订单");
            }

            order.setLastStatus(order.getStatus());
            order.setStatus(status);
            int result = orderMapper.updateById(order);
            if (result > 0) {
                // 查找该订单的物流信息
                LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Logistics::getOrderId, id);
                Logistics logistics = logisticsMapper.selectOne(queryWrapper);
                
                if (logistics != null) {
                    // 如果订单状态变为已退款，更新物流状态为已取消
                    if (status == 6) { // 6表示已退款
                        logistics.setStatus(3); // 3表示已取消
                        logisticsMapper.updateById(logistics);
                        LOGGER.info("订单退款成功，同步更新物流状态为已取消，物流ID：{}", logistics.getId());
                    }
                    // 如果订单状态变为已完成，更新物流状态为已签收
                    else if (status == 3) { // 3表示已完成
                        logistics.setStatus(2); // 2表示已签收
                        logisticsMapper.updateById(logistics);
                        LOGGER.info("订单已完成，同步更新物流状态为已签收，物流ID：{}", logistics.getId());
                    }
                }

                LOGGER.info("更新订单状态成功，订单ID：{}，新状态：{}", id, status);
                return Result.success(order);
            }
            return Result.error("-1", "更新订单状态失败");
        } catch (Exception e) {
            LOGGER.error("更新订单状态失败：{}", e.getMessage());
            return Result.error("-1", "更新订单状态失败：" + e.getMessage());
        }
    }

    public Result<?> deleteOrder(Long id) {
        try {
            deleteRelation(id);
            int result = orderMapper.deleteById(id);

            if (result > 0) {
                LOGGER.info("删除订单成功，订单ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除订单失败");
        } catch (Exception e) {
            LOGGER.error("删除订单失败：{}", e.getMessage());
            return Result.error("-1", "删除订单失败：" + e.getMessage());
        }
    }

    public void deleteRelation(Long id){
        logisticsMapper.delete(new LambdaQueryWrapper<Logistics>().eq(Logistics::getOrderId,id));
    }

    public Result<?> getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null) {
            // 填充关联信息
            order.setUser(userMapper.selectById(order.getUserId()));
            order.setProduct(productMapper.selectById(order.getProductId()));

            return Result.success(order);
        }
        return Result.error("-1", "未找到订单");
    }

    public Result<?> getOrdersByUserId(Long userId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(queryWrapper);
        if (orders != null && !orders.isEmpty()) {
            // 填充关联信息
            orders.forEach(order -> {
                order.setUser(userMapper.selectById(order.getUserId()));
                order.setProduct(productMapper.selectById(order.getProductId()));
            });
            return Result.success(orders);
        }
        return Result.error("-1", "未找到订单");
    }

    public Result<?> getOrdersByPage(Long userId,Long id,String status, Long merchantId,Integer currentPage, Integer size) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(Order::getUserId, userId);
        }
        if (id != null) {
            queryWrapper.eq(Order::getId, id);
        }
        if(StringUtils.isNotBlank(status)){
            queryWrapper.eq(Order::getStatus,status);
        }
        if (merchantId != null) {
            List<Product> product = productMapper.selectList(new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId));

            if(!product.isEmpty()){
            List<Long> productIds = product.stream().map(Product::getId).collect(Collectors.toList());
            queryWrapper.in(Order::getProductId, productIds);
            }else{
                Page<Order> page = new Page<>(currentPage, size);
                page.setTotal(0);
                page.setRecords(null);
                return Result.success(page);
            }
        }

        queryWrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> page = new Page<>(currentPage, size);
        Page<Order> result = orderMapper.selectPage(page, queryWrapper);

        // 填充关联信息
        result.getRecords().forEach(order -> {
            order.setUser(userMapper.selectById(order.getUserId()));
            Product product = productMapper.selectById(order.getProductId());
            if(product!=null){
                order.setProduct(product);
                order.setMerchant(userMapper.selectById(product.getMerchantId()));
            }else{
                order.setProduct(null);
                order.setMerchant(null);
            }
        });

        return Result.success(result);
    }

    public Result<?> refundOrder(Long id, String reason) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("-1", "未找到订单");
            }

            // 检查订单状态是否允许退款
            if (order.getStatus() != 1 && order.getStatus() != 2) {
                return Result.error("-1", "当前订单状态不允许退款");
            }

            order.setLastStatus(order.getStatus());  // 保存当前状态
            order.setStatus(5);  // 设为退款中
            order.setRefundStatus(1); // 申请退款
            order.setRefundReason(reason);
            int result = orderMapper.updateById(order);
            if (result > 0) {
                LOGGER.info("申请退款成功，订单ID：{}", id);
                return Result.success(order);
            }
            return Result.error("-1", "申请退款失败");
        } catch (Exception e) {
            LOGGER.error("申请退款失败：{}", e.getMessage());
            return Result.error("-1", "申请退款失败：" + e.getMessage());
        }
    }

    public Result<?> deleteBatch(List<Long> ids) {
        try {
            // 检查每个订单是否存在关联记录
            for (Long id : ids) {
                // 检查物流
                deleteRelation(id);
            }

            int result = orderMapper.deleteBatchIds(ids);
            if (result > 0) {
                LOGGER.info("批量删除订单成功，删除数量：{}", result);
                return Result.success();
            }
            return Result.error("-1", "批量删除订单失败");
        } catch (Exception e) {
            LOGGER.error("批量删除订单失败：{}", e.getMessage());
            return Result.error("-1", "批量删除订单失败：" + e.getMessage());
        }
    }
    @Transactional
    public Result<?> payOrder(Long id){

            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("-1", "未找到订单");
            }else{

                Product product=productMapper.selectById(order.getProductId());
                if (product!=null){
                    if(product.getStock()<order.getQuantity()){
                        return Result.error("-1", "库存不足");
                    }
                    product.setSalesCount(product.getSalesCount()+order.getQuantity());
                    product.setStock(product.getStock()-order.getQuantity());
                    order.setStatus(1);
                    int res = productMapper.updateById(product);

                    if(res<=0){
                        return  Result.error("-1","支付异常");
                    }
                    updateOrder(order.getId(),order);
                }

            }
        return Result.success();
    }

    public Result<?> updateOrderAddress(String name, Long id,String address, String phone) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("-1", "未找到订单");
            }

            // 检查订单状态，只有未发货的订单才能修改地址
            if (order.getStatus() > 1) {
                return Result.error("-1", "订单已发货，无法修改收货地址");
            }
            order.setRecvName(name);
            order.setRecvAddress(address);
            order.setRecvPhone(phone);
            
            int result = orderMapper.updateById(order);
            if (result > 0) {
                LOGGER.info("更新订单收货信息成功，订单ID：{}", id);
                return Result.success(order);
            }
            return Result.error("-1", "更新订单收货信息失败");
        } catch (Exception e) {
            LOGGER.error("更新订单收货信息失败：{}", e.getMessage());
            return Result.error("-1", "更新订单收货信息失败：" + e.getMessage());
        }
    }
    public Result<?> updateOrder(Long id, Order order) {
        try {
            Order existingOrder = orderMapper.selectById(id);
            if (existingOrder == null) {
                return Result.error("-1", "未找到订单");
            }

            // 设置ID确保更新正确的订单
            order.setId(id);
            
            // 保持原有的不可修改字段
            order.setCreatedAt(existingOrder.getCreatedAt());
            order.setUserId(existingOrder.getUserId());
            order.setProductId(existingOrder.getProductId());
            order.setTotalPrice(existingOrder.getTotalPrice());
            
            int result = orderMapper.updateById(order);
            if (result > 0) {
                // 查找该订单的物流信息
                LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Logistics::getOrderId, id);
                Logistics logistics = logisticsMapper.selectOne(queryWrapper);
                
                if (logistics != null) {
                    // 如果订单状态变为已退款，更新物流状态为已取消
                    if (order.getStatus() == 6 && existingOrder.getStatus() != 6) {
                        logistics.setStatus(3); // 3表示已取消
                        logisticsMapper.updateById(logistics);
                        LOGGER.info("订单退款成功，同步更新物流状态为已取消，物流ID：{}", logistics.getId());
                    }
                    // 如果订单状态变为已完成，更新物流状态为已签收
                    else if (order.getStatus() == 3 && existingOrder.getStatus() != 3) {
                        logistics.setStatus(2); // 2表示已签收
                        logisticsMapper.updateById(logistics);
                        LOGGER.info("订单已完成，同步更新物流状态为已签收，物流ID：{}", logistics.getId());
                    }
                }

                LOGGER.info("更新订单成功，订单ID：{}", id);
                return Result.success(order);
            }
            return Result.error("-1", "更新订单信息失败");
        } catch (Exception e) {
            LOGGER.error("更新订单失败：{}", e.getMessage());
            return Result.error("-1", "更新订单失败：" + e.getMessage());
        }
    }

    public Result<?> getOrderLogistics(Long orderId) {
        try {
            // 检查订单是否存在
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return Result.error("-1", "未找到订单");
            }

            // 查询物流信息
            LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Logistics::getOrderId, orderId);
            Logistics logistics = logisticsMapper.selectOne(queryWrapper);
            
            if (logistics != null) {
                // 填充关联信息
                logistics.setOrder(order);

                return Result.success(logistics);
            }
            return Result.error("-1", "未找到物流信息");
        } catch (Exception e) {
            LOGGER.error("查询订单物流信息失败：{}", e.getMessage());
            return Result.error("-1", "查询订单物流信息失败：" + e.getMessage());
        }
    }

    /**
     * 处理退款申请
     * @param id 订单ID
     * @param status 退款状态：6-同意退款 7-拒绝退款
     * @param remark 处理备注
     * @return 处理结果
     */
    public Result<?> handleRefund(Long id, Integer status, String remark) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("-1", "未找到订单");
            }

            // 检查订单是否处于退款中状态
            if (order.getStatus() != 5) {
                return Result.error("-1", "订单当前状态不是退款中");
            }

            // 保存原始状态
            order.setLastStatus(order.getStatus());
            // 更新状态
            order.setStatus(status);
            order.setRefundStatus(status == 6 ? 3 : 4); // 3-已退款 4-退款失败
            order.setRefundTime(Timestamp.valueOf(LocalDateTime.now()));
            order.setRemark(remark);
            
            int result = orderMapper.updateById(order);
            if (result > 0) {
                // 如果同意退款，恢复商品库存
                if (status == 6) {
                    Product product = productMapper.selectById(order.getProductId());
                    if (product != null) {
                        // 增加库存
                        product.setStock(product.getStock() + order.getQuantity());
                        // 减少销量
                        if (product.getSalesCount() >= order.getQuantity()) {
                            product.setSalesCount(product.getSalesCount() - order.getQuantity());
                        }
                        productMapper.updateById(product);
                        LOGGER.info("退款成功，已恢复商品库存，商品ID：{}，数量：{}", product.getId(), order.getQuantity());
                    }
                }

                // 同步更新物流状态
                LambdaQueryWrapper<Logistics> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Logistics::getOrderId, id);
                Logistics logistics = logisticsMapper.selectOne(queryWrapper);
                if (logistics != null && status == 6) { // 如果同意退款
                    logistics.setStatus(3); // 设置物流状态为已取消
                    logisticsMapper.updateById(logistics);
                    LOGGER.info("订单退款成功，同步更新物流状态为已取消，物流ID：{}", logistics.getId());
                }
                
                LOGGER.info("处理退款成功，订单ID：{}，处理结果：{}", id, status == 6 ? "已退款" : "拒绝退款");
                return Result.success(order);
            }
            return Result.error("-1", "处理退款失败");
        } catch (Exception e) {
            LOGGER.error("处理退款失败：{}", e.getMessage());
            return Result.error("-1", "处理退款失败：" + e.getMessage());
        }
    }
} 