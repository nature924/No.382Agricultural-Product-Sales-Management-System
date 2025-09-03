package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Cart;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.Product;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.CartMapper;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.mapper.ProductMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    public Result<?> addToCart(Cart cart) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                return Result.error("-1", "商品不存在");
            }

            // 检查库存是否足够
            if (product.getStock() < cart.getQuantity()) {
                return Result.error("-1", "库存不足");
            }

            // 检查是否已经存在相同商品
            LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Cart::getUserId, cart.getUserId())
                       .eq(Cart::getProductId, cart.getProductId());
            Cart existingCart = cartMapper.selectOne(queryWrapper);

            if (existingCart != null) {
                // 更新数量
                existingCart.setQuantity(existingCart.getQuantity() + cart.getQuantity());
                int result = cartMapper.updateById(existingCart);
                if (result > 0) {
                    LOGGER.info("更新购物车成功，购物车ID：{}", existingCart.getId());
                    return Result.success(existingCart);
                }
            } else {
                // 新增记录
                int result = cartMapper.insert(cart);
                if (result > 0) {
                    LOGGER.info("添加购物车成功，购物车ID：{}", cart.getId());
                    return Result.success(cart);
                }
            }
            return Result.error("-1", "操作购物车失败");
        } catch (Exception e) {
            LOGGER.error("操作购物车失败：{}", e.getMessage());
            return Result.error("-1", "操作购物车失败：" + e.getMessage());
        }
    }

    public Result<?> updateCartItem(Long id, Integer quantity) {
        try {
            Cart cart = cartMapper.selectById(id);
            if (cart == null) {
                return Result.error("-1", "未找到购物车记录");
            }

            // 检查商品库存
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null) {
                return Result.error("-1", "商品不存在");
            }
            if (product.getStock() < quantity) {
                return Result.error("-1", "库存不足");
            }

            cart.setQuantity(quantity);
            int result = cartMapper.updateById(cart);
            if (result > 0) {
                LOGGER.info("更新购物车成功，购物车ID：{}", id);
                return Result.success(cart);
            }
            return Result.error("-1", "更新购物车失败");
        } catch (Exception e) {
            LOGGER.error("更新购物车失败：{}", e.getMessage());
            return Result.error("-1", "更新购物车失败：" + e.getMessage());
        }
    }

    public Result<?> deleteCartItem(Long id) {
        try {
            int result = cartMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除购物车成功，购物车ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除购物车失败");
        } catch (Exception e) {
            LOGGER.error("删除购物车失败：{}", e.getMessage());
            return Result.error("-1", "删除购物车失败：" + e.getMessage());
        }
    }

    public Result<?> getCartByUserId(Long userId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId);
        List<Cart> carts = cartMapper.selectList(queryWrapper);
        if (carts != null && !carts.isEmpty()) {
            // 填充关联信息
            carts.forEach(cart -> {
                cart.setUser(userMapper.selectById(cart.getUserId()));
                cart.setProduct(productMapper.selectById(cart.getProductId()));
            });
            return Result.success(carts);
        }
        return Result.error("-1", "未找到购物车记录");
    }

    public Result<?> clearCart(Long userId) {
        try {
            LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Cart::getUserId, userId);
            int result = cartMapper.delete(queryWrapper);
            if (result > 0) {
                LOGGER.info("清空购物车成功，用户ID：{}", userId);
                return Result.success();
            }
            return Result.error("-1", "清空购物车失败");
        } catch (Exception e) {
            LOGGER.error("清空购物车失败：{}", e.getMessage());
            return Result.error("-1", "清空购物车失败：" + e.getMessage());
        }
    }

    public Result<?> getCartByPage(Long userId,String productName, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(Cart::getUserId, userId);
        }
        if (StringUtils.isNotEmpty(productName)) {
            List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>().like(Product::getName, productName));
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            queryWrapper.in(Cart::getProductId, productIds);
        }




        Page<Cart> page = new Page<>(currentPage, size);
        Page<Cart> result = cartMapper.selectPage(page, queryWrapper);

        // 填充关联信息
        result.getRecords().forEach(cart -> {
            cart.setUser(userMapper.selectById(cart.getUserId()));
            cart.setProduct(productMapper.selectById(cart.getProductId()));
        });

        return Result.success(result);
    }

    public Result<?> deleteBatch(List<Long> ids) {
        try {


            int result = cartMapper.deleteByIds(ids);
            if (result > 0) {
                LOGGER.info("批量删除购物车项成功，删除数量：{}", result);
                return Result.success();
            }
            return Result.error("-1", "批量删除购物车项失败");
        } catch (Exception e) {
            LOGGER.error("批量删除购物车项失败：{}", e.getMessage());
            return Result.error("-1", "批量删除购物车项失败：" + e.getMessage());
        }
    }
} 