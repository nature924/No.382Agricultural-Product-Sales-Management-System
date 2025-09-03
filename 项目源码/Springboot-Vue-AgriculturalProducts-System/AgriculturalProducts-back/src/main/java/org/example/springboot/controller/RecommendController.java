package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.service.RecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/recommend")
public class RecommendController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendController.class);

    @Autowired
    private RecommendService recommendService;
    @GetMapping("/user/{userId}")
    public Result<?> getRecommendations(@PathVariable Long userId) {
        return recommendService.generateRecommendations(userId);
    }
    @PostMapping("/update")
    public Result<?> updateRecommendations() {
        recommendService.updateRecommendations();
        return Result.success();
    }
} 