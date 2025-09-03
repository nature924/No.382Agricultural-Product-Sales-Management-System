package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.Product;
import org.example.springboot.entity.User;
import org.example.springboot.entity.Category;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.mapper.ProductMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.CategoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsService.class);

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CategoryMapper categoryMapper;

    public Map<String, Object> getMonthlyOrderStatistics(Long merchantId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前月份和上个月的时间范围
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        // 统计本月订单数
        long currentMonthOrders = getOrderCount(currentMonth, merchantId);
        // 统计上月订单数
        long lastMonthOrders = getOrderCount(lastMonth, merchantId);
        
        // 计算环比增长率
        double growthRate = calculateGrowthRate(currentMonthOrders, lastMonthOrders);
        
        result.put("currentMonthOrders", currentMonthOrders);
        result.put("lastMonthOrders", lastMonthOrders);
        result.put("growthRate", String.format("%.2f", growthRate) + "%");
        
        LOGGER.info("月度订单统计结果: {}", result);
        return result;
    }

    public Map<String, Object> getMonthlySalesStatistics(Long merchantId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前月份和上个月的时间范围
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        // 统计本月销售额
        double currentMonthSales = getSalesAmount(currentMonth, merchantId);
        // 统计上月销售额
        double lastMonthSales = getSalesAmount(lastMonth, merchantId);
        
        // 计算环比增长率
        double growthRate = calculateGrowthRate(currentMonthSales, lastMonthSales);
        
        result.put("currentMonthSales", currentMonthSales);
        result.put("lastMonthSales", lastMonthSales);
        result.put("growthRate", String.format("%.2f", growthRate) + "%");
        
        LOGGER.info("月度销售额统计结果: {}", result);
        return result;
    }

    public Map<String, Object> getUserOrderStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前月份和上个月的时间范围
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        // 统计本月订单数
        long currentMonthOrders = getUserOrderCount(currentMonth, userId);
        // 统计上月订单数
        long lastMonthOrders = getUserOrderCount(lastMonth, userId);
        // 统计总订单数
        long totalOrders = getTotalUserOrderCount(userId);
        
        // 计算环比增长率
        double growthRate = calculateGrowthRate(currentMonthOrders, lastMonthOrders);
        
        result.put("currentMonthOrders", currentMonthOrders);
        result.put("lastMonthOrders", lastMonthOrders);
        result.put("totalOrders", totalOrders);
        result.put("growthRate", String.format("%.2f", growthRate) + "%");
        
        LOGGER.info("用户订单统计结果: {}", result);
        return result;
    }

    public Map<String, Object> getUserSpendingStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前月份和上个月的时间范围
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        // 统计本月消费
        double currentMonthSpending = getUserSpendingAmount(currentMonth, userId);
        // 统计上月消费
        double lastMonthSpending = getUserSpendingAmount(lastMonth, userId);
        // 统计总消费
        double totalSpending = getTotalUserSpending(userId);
        
        // 计算环比增长率
        double growthRate = calculateGrowthRate(currentMonthSpending, lastMonthSpending);
        
        result.put("currentMonthSpending", currentMonthSpending);
        result.put("lastMonthSpending", lastMonthSpending);
        result.put("totalSpending", totalSpending);
        result.put("growthRate", String.format("%.2f", growthRate) + "%");
        
        LOGGER.info("用户消费统计结果: {}", result);
        return result;
    }

    public Map<String, Object> getYearlyUserStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取当前年份和上一年的时间范围
        int currentYear = LocalDateTime.now().getYear();
        
        // 统计今年用户总数
        long currentYearUsers = getUserCount(currentYear);
        // 统计去年用户总数
        long lastYearUsers = getUserCount(currentYear - 1);
        
        // 计算同比增长率
        double growthRate = calculateGrowthRate(currentYearUsers, lastYearUsers);
        
        result.put("currentYearUsers", currentYearUsers);
        result.put("lastYearUsers", lastYearUsers);
        result.put("growthRate", String.format("%.2f", growthRate) + "%");
        
        LOGGER.info("年度用户统计结果: {}", result);
        return result;
    }

    public Map<String, Object> getTopSellingProducts() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 构建查询条件
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getStatus, 1) // 只查询上架商品
                       .select(Product::getName, Product::getSalesCount) // 只查询需要的字段
                       .orderByDesc(Product::getSalesCount) // 按销量降序
                       .last("LIMIT 5"); // 限制返回5条记录
            // 查询前5个热销商品
            List<Product> products = productMapper.selectList(queryWrapper);
            
            // 转换为简化的销量统计结果
            List<Map<String, Object>> topProductStats = products.stream()
                .map(product -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("name", product.getName());
                    stat.put("salesCount", product.getSalesCount());
                    return stat;
                })
                .collect(Collectors.toList());
            
            result.put("topProducts", topProductStats);
            result.put("total", topProductStats.size());
            
            LOGGER.info("获取热销商品Top5成功，共{}条记录", topProductStats.size());
            
        } catch (Exception e) {
            LOGGER.error("获取热销商品Top5失败: {}", e.getMessage());
            result.put("error", "获取热销商品统计失败");
        }
        
        return result;
    }

    public Map<String, Object> getCategorySalesStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询所有有效分类
            List<Category> categories = categoryMapper.selectList(null);
            
            // 统计每个分类的销售总量
            List<Map<String, Object>> categoryStats = categories.stream()
                .map(category -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("name", category.getName());
                    
                    // 查询该分类下的所有商品销量总和
                    LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(Product::getCategoryId, category.getId())
                              .eq(Product::getStatus, 1)  // 只统计上架商品
                              .select(Product::getSalesCount);
                    
                    List<Product> products = productMapper.selectList(queryWrapper);
                    int totalSales = products.stream()
                        .mapToInt(Product::getSalesCount)
                        .sum();
                    
                    stat.put("salesCount", totalSales);
                    return stat;
                })
                .filter(stat -> (int)stat.get("salesCount") > 0) // 只返回有销量的分类
                .collect(Collectors.toList());
            
            // 计算总销量用于计算占比
            int totalSales = categoryStats.stream()
                .mapToInt(stat -> (int)stat.get("salesCount"))
                .sum();
            
            // 添加销售占比
            categoryStats.forEach(stat -> {
                int salesCount = (int)stat.get("salesCount");
                double percentage = totalSales > 0 ? 
                    (double)salesCount / totalSales * 100 : 0;
                stat.put("percentage", String.format("%.2f", percentage) + "%");
            });
            
            // 按销量降序排序
            categoryStats.sort((a, b) -> 
                ((Integer)b.get("salesCount")).compareTo((Integer)a.get("salesCount")));
            
            result.put("categoryStats", categoryStats);
            result.put("total", categoryStats.size());
            result.put("totalSales", totalSales);
            
            LOGGER.info("获取品类销售占比统计成功，共{}个品类", categoryStats.size());
            
        } catch (Exception e) {
            LOGGER.error("获取品类销售占比统计失败: {}", e.getMessage());
            result.put("error", "获取品类销售占比统计失败");
        }
        
        return result;
    }

    private long getOrderCount(YearMonth yearMonth, Long merchantId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreatedAt, yearMonth.atDay(1).atStartOfDay())
               .lt(Order::getCreatedAt, yearMonth.plusMonths(1).atDay(1).atStartOfDay())
               .eq(Order::getStatus,3) ; // 已完成的订单
        
        if (merchantId != null) {
            // 通过子查询找到商户的商品对应的订单
            wrapper.inSql(Order::getProductId, 
                    "SELECT id FROM product WHERE merchant_id = " + merchantId);
        }
        
        return orderMapper.selectCount(wrapper);
    }

    private double getSalesAmount(YearMonth yearMonth, Long merchantId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreatedAt, yearMonth.atDay(1).atStartOfDay())
               .lt(Order::getCreatedAt, yearMonth.plusMonths(1).atDay(1).atStartOfDay())
               .eq(Order::getStatus, 3); // 已完成的订单
        
        if (merchantId != null) {
            // 通过子查询找到商户的商品对应的订单
            wrapper.inSql(Order::getProductId, 
                "SELECT id FROM product WHERE merchant_id = " + merchantId);
        }
        
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();
    }

    private double calculateGrowthRate(double current, double last) {
        if (last == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - last) / last) * 100;
    }

    private long getUserOrderCount(YearMonth yearMonth, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreatedAt, yearMonth.atDay(1).atStartOfDay())
               .lt(Order::getCreatedAt, yearMonth.plusMonths(1).atDay(1).atStartOfDay())
               .eq(Order::getStatus, 3) // 已完成的订单
               .eq(Order::getUserId, userId);
        
        return orderMapper.selectCount(wrapper);
    }

    private long getTotalUserOrderCount(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 3) // 已完成的订单
               .eq(Order::getUserId, userId);
        
        return orderMapper.selectCount(wrapper);
    }

    private double getUserSpendingAmount(YearMonth yearMonth, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Order::getCreatedAt, yearMonth.atDay(1).atStartOfDay())
               .lt(Order::getCreatedAt, yearMonth.plusMonths(1).atDay(1).atStartOfDay())
               .eq(Order::getStatus, 3) // 已完成的订单
               .eq(Order::getUserId, userId);
        
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();
    }

    private double getTotalUserSpending(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 3) // 已完成的订单
               .eq(Order::getUserId, userId);
        
        List<Order> orders = orderMapper.selectList(wrapper);
        return orders.stream()
                    .mapToDouble(order -> order.getTotalPrice().doubleValue())
                    .sum();
    }

    private long getUserCount(int year) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(User::getCreatedAt, LocalDateTime.of(year, 1, 1, 0, 0))
               .lt(User::getCreatedAt, LocalDateTime.of(year + 1, 1, 1, 0, 0))
               .eq(User::getStatus, 1); // 只统计启用状态的用户
        
        return userMapper.selectCount(wrapper);
    }
} 