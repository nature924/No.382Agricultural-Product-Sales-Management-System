package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Review;
import org.example.springboot.entity.User;
import org.example.springboot.entity.Product;
import org.example.springboot.mapper.ReviewMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    public Result<?> createReview(Review review) {
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(review.getUserId());
            if (user == null) {
                return Result.error("-1", "用户不存在");
            }

            // 检查商品是否存在
            Product product = productMapper.selectById(review.getProductId());
            if (product == null) {
                return Result.error("-1", "商品不存在");
            }

            // 检查评分是否合法
            if (review.getRating() < 1 || review.getRating() > 5) {
                return Result.error("-1", "评分必须在1-5之间");
            }

            int result = reviewMapper.insert(review);
            if (result > 0) {
                LOGGER.info("创建评价成功，评价ID：{}", review.getId());
                return Result.success(review);
            }
            return Result.error("-1", "创建评价失败");
        } catch (Exception e) {
            LOGGER.error("创建评价失败：{}", e.getMessage());
            return Result.error("-1", "创建评价失败：" + e.getMessage());
        }
    }

    public Result<?> updateReviewStatus(Long id, Integer status) {
        try {
            Review review = reviewMapper.selectById(id);
            if (review == null) {
                return Result.error("-1", "未找到评论");
            }

            review.setStatus(status);
            int result = reviewMapper.updateById(review);
            if (result > 0) {
                LOGGER.info("更新评价状态成功，评价ID：{}，新状态：{}", id, status);
                return Result.success(review);
            }
            return Result.error("-1", "更新评价状态失败");
        } catch (Exception e) {
            LOGGER.error("更新评价状态失败：{}", e.getMessage());
            return Result.error("-1", "更新评价状态失败：" + e.getMessage());
        }
    }

    public Result<?> deleteReview(Long id) {
        try {
            int result = reviewMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除评价成功，评价ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除评价失败");
        } catch (Exception e) {
            LOGGER.error("删除评价失败：{}", e.getMessage());
            return Result.error("-1", "删除评价失败：" + e.getMessage());
        }
    }

    public Result<?> getReviewById(Long id) {
        Review review = reviewMapper.selectById(id);
        if (review != null) {
            // 填充关联信息
            review.setUser(userMapper.selectById(review.getUserId()));
            review.setProduct(productMapper.selectById(review.getProductId()));
            return Result.success(review);
        }
        return Result.error("-1", "未找到评论");
    }

    public Result<?> getReviewsByProductId(Long productId, Integer status) {
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getProductId, productId);
        if (status != null) {
            queryWrapper.eq(Review::getStatus, status);
        }
        List<Review> reviews = reviewMapper.selectList(queryWrapper);
        if (reviews != null && !reviews.isEmpty()) {
            // 填充关联信息
            reviews.forEach(review -> {
                review.setUser(userMapper.selectById(review.getUserId()));
                review.setProduct(productMapper.selectById(review.getProductId()));
            });
            return Result.success(reviews);
        }
        return Result.error("-1", "未找到评论");
    }

    public Result<?> getReviewsByPage(Long productId, String productName, Integer userId, String username,
            Long merchantId, Integer status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            queryWrapper.eq(Review::getProductId, productId);
        }
        if (StringUtils.isNotBlank(productName)) {
            List<Long> productIds = productMapper
                    .selectList(new LambdaQueryWrapper<Product>().like(Product::getName, productName)).stream()
                    .map(Product::getId).collect(Collectors.toList());
            queryWrapper.in(Review::getProductId, productIds);
        }
        if (StringUtils.isNotBlank(username)) {
            List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, username))
                    .stream().map(User::getId).collect(Collectors.toList());
            queryWrapper.in(Review::getUserId, userIds);
        }
        if (userId != null) {
            queryWrapper.eq(Review::getUserId, userId);
        }
        if (merchantId != null) {
            List<Product> products = productMapper
                    .selectList(new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchantId));
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            queryWrapper.in(Review::getProductId, productIds);
        }

        if (status != null) {
            queryWrapper.eq(Review::getStatus, status);

        }

        Page<Review> page = new Page<>(currentPage, size);
        Page<Review> result = reviewMapper.selectPage(page, queryWrapper);

        // 填充关联信息
        result.getRecords().forEach(review -> {
            review.setUser(userMapper.selectById(review.getUserId()));
            review.setProduct(productMapper.selectById(review.getProductId()));
        });

        return Result.success(result);
    }

}