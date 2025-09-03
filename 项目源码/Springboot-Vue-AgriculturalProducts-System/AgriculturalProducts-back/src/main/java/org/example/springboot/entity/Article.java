package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "资讯标题不能为空")
    private String title;

    @NotBlank(message = "资讯内容不能为空")
    private String content;

    private String summary;

    private String coverImage;

    private Integer viewCount = 0;

    @NotNull(message = "状态不能为空")
    private Integer status = 1;

    private Timestamp createdAt;

    private Timestamp updatedAt;
} 