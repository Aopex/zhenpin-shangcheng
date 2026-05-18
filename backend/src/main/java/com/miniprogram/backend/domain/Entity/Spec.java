package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;

/**
 * 规格名实体类
 * 对应数据库specs表
 */
@Entity
@Table(name = "specs")
public class Spec {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(nullable = false, length = 32)
    private String name; // 规格名（如"颜色"、"尺码"）
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    // 构造函数
    public Spec() {}
    
    public Spec(Long productId, String name, Integer sortOrder) {
        this.productId = productId;
        this.name = name;
        this.sortOrder = sortOrder;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
