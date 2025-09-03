package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class CarouselItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String imageUrl;
    private String tag;
    private String title;
    private String description;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long productId;
    private Integer sortOrder;
    private Integer status;

    @TableField(exist = false)
    private Product product;
} 