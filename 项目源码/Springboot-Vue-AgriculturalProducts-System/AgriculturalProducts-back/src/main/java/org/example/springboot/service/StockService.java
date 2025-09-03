package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Product;
import org.example.springboot.entity.StockIn;
import org.example.springboot.entity.StockOut;
import org.example.springboot.mapper.ProductMapper;
import org.example.springboot.mapper.StockInMapper;
import org.example.springboot.mapper.StockOutMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);

    @Autowired
    private StockInMapper stockInMapper;

    @Autowired
    private StockOutMapper stockOutMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public Result<?> createStockIn(StockIn stockIn) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(stockIn.getProductId());
            if (product == null) {
                return Result.error("-1", "商品不存在");
            }

            // 计算总价
            stockIn.setTotalPrice(stockIn.getUnitPrice().multiply(new BigDecimal(stockIn.getQuantity())));

            // 更新商品库存
            product.setStock(product.getStock() + stockIn.getQuantity());
            productMapper.updateById(product);

            // 保存入库记录
            int result = stockInMapper.insert(stockIn);
            if (result > 0) {
                LOGGER.info("创建入库记录成功，入库ID：{}", stockIn.getId());
                return Result.success(stockIn);
            }
            return Result.error("-1", "创建入库记录失败");
        } catch (Exception e) {
            LOGGER.error("创建入库记录失败：{}", e.getMessage());
            return Result.error("-1", "创建入库记录失败：" + e.getMessage());
        }
    }

    @Transactional
    public Result<?> createStockOut(StockOut stockOut) {
        try {
            // 检查商品是否存在
            Product product = productMapper.selectById(stockOut.getProductId());
            if (product == null) {
                return Result.error("-1", "商品不存在");
            }

            // 检查库存是否足够
            if (product.getStock() < stockOut.getQuantity()) {
                return Result.error("-1", "库存不足");
            }

            // 计算总价
            stockOut.setTotalPrice(stockOut.getUnitPrice().multiply(new BigDecimal(stockOut.getQuantity())));

            // 更新商品库存
            product.setStock(product.getStock() - stockOut.getQuantity());
            productMapper.updateById(product);

            // 保存出库记录
            int result = stockOutMapper.insert(stockOut);
            if (result > 0) {
                LOGGER.info("创建出库记录成功，出库ID：{}", stockOut.getId());
                return Result.success(stockOut);
            }
            return Result.error("-1", "创建出库记录失败");
        } catch (Exception e) {
            LOGGER.error("创建出库记录失败：{}", e.getMessage());
            return Result.error("-1", "创建出库记录失败：" + e.getMessage());
        }
    }

    // 获取入库记录列表
    public Page<StockIn> getStockInList(Long productId, String supplier, Integer status, Long operatorId,
                                      Integer currentPage, Integer size) {
        LambdaQueryWrapper<StockIn> queryWrapper = new LambdaQueryWrapper<>();
        
        if (productId != null) {
            queryWrapper.eq(StockIn::getProductId, productId);
        }
        if (supplier != null) {
            queryWrapper.like(StockIn::getSupplier, supplier);
        }
        if (status != null) {
            queryWrapper.eq(StockIn::getStatus, status);
        }
        if (operatorId != null) {
            queryWrapper.eq(StockIn::getOperatorId, operatorId);
        }

        queryWrapper.orderByDesc(StockIn::getId);

        Page<StockIn> page = new Page<>(currentPage, size);
        Page<StockIn> result = stockInMapper.selectPage(page, queryWrapper);

        // 填充关联信息
        result.getRecords().forEach(stockIn -> {
            stockIn.setProduct(productMapper.selectById(stockIn.getProductId()));
            stockIn.setOperator(userMapper.selectById(stockIn.getOperatorId()));
        });

        return result;
    }

    // 获取出库记录列表
    public Page<StockOut> getStockOutList(Long productId, Integer type, Integer status, Long operatorId,
                                        String customerName, String orderNo,
                                        Integer currentPage, Integer size) {
        LambdaQueryWrapper<StockOut> queryWrapper = new LambdaQueryWrapper<>();
        
        if (productId != null) {
            queryWrapper.eq(StockOut::getProductId, productId);
        }
        if (type != null) {
            queryWrapper.eq(StockOut::getType, type);
        }
        if (status != null) {
            queryWrapper.eq(StockOut::getStatus, status);
        }
        if (operatorId != null) {
            queryWrapper.eq(StockOut::getOperatorId, operatorId);
        }
        if (customerName != null) {
            queryWrapper.like(StockOut::getCustomerName, customerName);
        }
        if (orderNo != null) {
            queryWrapper.eq(StockOut::getOrderNo, orderNo);
        }

        queryWrapper.orderByDesc(StockOut::getId);

        Page<StockOut> page = new Page<>(currentPage, size);
        Page<StockOut> result = stockOutMapper.selectPage(page, queryWrapper);

        // 填充关联信息
        result.getRecords().forEach(stockOut -> {
            stockOut.setProduct(productMapper.selectById(stockOut.getProductId()));
            stockOut.setOperator(userMapper.selectById(stockOut.getOperatorId()));
        });

        return result;
    }

    // 作废入库记录
    @Transactional
    public Result<?> invalidateStockIn(Long id) {
        try {
            StockIn stockIn = stockInMapper.selectById(id);
            if (stockIn == null) {
                return Result.error("-1", "入库记录不存在");
            }

            if (stockIn.getStatus() == 0) {
                return Result.error("-1", "该记录已作废");
            }

            // 更新商品库存
            Product product = productMapper.selectById(stockIn.getProductId());
            if (product != null) {
                product.setStock(product.getStock() - stockIn.getQuantity());
                productMapper.updateById(product);
            }

            // 更新入库记录状态
            stockIn.setStatus(0);
            stockInMapper.updateById(stockIn);

            return Result.success();
        } catch (Exception e) {
            LOGGER.error("作废入库记录失败：{}", e.getMessage());
            return Result.error("-1", "作废入库记录失败：" + e.getMessage());
        }
    }

    // 作废出库记录
    @Transactional
    public Result<?> invalidateStockOut(Long id) {
        try {
            StockOut stockOut = stockOutMapper.selectById(id);
            if (stockOut == null) {
                return Result.error("-1", "出库记录不存在");
            }

            if (stockOut.getStatus() == 0) {
                return Result.error("-1", "该记录已作废");
            }

            // 更新商品库存
            Product product = productMapper.selectById(stockOut.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + stockOut.getQuantity());
                productMapper.updateById(product);
            }

            // 更新出库记录状态
            stockOut.setStatus(0);
            stockOutMapper.updateById(stockOut);

            return Result.success();
        } catch (Exception e) {
            LOGGER.error("作废出库记录失败：{}", e.getMessage());
            return Result.error("-1", "作废出库记录失败：" + e.getMessage());
        }
    }

    // 删除入库记录
    @Transactional
    public Result<?> deleteStockIn(Long id) {
        try {
            StockIn stockIn = stockInMapper.selectById(id);
            if (stockIn == null) {
                return Result.error("-1", "入库记录不存在");
            }

            // 如果记录状态为正常，需要先作废（减少库存）
            if (stockIn.getStatus() == 1) {
                Result<?> invalidateResult = invalidateStockIn(id);
                if (!invalidateResult.getCode().equals("0")) {
                    return invalidateResult;
                }
            }

            // 删除记录
            int result = stockInMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除入库记录成功，入库ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除入库记录失败");
        } catch (Exception e) {
            LOGGER.error("删除入库记录失败：{}", e.getMessage());
            return Result.error("-1", "删除入库记录失败：" + e.getMessage());
        }
    }

    // 删除出库记录
    @Transactional
    public Result<?> deleteStockOut(Long id) {
        try {
            StockOut stockOut = stockOutMapper.selectById(id);
            if (stockOut == null) {
                return Result.error("-1", "出库记录不存在");
            }

            // 如果记录状态为正常，需要先作废（恢复库存）
            if (stockOut.getStatus() == 1) {
                Result<?> invalidateResult = invalidateStockOut(id);
                if (!invalidateResult.getCode().equals("0")) {
                    return invalidateResult;
                }
            }

            // 删除记录
            int result = stockOutMapper.deleteById(id);
            if (result > 0) {
                LOGGER.info("删除出库记录成功，出库ID：{}", id);
                return Result.success();
            }
            return Result.error("-1", "删除出库记录失败");
        } catch (Exception e) {
            LOGGER.error("删除出库记录失败：{}", e.getMessage());
            return Result.error("-1", "删除出库记录失败：" + e.getMessage());
        }
    }
} 