package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Address {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    private String receiver;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @TableField(exist = false)
    private User user;
} 