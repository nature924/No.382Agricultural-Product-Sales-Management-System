package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.CarouselItem;
import org.example.springboot.service.CarouselItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/carousel")
public class CarouselItemController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarouselItemController.class);

    @Autowired
    private CarouselItemService carouselItemService;
    @PostMapping
    public Result<?> createCarouselItem(@RequestBody CarouselItem carouselItem) {
        return carouselItemService.createCarouselItem(carouselItem);
    }
    @PutMapping("/{id}")
    public Result<?> updateCarouselItem(@PathVariable Long id, @RequestBody CarouselItem carouselItem) {
        return carouselItemService.updateCarouselItem(id, carouselItem);
    }
    @DeleteMapping("/{id}")
    public Result<?> deleteCarouselItem(@PathVariable Long id) {
        return carouselItemService.deleteCarouselItem(id);
    }
    @GetMapping("/{id}")
    public Result<?> getCarouselItemById(@PathVariable Long id) {
        return carouselItemService.getCarouselItemById(id);
    }
    @GetMapping("/active")
    public Result<?> getActiveCarouselItems() {
        return carouselItemService.getActiveCarouselItems();
    }
    @GetMapping("/page")
    public Result<?> getCarouselItemsByPage(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return carouselItemService.getCarouselItemsByPage(currentPage, size);
    }
} 