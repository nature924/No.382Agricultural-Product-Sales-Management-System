package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String name;
    private String description;

    @NotNull(message = "价格不能为空")
    @PositiveOrZero(message = "价格不能为负数")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @PositiveOrZero(message = "库存不能为负数")
    private Integer stock;
    private Integer isDiscount;
    private BigDecimal discountPrice;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    private String imageUrl;
    private Integer salesCount = 0;

    @NotNull(message = "商户ID不能为空")
    private Long merchantId;
    private Integer status = 1;
    private String placeOfOrigin;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @TableField(exist = false)
    private Category category;

    @TableField(exist = false)
    private User merchant;
} 