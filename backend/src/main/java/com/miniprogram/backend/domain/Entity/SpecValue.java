package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;

/**
 * 规格值实体类
 * 对应数据库spec_values表
 */
@Entity
@Table(name = "spec_values")
public class SpecValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "spec_id", nullable = false)
    private Long specId;
    
    @Column(nullable = false, length = 32)
    private String value; // 规格值（如"朱砂红"、"M"）
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    // 构造函数
    public SpecValue() {}
    
    public SpecValue(Long specId, String value, Integer sortOrder) {
        this.specId = specId;
        this.value = value;
        this.sortOrder = sortOrder;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSpecId() {
        return specId;
    }
    
    public void setSpecId(Long specId) {
        this.specId = specId;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
