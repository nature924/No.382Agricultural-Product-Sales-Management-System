package org.example.springboot.mapper.Dict;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.Dict.DictItem;

@Mapper
public interface DictItemMapper extends BaseMapper<DictItem> {
}
