package org.example.springboot.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.entity.Product;
import org.springframework.util.DigestUtils;

import java.util.StringJoiner;

/**
 * 缓存工具�?- 提供分页缓存指纹生成功能
 * 
 * <p>本工具类主要用于生成分页查询结果的唯一标识，解决以下问题：
 * 1. 不同查询条件返回相同结果时的重复缓存问题
 * 2. 数据更新后的缓存自动失效问题
 * 3. 分页结果集的唯一性标识生�?/p>
 * 
 * @author 系统自动生成
 * @since 2024-03-01
 */
public class CacheUtil {
    
    /**
     * 生成分页结果ID指纹
     * 
     * <p>实现原理�?
     * 1. 提取分页结果中所有商品的ID
     * 2. 使用竖线分隔符拼接ID字符�?
     * 3. 对拼接后的字符串进行MD5哈希计算
     * 
     * 示例�?
     * 商品ID列表：[101, 203, 305]
     * 拼接字符串："101|203|305"
     * MD5结果�?a1b2c3d4e5f6..."�?2位十六进制字符串�?/p>
     *
     * @param page 分页查询结果对象
     * @return 32位MD5哈希字符�?
     * 
     * @see org.springframework.util.DigestUtils#md5DigestAsHex(byte[])
     */
    public static String generateIdFingerprint(Page<Product> page) {
        StringJoiner sj = new StringJoiner("|");
        page.getRecords().forEach(p -> sj.add(p.getId().toString()));
        return DigestUtils.md5DigestAsHex(sj.toString().getBytes());
    }
}