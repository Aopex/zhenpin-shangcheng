package com.miniprogram.backend.service;

import com.miniprogram.backend.domain.DAO.OrderRepository;
import com.miniprogram.backend.domain.Entity.Order;
import com.miniprogram.backend.domain.Service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时自动取消定时任务
 * 定期扫描未支付订单，超过30分钟自动取消并恢复库存
 * 
 * 功能说明：
 * 1. 每5分钟执行一次，扫描创建时间超过30分钟的未支付订单
 * 2. 使用分布式锁防止多实例重复执行
 * 3. 自动调用cancelOrder方法取消订单并恢复库存
 * 4. 记录详细的执行日志，便于问题排查
 */
@Slf4j
@Component
public class OrderTimeoutService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 订单超时时间（分钟），默认30分钟
     * 可通过配置文件 order.timeout.minutes 进行配置
     */
    @Value("${order.timeout.minutes:30}")
    private int timeoutMinutes;
    
    /**
     * 定时任务执行间隔（毫秒），默认5分钟
     * 可通过配置文件 order.timeout.check-interval 进行配置
     */
    @Value("${order.timeout.check-interval:300000}")
    private long checkInterval;
    
    /**
     * 定时执行订单超时取消任务
     * 扫描创建时间超过指定时间的未支付订单，自动取消
     * 使用分布式锁防止多实例重复执行
     * 
     * 执行流程：
     * 1. 尝试获取分布式锁（有效期2分钟）
     * 2. 查询超时未支付订单（超过配置的超时时间）
     * 3. 逐个取消订单并恢复库存
     * 4. 释放分布式锁
     * 
     * 配置说明：
     * - order.timeout.minutes: 订单超时时间（分钟），默认30
     * - order.timeout.check-interval: 检查间隔（毫秒），默认300000（5分钟）
     */
    @Scheduled(fixedRateString = "${order.timeout.check-interval:300000}")
    public void cancelTimeoutOrders() {
        String lockKey = "order:timeout:lock";
        long startTime = System.currentTimeMillis();
        
        log.info("=== Order Timeout Cancellation Task Started ===");
        
        // 尝试获取分布式锁，有效期2分钟（小于定时任务间隔）
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 2, TimeUnit.MINUTES);
        
        if (!Boolean.TRUE.equals(locked)) {
            log.debug("Another instance is processing timeout orders, skipping this execution");
            return;
        }
        
        try {
            log.info("Acquired distributed lock, starting timeout order processing...");
            log.info("Configuration: timeout={} minutes, check interval={} ms", timeoutMinutes, checkInterval);
            
            // 计算超时时间点（根据配置的超时时间）
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
            log.debug("Timeout threshold: {} ({} minutes ago)", timeoutThreshold, timeoutMinutes);
            
            // 查询所有超过30分钟仍未支付的订单
            List<Order> timeoutOrders = orderRepository.findTimeoutUnpaidOrders(timeoutThreshold);
            
            if (timeoutOrders.isEmpty()) {
                log.info("No timeout unpaid orders found, task completed");
                return;
            }
            
            log.info("Found {} timeout unpaid orders, starting cancellation process...", timeoutOrders.size());
            
            int successCount = 0;
            int failCount = 0;
            List<Long> failedOrderIds = new java.util.ArrayList<>();
            
            for (Order order : timeoutOrders) {
                try {
                    // 调用取消订单方法（会自动恢复库存）
                    // userId传null表示系统自动取消，不进行权限验证
                    orderService.cancelOrder(order.getId(), null);
                    successCount++;
                    log.info("Auto-cancelled timeout order - orderId: {}, orderNo: {}, createdAt: {}", 
                             order.getId(), order.getOrderNo(), order.getCreatedAt());
                } catch (Exception e) {
                    failCount++;
                    failedOrderIds.add(order.getId());
                    log.error("Failed to cancel timeout order - orderId: {}, orderNo: {}, error: {}", 
                              order.getId(), order.getOrderNo(), e.getMessage(), e);
                }
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("=== Order Timeout Cancellation Task Completed ===");
            log.info("Total orders processed: {}, Success: {}, Failed: {}, Execution time: {}ms", 
                     timeoutOrders.size(), successCount, failCount, executionTime);
            
            if (failCount > 0) {
                log.warn("Failed order IDs: {}", failedOrderIds);
            }
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error in timeout order cancellation task after {}ms", executionTime, e);
        } finally {
            // 释放锁
            stringRedisTemplate.delete(lockKey);
            log.debug("Released distributed lock: {}", lockKey);
        }
    }
}
