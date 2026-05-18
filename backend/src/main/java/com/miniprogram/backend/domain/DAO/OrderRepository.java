package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 订单 JPA Repository
 * 用于简单的增删改查操作
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 根据订单号查询订单
     */
    Optional<Order> findByOrderNo(String orderNo);
    
    /**
     * 根据用户ID查询订单列表（分页）
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status <> 'DELETED' ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和状态查询订单列表（分页）
     */
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);
    
    /**
     * 根据状态查询订单列表（分页）
     */
    Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * 查询用户的所有订单
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status <> 'DELETED' ORDER BY o.createdAt DESC")
    List<Order> findOrdersByUserId(@Param("userId") Long userId);
    
    /**
     * 更新订单状态
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :orderId")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);
    
    /**
     * 更新订单为已支付状态
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'PAID', o.payTime = CURRENT_TIMESTAMP WHERE o.id = :orderId")
    void markAsPaid(@Param("orderId") Long orderId);
    
    /**
     * 更新订单为已发货状态
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'SHIPPED', o.shipTime = CURRENT_TIMESTAMP WHERE o.id = :orderId")
    void markAsShipped(@Param("orderId") Long orderId);
    
    /**
     * 更新订单为已完成状态
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'FINISHED', o.receiveTime = CURRENT_TIMESTAMP WHERE o.id = :orderId")
    void markAsCompleted(@Param("orderId") Long orderId);
    
    /**
     * 更新订单为已取消状态
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'CANCELLED', o.cancelTime = CURRENT_TIMESTAMP WHERE o.id = :orderId")
    void markAsCancelled(@Param("orderId") Long orderId);
    
    /**
     * 查询超时未支付的订单列表
     * 用于定时任务自动取消订单
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'UNPAID' AND o.createdAt < :threshold")
    List<Order> findTimeoutUnpaidOrders(@Param("threshold") java.time.LocalDateTime threshold);
    
    /**
     * 软删除订单（更新isDeleted标记）
     * 注意：如果Order实体没有isDeleted字段，可以改为硬删除或状态标记
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = 'DELETED' WHERE o.id = :orderId")
    void softDelete(@Param("orderId") Long orderId);
}
