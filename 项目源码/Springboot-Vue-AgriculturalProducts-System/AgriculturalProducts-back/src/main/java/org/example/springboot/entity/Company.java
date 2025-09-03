package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Company {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String phone;
    private Integer status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
} 