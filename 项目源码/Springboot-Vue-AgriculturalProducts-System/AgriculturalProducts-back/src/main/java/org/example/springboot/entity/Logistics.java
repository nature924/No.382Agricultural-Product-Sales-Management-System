package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Logistics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String companyName;
    private String trackingNumber;
    //时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp expectedArrivalTime;
    private Integer status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @TableField(exist = false)
    private Order order;
} 