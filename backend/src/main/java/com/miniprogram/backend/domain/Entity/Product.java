package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_no", nullable = false, unique = true, length = 32)
    private String productNo; // 商品编号（如 P101、D101）
    
    @Column(nullable = false, length = 256)
    private String title; // 商品标题
    
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price; // 售价
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private java.math.BigDecimal originalPrice; // 原价（划线价）
    
    @Column(name = "category_id")
    private Long categoryId; // 所属分类
    
    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl; // 商品图片URL（如：/pics/iphone15.jpg）
    
    @Column(nullable = false)
    private Integer stock = 0; // 总库存
    
    @Column(nullable = false)
    private Integer sales = 0; // 累计销量
    
    @Column(nullable = false)
    private Integer status = 1; // 状态：0-下架 1-上架
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0; // 排序权重
    
    @Column(columnDefinition = "TEXT")
    private String description; // 商品描述（富文本）
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Product() {}
    
    public Product(String productNo, String title, java.math.BigDecimal price, String imageUrl) {
        this.productNo = productNo;
        this.title = title;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    
    public Product(String productNo, String title, java.math.BigDecimal price, java.math.BigDecimal originalPrice,
                   Long categoryId, String imageUrl, Integer stock, Integer sales) {
        this.productNo = productNo;
        this.title = title;
        this.price = price;
        this.originalPrice = originalPrice;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.sales = sales;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public java.math.BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(java.math.BigDecimal price) {
        this.price = price;
    }
    
    public java.math.BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(java.math.BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Integer getSales() {
        return sales;
    }
    
    public void setSales(Integer sales) {
        this.sales = sales;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
}
