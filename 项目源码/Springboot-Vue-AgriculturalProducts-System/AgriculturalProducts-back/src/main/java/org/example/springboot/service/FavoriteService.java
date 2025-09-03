package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Favorite;
import org.example.springboot.entity.User;
import org.example.springboot.entity.Product;
import org.example.springboot.mapper.FavoriteMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FavoriteService.class);

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;


    public Result<?> createFavorite(Favorite favorite) {
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(favorite.getUserId());
            if (user == null) {
                return Result.error("-1", "用户不存在");
            }

            // 检查商品是否存在
            Product product = productMapper.selectById(favorite.getProductId());
            if (product == null) {
                return Result.error("-1", "商品不存在");
            }

            // 检查是否已经收藏，已收藏直接更新状态
            LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Favorite::getUserId, favorite.getUserId())
                       .eq(Favorite::getProductId, favorite.getProductId());
            if (favoriteMapper.selectCount(queryWrapper) > 0) {

                favorite.setStatus(favorite.getStatus()==0?1:0); // 默认状态为收藏
                return  updateFavoriteStatus(favorite.getUserId(), favorite.getProductId(), favorite.getStatus());

            }

            favorite.setStatus(1); // 默认状态为收藏
            int result = favoriteMapper.insert(favorite);
            if (result > 0) {
                LOGGER.info("创建收藏成功，收藏ID：{}", favorite.getId());
                return Result.success(favorite);
            }
            return Result.error("-1", "创建收藏失败");
        } catch (Exception e) {
            LOGGER.error("创建收藏失败：{}", e.getMessage());
            return Result.error("-1", "创建收藏失败：" + e.getMessage());
        }
    }

    public Result<?> updateFavoriteStatus(Long userId, Long productId, Integer status) {
        try {
            Favorite favorite = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
            if (favorite == null) {
                return Result.error("-1", "未找到收藏记录");
            }

            // 检查状态是否合法
            if (status < 0 || status > 1) {
                return Result.error("-1", "收藏状态不合法");
            }

            favorite.setStatus(status);
            int result = favoriteMapper.updateById(favorite);
            if (result > 0) {
                LOGGER.info("更新收藏状态成功，收藏ID：{}，新状态：{}", favorite.getId(), status);
                return Result.success(favorite);
            }
            return Result.error("-1", "更新收藏状态失败");
        } catch (Exception e) {
            LOGGER.error("更新收藏状态失败：{}", e.getMessage());
            return Result.error("-1", "更新收藏状态失败：" + e.getMessage());
        }
    }

    public Result<?> deleteFavorite(Long id) {
        try {



            int result = favoriteMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除收藏成功，收藏ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除收藏失败");
        } catch (Exception e) {
            LOGGER.error("删除收藏失败：{}", e.getMessage());
            return Result.error("-1", "删除收藏失败：" + e.getMessage());
        }
    }

    public Result<?> getFavoriteById(Long id) {
        Favorite favorite = favoriteMapper.selectById(id);
        if (favorite != null) {
            // 填充关联信息
            favorite.setUser(userMapper.selectById(favorite.getUserId()));
            favorite.setProduct(productMapper.selectById(favorite.getProductId()));
            return Result.success(favorite);
        }
        return Result.error("-1", "未找到收藏记录");
    }

    public Result<?> getFavoritesByUserId(Long userId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                   .eq(Favorite::getStatus, 1); // 只查询状态为收藏的记录
        List<Favorite> favorites = favoriteMapper.selectList(queryWrapper);
        if (favorites != null && !favorites.isEmpty()) {
            // 填充关联信息
            favorites.forEach(favorite -> {
                favorite.setUser(userMapper.selectById(favorite.getUserId()));
                favorite.setProduct(productMapper.selectById(favorite.getProductId()));
            });
            return Result.success(favorites);
        }
        return Result.error("-1", "未找到收藏记录");
    }

    public Result<?> getFavoritesByPage(Long userId, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(Favorite::getUserId, userId)
                       .eq(Favorite::getStatus, 1); // 只查询状态为收藏的记录
        }

        Page<Favorite> page = new Page<>(currentPage, size);
        Page<Favorite> result = favoriteMapper.selectPage(page, queryWrapper);

        // 填充关联信息
        result.getRecords().forEach(favorite -> {
            favorite.setUser(userMapper.selectById(favorite.getUserId()));
            favorite.setProduct(productMapper.selectById(favorite.getProductId()));
        });

        return Result.success(result);
    }

    public Result<?> deleteBatch(List<Long> ids) {
        try {


            int result = favoriteMapper.deleteBatchIds(ids);
            if (result > 0) {
                LOGGER.info("批量删除收藏成功，删除数量：{}", result);
                return Result.success();
            }
            return Result.error("-1", "批量删除收藏失败");
        } catch (Exception e) {
            LOGGER.error("批量删除收藏失败：{}", e.getMessage());
            return Result.error("-1", "批量删除收藏失败：" + e.getMessage());
        }
    }
} 