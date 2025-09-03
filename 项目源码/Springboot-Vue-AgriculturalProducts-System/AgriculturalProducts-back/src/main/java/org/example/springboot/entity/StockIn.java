package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
public class StockIn {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "入库数量不能为空")
    @Positive(message = "入库数量必须大于0")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    @Positive(message = "单价必须大于0")
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String supplier;

    @NotNull(message = "入库日期不能为空")
    private Date stockDate;

    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;
    private String remark;
    private Integer status = 1;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @TableField(exist = false)
    private Product product;

    @TableField(exist = false)
    private User operator;
} 