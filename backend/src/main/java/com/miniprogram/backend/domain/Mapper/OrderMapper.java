package com.miniprogram.backend.domain.Mapper;

import com.miniprogram.backend.domain.Entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 MyBatis Mapper
 * 用于复杂查询和批量操作
 */
@Mapper
public interface OrderMapper {
    
    /**
     * 根据时间范围查询订单（分页）
     */
    List<Order> findByTimeRangeWithPage(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("status") String status,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);
    
    /**
     * 统计时间范围内的订单数量
     */
    long countByTimeRange(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("status") String status);
    
    /**
     * 查询用户订单统计信息
     */
    OrderStats findOrderStatsByUserId(@Param("userId") Long userId);
    
    /**
     * 订单统计结果映射
     */
    class OrderStats {
        private Long totalOrders;
        private Long pendingPayment;
        private Long paid;
        private Long delivered;
        private Long completed;
        private Long cancelled;
        
        // Getters and Setters
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getPendingPayment() { return pendingPayment; }
        public void setPendingPayment(Long pendingPayment) { this.pendingPayment = pendingPayment; }
        public Long getPaid() { return paid; }
        public void setPaid(Long paid) { this.paid = paid; }
        public Long getDelivered() { return delivered; }
        public void setDelivered(Long delivered) { this.delivered = delivered; }
        public Long getCompleted() { return completed; }
        public void setCompleted(Long completed) { this.completed = completed; }
        public Long getCancelled() { return cancelled; }
        public void setCancelled(Long cancelled) { this.cancelled = cancelled; }
    }
}
