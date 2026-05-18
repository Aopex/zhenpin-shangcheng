package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * 购物车项实体类
 * 对应数据库cart_items表
 */
@Entity
@Table(name = "cart_items")
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "sku_id")
    private Long skuId; // 关联SKU
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    @Column(name = "checked", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean checked = true; // true-选中, false-未选中（数据库字段为TINYINT）
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 构造函数
    public CartItem() {}
    
    public CartItem(Long userId, Long productId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public CartItem(Long userId, Long productId, Long skuId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.skuId = skuId;
        this.quantity = quantity;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Long getSkuId() {
        return skuId;
    }
    
    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Boolean getChecked() {
        return checked;
    }
    
    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 获取选中状态（别名方法，用于Mapper映射）
     */
    public Boolean getSelected() {
        return checked;
    }
    
    /**
     * 设置选中状态（别名方法，用于Mapper映射）
     */
    public void setSelected(Boolean selected) {
        this.checked = selected;
    }
    
    /**
     * 获取创建时间（别名方法，用于DTO映射）
     */
    public LocalDateTime getCreateTime() {
        return createdAt;
    }
    
    /**
     * 获取更新时间（别名方法，用于DTO映射）
     */
    public LocalDateTime getUpdateTime() {
        return updatedAt;
    }
}
