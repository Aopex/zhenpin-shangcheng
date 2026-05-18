package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 * 对应数据库banners表
 */
@Entity
@Table(name = "banners")
public class Banner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 64)
    private String title; // 轮播图标题（管理用）
    
    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;
    
    @Column(name = "link_type")
    private Integer linkType = 1; // 跳转类型：1-商品详情 2-分类页 3-小程序页面 4-外部链接
    
    @Column(name = "link_value", length = 256)
    private String linkValue; // 跳转目标
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(nullable = false)
    private Integer status = 1; // 0-隐藏 1-显示
    
    @Column(name = "start_time")
    private LocalDateTime startTime; // 生效开始时间
    
    @Column(name = "end_time")
    private LocalDateTime endTime; // 生效结束时间
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 构造函数
    public Banner() {}
    
    public Banner(String title, String imageUrl, Integer linkType, String linkValue, Integer sortOrder) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.linkType = linkType;
        this.linkValue = linkValue;
        this.sortOrder = sortOrder;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Integer getLinkType() {
        return linkType;
    }
    
    public void setLinkType(Integer linkType) {
        this.linkType = linkType;
    }
    
    public String getLinkValue() {
        return linkValue;
    }
    
    public void setLinkValue(String linkValue) {
        this.linkValue = linkValue;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
