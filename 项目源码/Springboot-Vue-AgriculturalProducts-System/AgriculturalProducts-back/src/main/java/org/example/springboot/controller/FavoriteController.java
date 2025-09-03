package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Favorite;
import org.example.springboot.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FavoriteController.class);

    @Autowired
    private FavoriteService favoriteService;
    @PostMapping
    public Result<?> createFavorite(@RequestBody Favorite favorite) {
        return favoriteService.createFavorite(favorite);
    }
    @PutMapping("/{userId}/{productId}/status")
    public Result<?> updateFavoriteStatus(@PathVariable Long userId, @PathVariable Long productId, @RequestParam Integer status) {
        return favoriteService.updateFavoriteStatus(userId, productId, status);
    }
    @DeleteMapping("/{id}")
    public Result<?> deleteFavorite(@PathVariable Long id) {
        return favoriteService.deleteFavorite(id);
    }
    @GetMapping("/{id}")
    public Result<?> getFavoriteById(@PathVariable Long id) {
        return favoriteService.getFavoriteById(id);
    }
    @GetMapping("/user/{userId}")
    public Result<?> getFavoritesByUserId(@PathVariable Long userId) {
        return favoriteService.getFavoritesByUserId(userId);
    }
    @GetMapping("/page")
    public Result<?> getFavoritesByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return favoriteService.getFavoritesByPage(userId, currentPage, size);
    }
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Long> ids) {
        return favoriteService.deleteBatch(ids);
    }
} 