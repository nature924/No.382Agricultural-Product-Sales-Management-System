package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private String email;
    private Integer status;
    private String businessLicense;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    @TableField(exist = false)
    private List<Menu> menuList;
    @TableField(exist = false)
    private String token;
}
