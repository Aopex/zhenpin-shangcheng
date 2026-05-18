package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * 用户收货地址实体类
 * 对应数据库user_addresses表
 * 支持一个用户多个收货地址
 */
@Entity
@Table(name = "addresses")
public class UserAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 32)
    private String name; // 收货人姓名
    
    @Column(nullable = false, length = 20)
    private String phone; // 联系电话
    
    @Column(nullable = false, length = 32)
    private String province; // 省
    
    @Column(nullable = false, length = 32)
    private String city; // 市
    
    @Column(nullable = false, length = 32)
    private String district; // 区/县
    
    @Column(nullable = false, length = 256)
    private String detail; // 详细地址
    
    @Column(name = "is_default")
    private Integer isDefault = 0; // 是否默认地址：0-否 1-是
    
    @Column(name = "is_deleted")
    private Integer isDeleted = 0; // 是否删除：0-未删除 1-已删除
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserAddress() {}
    
    public UserAddress(Long userId, String name, String phone, 
                      String province, String city, String district, String detail) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String detail) {
        this.detail = detail;
    }
    
    public Integer getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }
    
    public Integer getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
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
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        return province + city + district + " " + detail;
    }
    
    /**
     * 获取收货人姓名（别名方法，用于DTO映射）
     */
    public String getReceiverName() {
        return name;
    }
    
    /**
     * 设置收货人姓名（别名方法，用于DTO映射）
     */
    public void setReceiverName(String receiverName) {
        this.name = receiverName;
    }
    
    /**
     * 获取收货人电话（别名方法，用于DTO映射）
     */
    public String getReceiverPhone() {
        return phone;
    }
    
    /**
     * 设置收货人电话（别名方法，用于DTO映射）
     */
    public void setReceiverPhone(String receiverPhone) {
        this.phone = receiverPhone;
    }
    
    /**
     * 获取详细地址（别名方法，用于DTO映射）
     */
    public String getAddress() {
        return detail;
    }
    
    /**
     * 设置详细地址（别名方法，用于DTO映射）
     */
    public void setAddress(String address) {
        this.detail = address;
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
