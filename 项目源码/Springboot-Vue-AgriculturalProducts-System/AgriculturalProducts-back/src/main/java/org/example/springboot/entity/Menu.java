package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String path;
    private String icon;

    private String description;
    private Integer pid;
    private String pagePath;
    private Integer sortNum;
    @TableField(exist = false)
    private List<Menu> children;
    private String role;
    @TableField(exist = false)
    @Builder.Default
    private Boolean hasChildren=false;
}
