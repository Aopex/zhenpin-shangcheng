package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单项 JPA Repository
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * 根据订单ID查询订单项列表
     */
    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(Long orderId);
    
    /**
     * 批量查询多个订单的订单项（优化N+1查询）
     */
    List<OrderItem> findByOrderIdIn(List<Long> orderIds);
    
    /**
     * 根据产品ID查询订单项列表
     */
    List<OrderItem> findByProductId(Long productId);
    
    /**
     * 根据SKU ID查询订单项列表
     */
    List<OrderItem> findBySkuId(Long skuId);
    
    /**
     * 删除订单的所有订单项
     */
    void deleteByOrderId(Long orderId);
}
