package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.example.springboot.entity.Category;
import org.example.springboot.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/category")
public class CategoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public Result<?> createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }
    @GetMapping("/page")
    public Result<?> getCategoriesByPage(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        return categoryService.getCategoriesByPage(name, currentPage, size);
    }
    @GetMapping("/all")
    public Result<?> getAllCategories() {
        return categoryService.getAllCategories();
    }
    @DeleteMapping("/batch")
    public Result<?> deleteBatch(@RequestParam List<Long> ids) {
        return categoryService.deleteBatch(ids);
    }
} 