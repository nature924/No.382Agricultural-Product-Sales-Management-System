package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    private Integer status;
    private Integer lastStatus;
    private String remark;
    private Timestamp refundTime;
    private Integer refundStatus;
    private String refundReason;
    private String recvAddress;
    private String recvPhone;
    private String recvName;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Product product;

    @TableField(exist = false)
    private User merchant;

} 