package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 用户收货地址 DTO 类
 * 用于 API 数据传输和参数校验
 */
public class UserAddressDTO {
    
    private Long id;
    
    private Long userId;
    
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;
    
    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;
    
    @NotBlank(message = "省份不能为空")
    private String province;
    
    @NotBlank(message = "城市不能为空")
    private String city;
    
    @NotBlank(message = "区县不能为空")
    private String district;
    
    @NotBlank(message = "详细地址不能为空")
    private String address;
    
    private Boolean isDefault = false;
    
    private Boolean isDeleted = false;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 构造函数
    public UserAddressDTO() {}
    
    public UserAddressDTO(UserAddress address) {
        this.id = address.getId();
        this.userId = address.getUserId();
        this.receiverName = address.getReceiverName();
        this.receiverPhone = address.getReceiverPhone();
        this.province = address.getProvince();
        this.city = address.getCity();
        this.district = address.getDistrict();
        this.address = address.getAddress();
        this.isDefault = address.getIsDefault() != null && address.getIsDefault() == 1;
        this.isDeleted = address.getIsDeleted() != null && address.getIsDeleted() == 1;
        this.createTime = address.getCreateTime();
        this.updateTime = address.getUpdateTime();
    }
    
    /**
     * 转换为 Entity
     */
    public UserAddress toEntity() {
        UserAddress address = new UserAddress();
        address.setId(this.id);
        address.setUserId(this.userId);
        address.setReceiverName(this.receiverName);
        address.setReceiverPhone(this.receiverPhone);
        address.setProvince(this.province);
        address.setCity(this.city);
        address.setDistrict(this.district);
        address.setAddress(this.address);
        address.setIsDefault(this.isDefault != null && this.isDefault ? 1 : 0);
        address.setIsDeleted(this.isDeleted != null && this.isDeleted ? 1 : 0);
        return address;
    }
    
    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        return province + city + district + " " + address;
    }
    
    // Getter 和 Setter
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
    
    public String getReceiverName() {
        return receiverName;
    }
    
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    
    public String getReceiverPhone() {
        return receiverPhone;
    }
    
    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
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
}
