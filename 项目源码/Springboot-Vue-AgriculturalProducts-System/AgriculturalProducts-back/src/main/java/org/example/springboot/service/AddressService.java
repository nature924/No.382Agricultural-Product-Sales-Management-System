package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Address;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.AddressMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddressService.class);

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private UserMapper userMapper;

    public Result<?> createAddress(Address address) {
        try {
            int result = addressMapper.insert(address);
            if (result > 0) {
                LOGGER.info("创建地址成功，地址ID：{}", address.getId());
                return Result.success(address);
            }
            return Result.error("-1", "创建地址失败");
        } catch (Exception e) {
            LOGGER.error("创建地址失败：{}", e.getMessage());
            return Result.error("-1", "创建地址失败：" + e.getMessage());
        }
    }

    public Result<?> updateAddress(Long id, Address address) {
        address.setId(id);
        try {
            int result = addressMapper.updateById(address);
            if (result > 0) {
                LOGGER.info("更新地址成功，地址ID：{}", id);
                return Result.success(address);
            }
            return Result.error("-1", "更新地址失败");
        } catch (Exception e) {
            LOGGER.error("更新地址失败：{}", e.getMessage());
            return Result.error("-1", "更新地址失败：" + e.getMessage());
        }
    }

    public Result<?> deleteAddress(Long id) {
        try {
            int result = addressMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除地址成功，地址ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除地址失败");
        } catch (Exception e) {
            LOGGER.error("删除地址失败：{}", e.getMessage());
            return Result.error("-1", "删除地址失败：" + e.getMessage());
        }
    }

    public Result<?> getAddressById(Long id) {
        Address address = addressMapper.selectById(id);
        if (address != null) {
            // 填充用户信息
            address.setUser(userMapper.selectById(address.getUserId()));
            return Result.success(address);
        }
        return Result.error("-1", "未找到地址");
    }

    public Result<?> getAddressesByUserId(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId);
        List<Address> addresses = addressMapper.selectList(queryWrapper);
        if (addresses != null && !addresses.isEmpty()) {
            // 填充用户信息
            addresses.forEach(address -> 
                address.setUser(userMapper.selectById(address.getUserId()))
            );
            return Result.success(addresses);
        }
        return Result.error("-1", "未找到地址");
    }

    public Result<?> getAddressesByPage(Long userId, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(Address::getUserId, userId);
        }

        Page<Address> page = new Page<>(currentPage, size);
        Page<Address> result = addressMapper.selectPage(page, queryWrapper);

        // 填充用户信息
        result.getRecords().forEach(address -> 
            address.setUser(userMapper.selectById(address.getUserId()))
        );

        return Result.success(result);
    }

    public Result<?> deleteBatch(List<Long> ids) {
        try {
            int result = addressMapper.deleteBatchIds(ids);
            if (result > 0) {
                LOGGER.info("批量删除地址成功，删除数量：{}", result);
                return Result.success();
            }
            return Result.error("-1", "批量删除地址失败");
        } catch (Exception e) {
            LOGGER.error("批量删除地址失败：{}", e.getMessage());
            return Result.error("-1", "批量删除地址失败：" + e.getMessage());
        }
    }
} 