package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类
 * 对应数据库order_items表
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "sku_id")
    private Long skuId; // 关联SKU
    
    @Column(name = "product_no", nullable = false, length = 32)
    private String productNo; // 商品编号（冗余快照）
    
    @Column(nullable = false, length = 256)
    private String title; // 商品标题（下单时快照）
    
    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl; // 商品图片（下单时快照）
    
    @Column(name = "spec_text", length = 128)
    private String specText; // 规格描述（如"朱砂红,M"）
    
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price; // 下单时单价
    
    @Column(nullable = false)
    private Integer quantity; // 购买数量
    
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal subtotal; // 小计金额
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 构造函数
    public OrderItem() {}
    
    public OrderItem(Long orderId, Long productId, Long skuId, String productNo, String title,
                     String imageUrl, String specText, java.math.BigDecimal price, Integer quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.skuId = skuId;
        this.productNo = productNo;
        this.title = title;
        this.imageUrl = imageUrl;
        this.specText = specText;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price != null ? price.multiply(new java.math.BigDecimal(quantity)) : java.math.BigDecimal.ZERO;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
    
    public String getProductNo() {
        return productNo;
    }
    
    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getSpecText() {
        return specText;
    }
    
    public void setSpecText(String specText) {
        this.specText = specText;
    }
    
    public java.math.BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.subtotal = this.price != null ? this.price.multiply(new java.math.BigDecimal(quantity)) : java.math.BigDecimal.ZERO;
    }
    
    public java.math.BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(java.math.BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
