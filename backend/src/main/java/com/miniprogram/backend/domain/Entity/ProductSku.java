package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU实体类
 * 对应数据库product_skus表
 */
@Entity
@Table(name = "product_skus")
public class ProductSku {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "sku_no", nullable = false, unique = true, length = 32)
    private String skuNo;
    
    @Column(name = "spec_values", nullable = false, length = 256)
    private String specValues; // 规格值组合（如 "朱砂红,M"）
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price; // SKU级别定价（为空则取商品价）
    
    @Column(nullable = false)
    private Integer stock = 0;
    
    @Column(name = "image_url", length = 512)
    private String imageUrl;
    
    @Column(nullable = false)
    private Integer status = 1; // 0-禁用 1-启用
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 构造函数
    public ProductSku() {}
    
    public ProductSku(Long productId, String skuNo, String specValues, Integer stock) {
        this.productId = productId;
        this.skuNo = skuNo;
        this.specValues = specValues;
        this.stock = stock;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getSkuNo() {
        return skuNo;
    }
    
    public void setSkuNo(String skuNo) {
        this.skuNo = skuNo;
    }
    
    public String getSpecValues() {
        return specValues;
    }
    
    public void setSpecValues(String specValues) {
        this.specValues = specValues;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
