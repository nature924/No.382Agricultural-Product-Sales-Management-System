package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Review;
import org.example.springboot.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/review")
public class ReviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;
    @PostMapping
    public Result<?> createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }
    @PutMapping("/{id}/status")
    public Result<?> updateReviewStatus(@PathVariable Long id, @RequestParam Integer status) {
        return reviewService.updateReviewStatus(id, status);
    }
    @DeleteMapping("/{id}")
    public Result<?> deleteReview(@PathVariable Long id) {
        return reviewService.deleteReview(id);
    }
    @GetMapping("/{id}")
    public Result<?> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }
    @GetMapping("/product/{productId}")
    public Result<?> getReviewsByProductId(@PathVariable Long productId, @RequestParam(required = false) Integer status) {
        return reviewService.getReviewsByProductId(productId, status);
    }
    @GetMapping("/page")
    public Result<?> getReviewsByPage(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return reviewService.getReviewsByPage(productId, productName,userId,username,merchantId, status, currentPage, size);
    }
}

 