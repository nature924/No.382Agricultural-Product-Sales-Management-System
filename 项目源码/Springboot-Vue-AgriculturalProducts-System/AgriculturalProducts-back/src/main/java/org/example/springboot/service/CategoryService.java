package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Category;
import org.example.springboot.entity.Product;
import org.example.springboot.mapper.CategoryMapper;
import org.example.springboot.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    public Result<?> createCategory(Category category) {
        try {
            int result = categoryMapper.insert(category);
            if (result > 0) {
                LOGGER.info("创建分类成功，分类ID：{}", category.getId());
                return Result.success(category);
            }
            return Result.error("-1", "创建分类失败");
        } catch (Exception e) {
            LOGGER.error("创建分类失败：{}", e.getMessage());
            return Result.error("-1", "创建分类失败：" + e.getMessage());
        }
    }

    public Result<?> updateCategory(Long id, Category category) {
        category.setId(id);
        try {
            int result = categoryMapper.updateById(category);
            if (result > 0) {
                LOGGER.info("更新分类成功，分类ID：{}", id);
                return Result.success(category);
            }
            return Result.error("-1", "更新分类失败");
        } catch (Exception e) {
            LOGGER.error("更新分类失败：{}", e.getMessage());
            return Result.error("-1", "更新分类失败：" + e.getMessage());
        }
    }

    public Result<?> deleteCategory(Long id) {
        try {
            // 检查是否存在关联商品
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.eq(Product::getCategoryId, id);
            Long productCount = productMapper.selectCount(productQuery);
            if (productCount > 0) {
                return Result.error("-1", "无法删除分类，存在关联商品");
            }

            int result = categoryMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除分类成功，分类ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除分类失败");
        } catch (Exception e) {
            LOGGER.error("删除分类失败：{}", e.getMessage());
            return Result.error("-1", "删除分类失败：" + e.getMessage());
        }
    }

    public Result<?> getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category != null) {
            // 填充商品数量
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.eq(Product::getCategoryId, id);
            category.setProductCount(Math.toIntExact(productMapper.selectCount(productQuery)));
            return Result.success(category);
        }
        return Result.error("-1", "未找到分类");
    }

    public Result<?> getCategoriesByPage(String name, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        if (name != null) {
            queryWrapper.like(Category::getName, name);
        }

        Page<Category> page = new Page<>(currentPage, size);
        Page<Category> result = categoryMapper.selectPage(page, queryWrapper);

        // 填充每个分类的商品数量
        result.getRecords().forEach(category -> {
            LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
            productQuery.eq(Product::getCategoryId, category.getId());
            category.setProductCount(Math.toIntExact(productMapper.selectCount(productQuery)));
        });

        return Result.success(result);
    }

    public Result<?> getAllCategories() {
        List<Category> categories = categoryMapper.selectList(null);
        if (categories != null && !categories.isEmpty()) {
            // 填充每个分类的商品数量
            categories.forEach(category -> {
                LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
                productQuery.eq(Product::getCategoryId, category.getId());
                category.setProductCount(Math.toIntExact(productMapper.selectCount(productQuery)));
            });
            return Result.success(categories);
        }
        return Result.error("-1", "未找到分类");
    }

    public Result<?> deleteBatch(List<Long> ids) {
        try {
            // 检查每个分类是否存在关联商品
            for (Long id : ids) {
                LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
                productQuery.eq(Product::getCategoryId, id);
                Long productCount = productMapper.selectCount(productQuery);
                if (productCount > 0) {
                    return Result.error("-1", "无法删除分类ID：" + id + "，存在关联商品");
                }
            }

            int result = categoryMapper.deleteBatchIds(ids);
            if (result > 0) {
                LOGGER.info("批量删除分类成功，删除数量：{}", result);
                return Result.success();
            }
            return Result.error("-1", "批量删除分类失败");
        } catch (Exception e) {
            LOGGER.error("批量删除分类失败：{}", e.getMessage());
            return Result.error("-1", "批量删除分类失败：" + e.getMessage());
        }
    }
} 