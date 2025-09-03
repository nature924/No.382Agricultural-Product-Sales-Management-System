package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.CarouselItem;
import org.example.springboot.entity.Product;
import org.example.springboot.mapper.CarouselItemMapper;
import org.example.springboot.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarouselItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CarouselItemService.class);

    @Resource
    private CarouselItemMapper carouselItemMapper;

    @Resource
    private ProductMapper productMapper;

    public Result<?> createCarouselItem(CarouselItem carouselItem) {
        // 检查商品是否存在
        if (carouselItem.getProductId() != null&&carouselItem.getProductId() != 0) {
            Product product = productMapper.selectById(carouselItem.getProductId());
            if (product == null) {
                return Result.error("-1", "关联的商品不存在");
            }
        }
        
        try {
            carouselItemMapper.insert(carouselItem);
            LOGGER.info("Created carousel item: {}", carouselItem.getId());
            return Result.success(carouselItem);
        } catch (Exception e) {
            LOGGER.error("Failed to create carousel item", e);
            return Result.error("-1", "创建轮播图失败");
        }
    }

    public Result<?> updateCarouselItem(Long id, CarouselItem carouselItem) {
        CarouselItem existing = carouselItemMapper.selectById(id);
        if (existing == null) {
            return Result.error("-1", "轮播图不存在");
        }


        // 检查商品是否存在
        if (carouselItem.getProductId() != null&&carouselItem.getProductId() != 0) {
            Product product = productMapper.selectById(carouselItem.getProductId());
            if (product == null) {
                return Result.error("-1", "关联的商品不存在");
            }
        }

        carouselItem.setId(id);
        try {
            carouselItemMapper.updateById(carouselItem);
            LOGGER.info("Updated carousel item: {}", id);
            return Result.success();
        } catch (Exception e) {
            LOGGER.error("Failed to update carousel item: {}", id, e);
            return Result.error("-1", "更新轮播图失败");
        }
    }

    public Result<?> deleteCarouselItem(Long id) {
        try {
            carouselItemMapper.deleteById(id);
            LOGGER.info("Deleted carousel item: {}", id);
            return Result.success();
        } catch (Exception e) {
            LOGGER.error("Failed to delete carousel item: {}", id, e);
            return Result.error("-1", "删除轮播图失败");
        }
    }

    public Result<?> getCarouselItemById(Long id) {
        CarouselItem item = carouselItemMapper.selectById(id);
        if (item != null && item.getProductId() != 0) {
            item.setProduct(productMapper.selectById(item.getProductId()));
        }
        return Result.success(item);
    }

    public Result<?> getActiveCarouselItems() {
        LambdaQueryWrapper<CarouselItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CarouselItem::getStatus, 1)
                   .orderByAsc(CarouselItem::getSortOrder);
        List<CarouselItem> items = carouselItemMapper.selectList(queryWrapper);
        
        // 填充商品信息
        for (CarouselItem item : items) {
            if (item.getProductId()!=null&&item.getProductId() != 0) {
                item.setProduct(productMapper.selectById(item.getProductId()));
            }
        }
        
        return Result.success(items);
    }

    public Result<?> getCarouselItemsByPage(Integer currentPage, Integer size) {
        Page<CarouselItem> page = new Page<>(currentPage, size);
        Page<CarouselItem> result = carouselItemMapper.selectPage(page, 
            new LambdaQueryWrapper<CarouselItem>().orderByAsc(CarouselItem::getSortOrder));
        
        // 填充商品信息
        for (CarouselItem item : result.getRecords()) {
            if (item.getProductId()!=null&&item.getProductId() != 0) {
                item.setProduct(productMapper.selectById(item.getProductId()));
            }
        }
        
        return Result.success(result);
    }
} 