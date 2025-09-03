package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Cart;
import org.example.springboot.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/cart")
public class CartController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;
    @PostMapping
    public Result<?> addToCart(@RequestBody Cart cart) {
        return cartService.addToCart(cart);
    }
    @PutMapping("/{id}")
    public Result<?> updateCartItem(@PathVariable Long id, @RequestParam Integer quantity) {
        return cartService.updateCartItem(id, quantity);
    }
    @DeleteMapping("/{id}")
    public Result<?> deleteCartItem(@PathVariable Long id) {
        return cartService.deleteCartItem(id);
    }
    @GetMapping("/user/{userId}")
    public Result<?> getCartByUserId(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }
    @DeleteMapping("/clear/{userId}")
    public Result<?> clearCart(@PathVariable Long userId) {
        return cartService.clearCart(userId);
    }
    @GetMapping("/page")
    public Result<?> getCartByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return cartService.getCartByPage(userId, productName, currentPage, size);
    }
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Long> ids) {
        return cartService.deleteBatch(ids);
    }
} 