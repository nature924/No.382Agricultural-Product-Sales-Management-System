package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Product;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.Favorite;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.mapper.FavoriteMapper;
import org.example.springboot.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendService.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    // 计算用户相似度矩�?
    private Map<Long, Map<Long, Double>> calculateUserSimilarity() {
        // 构建用户-商品行为矩阵
        Map<Long, Set<Long>> userProductMap = new HashMap<>();

        // 获取所有订单数据（已完成的订单�?
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getStatus, 3); // 已完成状�?
        List<Order> orders = orderMapper.selectList(orderWrapper);

        // 获取所有收藏数据（有效的收藏）
        LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(Favorite::getStatus, 1); // 收藏状态为1
        List<Favorite> favorites = favoriteMapper.selectList(favoriteWrapper);

        // 构建用户-商品映射，购买行为权重为2，收藏行为权重为1
        for (Order order : orders) {
            Set<Long> products = userProductMap.computeIfAbsent(order.getUserId(), k -> new HashSet<>());
            products.add(order.getProductId());
            products.add(order.getProductId()); // 添加两次表示更高权重
        }

        for (Favorite favorite : favorites) {
            userProductMap.computeIfAbsent(favorite.getUserId(), k -> new HashSet<>())
                    .add(favorite.getProductId());
        }

        // 计算用户相似�?
        Map<Long, Map<Long, Double>> similarityMatrix = new HashMap<>();
        List<Long> userIds = new ArrayList<>(userProductMap.keySet());

        for (int i = 0; i < userIds.size(); i++) {
            Long user1 = userIds.get(i);
            Map<Long, Double> userSimilarities = new HashMap<>();
            similarityMatrix.put(user1, userSimilarities);

            for (int j = i + 1; j < userIds.size(); j++) {
                Long user2 = userIds.get(j);
                double similarity = calculateCosineSimilarity(
                        userProductMap.get(user1),
                        userProductMap.get(user2));
                userSimilarities.put(user2, similarity);
                similarityMatrix.computeIfAbsent(user2, k -> new HashMap<>())
                        .put(user1, similarity);
            }
        }

        return similarityMatrix;
    }

    // 计算余弦相似�?
    private double calculateCosineSimilarity(Set<Long> set1, Set<Long> set2) {
        if (set1 == null || set2 == null || set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        // 计算交集
        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // 添加最小阈�?
        if (intersection.isEmpty()) {
            return 0.0;
        }

        // 计算余弦相似�?- 交集大小作为点积，除以两个集合大小的平方根乘�?
        double numerator = intersection.size();
        double denominator = Math.sqrt(set1.size()) * Math.sqrt(set2.size());
        double similarity = numerator / denominator;

        LOGGER.debug("计算相似�? set1={}, set2={}, similarity={}", set1, set2, similarity);
        return similarity;
    }

    // 为指定用户生成推�?
    public Result<?> generateRecommendations(Long userId) {
        try {
            // 获取用户的订单和收藏数据
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getUserId, userId)
                    .eq(Order::getStatus, 3); // 已完成的订单
            List<Order> userOrders = orderMapper.selectList(orderWrapper);

            LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(Favorite::getUserId, userId)
                    .eq(Favorite::getStatus, 1); // 有效的收�?
            List<Favorite> userFavorites = favoriteMapper.selectList(favoriteWrapper);

            // 获取用户的商品集�?
            Set<Long> userProducts = new HashSet<>();
            userOrders.forEach(order -> userProducts.add(order.getProductId()));
            userFavorites.forEach(favorite -> userProducts.add(favorite.getProductId()));

            if (userProducts.isEmpty()) {
                LOGGER.warn("用户 {} 没有任何订单或收藏记录", userId);
            }

            // 获取用户相似度矩�?
            Map<Long, Map<Long, Double>> similarityMatrix = calculateUserSimilarity();

            // 获取相似用户
            Map<Long, Double> similarUsers = new HashMap<>();
            // 获取当前用户与其他用户的相似�?
            if (similarityMatrix.containsKey(userId)) {
                similarUsers.putAll(similarityMatrix.get(userId));
            }
            // 获取其他用户与当前用户的相似�?
            for (Map.Entry<Long, Map<Long, Double>> entry : similarityMatrix.entrySet()) {
                if (entry.getValue().containsKey(userId)) {
                    similarUsers.put(entry.getKey(), entry.getValue().get(userId));
                }
            }

            // 动态调整相似度阈�?
            double similarityThreshold;
            if (userProducts.size() < 3) { // 新用�?
                similarityThreshold = 0.2;
            } else if (userProducts.size() > 10) { // 活跃用户
                similarityThreshold = 0.4;
            } else {
                similarityThreshold = 0.3;
            }

            // 过滤和排序相似用�?
            similarUsers = similarUsers.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() >= similarityThreshold)
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 收集推荐商品及其得分
            Map<Long, Double> productScores = new HashMap<>();
            for (Map.Entry<Long, Double> entry : similarUsers.entrySet()) {
                Long similarUserId = entry.getKey();
                double similarity = entry.getValue();

                // 获取相似用户的订单和收藏
                List<Order> similarUserOrders = orderMapper.selectList(
                        new LambdaQueryWrapper<Order>()
                                .eq(Order::getUserId, similarUserId)
                                .eq(Order::getStatus, 3));

                List<Favorite> similarUserFavorites = favoriteMapper.selectList(
                        new LambdaQueryWrapper<Favorite>()
                                .eq(Favorite::getUserId, similarUserId)
                                .eq(Favorite::getStatus, 1));

                // 计算推荐分数（订单权�?，收藏权�?�?
                for (Order order : similarUserOrders) {
                    if (!userProducts.contains(order.getProductId())) {
                        productScores.merge(order.getProductId(), similarity * 2, Double::sum);
                    }
                }

                for (Favorite favorite : similarUserFavorites) {
                    if (!userProducts.contains(favorite.getProductId())) {
                        productScores.merge(favorite.getProductId(), similarity, Double::sum);
                    }
                }
            }

            List<Product> recommendations;
            if (productScores.isEmpty()) {
                LOGGER.info("没有找到相似用户，使用基于销量的推荐");
                recommendations = productMapper.selectList(
                        new LambdaQueryWrapper<Product>()
                                .orderByDesc(Product::getSalesCount)
                                .last("LIMIT 12"));
            } else {
                List<Map.Entry<Long, Double>> sortedProducts = new ArrayList<>(productScores.entrySet());
                sortedProducts.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                List<Long> recommendedIds = sortedProducts.stream()
                        .limit(12)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                recommendations = productMapper.selectList(
                        new LambdaQueryWrapper<Product>()
                                .in(Product::getId, recommendedIds));
            }

            return Result.success(recommendations);
        } catch (Exception e) {
            LOGGER.error("生成推荐失败: {}", e.getMessage());
            return Result.error("-1", "生成推荐失败: " + e.getMessage());
        }
    }

    // 定时更新推荐
    public void updateRecommendations() {
        try {
            // 获取所有用户ID
            List<Long> userIds = orderMapper.selectList(new LambdaQueryWrapper<>())
                    .stream()
                    .map(Order::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            // 为每个用户生成推?
            for (Long userId : userIds) {
                generateRecommendations(userId);
            }

            LOGGER.info("成功更新所有用户推");
        } catch (Exception e) {
            LOGGER.error("更新推荐失败: {}", e.getMessage());
        }
    }
}