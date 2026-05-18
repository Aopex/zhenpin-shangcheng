package com.miniprogram.backend.domain.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项DTO类
 * 包含购物车项信息和关联的产品信息
 */
public class CartItemDTO {
    
    private Long id;
    private Long userId;
    private Long productId;
    private Long skuId; // SKU ID（可选）
    private Integer quantity;
    private Boolean selected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 产品信息（关联查询）
    private String productName;
    private BigDecimal productPrice;
    private String productImage;
    private Integer productStock;
    
    // 构造函数
    public CartItemDTO() {}
    
    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.userId = cartItem.getUserId();
        this.productId = cartItem.getProductId();
        this.skuId = cartItem.getSkuId();
        this.quantity = cartItem.getQuantity();
        this.selected = cartItem.getSelected();
        this.createTime = cartItem.getCreateTime();
        this.updateTime = cartItem.getUpdateTime();
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
    
    public Boolean getSelected() {
        return selected;
    }
    
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public BigDecimal getProductPrice() {
        return productPrice;
    }
    
    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }
    
    public String getProductImage() {
        return productImage;
    }
    
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }
    
    public Integer getProductStock() {
        return productStock;
    }
    
    public void setProductStock(Integer productStock) {
        this.productStock = productStock;
    }
    
    /**
     * 计算小计金额
     */
    public BigDecimal getSubtotal() {
        if (productPrice != null && quantity != null) {
            return productPrice.multiply(new BigDecimal(quantity));
        }
        return BigDecimal.ZERO;
    }
}
