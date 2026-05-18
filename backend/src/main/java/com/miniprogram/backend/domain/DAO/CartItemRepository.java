package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 购物车 JPA Repository
 * 用于简单的增删改查操作
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 根据用户ID查询购物车项列表
     */
    List<CartItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 根据用户ID和产品ID查询购物车项
     */
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 根据用户ID、产品ID和SKU ID查询购物车项
     */
    Optional<CartItem> findByUserIdAndProductIdAndSkuId(Long userId, Long productId, Long skuId);
    
    /**
     * 根据用户ID查询选中的购物车项
     */
    List<CartItem> findByUserIdAndCheckedTrue(Long userId);
    
    /**
     * 根据用户ID和选中状态查询购物车项（别名方法）
     */
    default List<CartItem> findByUserIdAndSelectedTrue(Long userId) {
        return findByUserIdAndCheckedTrue(userId);
    }
    
    /**
     * 删除用户的购物车项
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除用户的指定产品购物车项
     */
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 根据产品ID删除所有购物车项（用于商品下架时清理）
     */
    void deleteByProductId(Long productId);
    
    /**
     * 更新购物车项数量
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.quantity = :quantity WHERE c.id = :id")
    void updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 更新购物车项选中状态
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.checked = :checked WHERE c.id = :id")
    void updateChecked(@Param("id") Long id, @Param("checked") Boolean checked);
    
    /**
     * 批量更新用户购物车项的选中状态
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.checked = :checked WHERE c.userId = :userId")
    void updateCheckedByUserId(@Param("userId") Long userId, @Param("checked") Boolean checked);
    
    /**
     * 批量更新用户购物车项的选中状态（别名方法）
     */
    @Modifying
    @Query("UPDATE CartItem c SET c.checked = :selected WHERE c.userId = :userId")
    void updateSelectedByUserId(@Param("userId") Long userId, @Param("selected") Boolean selected);
}
