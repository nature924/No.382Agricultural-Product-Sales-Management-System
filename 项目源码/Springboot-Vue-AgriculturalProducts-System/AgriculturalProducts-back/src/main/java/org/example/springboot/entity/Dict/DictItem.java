package org.example.springboot.entity.Dict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dict_item")
public class DictItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String dictTypeCode; // 关联sys_dict表的id字段
    private String itemKey;
    private String itemValue;
    private String description;
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd")
    private LocalDateTime createTime;
    @JsonFormat(locale="zh",timezone="GMT+8", pattern="yyyy-MM-dd")

    private LocalDateTime updateTime;
}
