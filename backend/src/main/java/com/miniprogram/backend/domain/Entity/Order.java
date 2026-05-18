package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 对应数据库orders表
 */
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 16)
    private String status; // UNPAID-待付款 PAID-已付款 SHIPPED-已发货 FINISHED-已完成 CANCELLED-已取消 DELETED-已删除
    
    @Column(name = "total_count", nullable = false)
    private Integer totalCount; // 商品总件数
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal totalAmount; // 实付金额
    
    @Column(name = "actual_amount", precision = 10, scale = 2)
    private java.math.BigDecimal actualAmount; // 实际支付金额（优惠后）
    
    @Column(name = "freight_amount", precision = 10, scale = 2)
    private java.math.BigDecimal freightAmount = java.math.BigDecimal.ZERO; // 运费
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO; // 优惠金额
    
    @Column(name = "address_snapshot", columnDefinition = "JSON")
    private String addressSnapshot; // 下单时地址快照
    
    @Column(length = 256)
    private String remark; // 买家备注
    
    @Column(name = "receiver_name", length = 32)
    private String receiverName; // 收货人姓名
    
    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone; // 收货人电话
    
    @Column(name = "receiver_address", length = 512)
    private String receiverAddress; // 收货地址
    
    @Column(name = "pay_time")
    private LocalDateTime payTime; // 支付时间
    
    @Column(name = "ship_time")
    private LocalDateTime shipTime; // 发货时间
    
    @Column(name = "receive_time")
    private LocalDateTime receiveTime; // 收货时间
    
    @Column(name = "finish_time")
    private LocalDateTime finishTime; // 完成时间
    
    @Column(name = "cancel_time")
    private LocalDateTime cancelTime; // 取消时间
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Order() {}
    
    public Order(String orderNo, Long userId, String status, Integer totalCount,
                 java.math.BigDecimal totalAmount, String addressSnapshot) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.status = status;
        this.totalCount = totalCount;
        this.totalAmount = totalAmount;
        this.addressSnapshot = addressSnapshot;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNo() {
        return orderNo;
    }
    
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public java.math.BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(java.math.BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public java.math.BigDecimal getActualAmount() {
        return actualAmount;
    }
    
    public void setActualAmount(java.math.BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }
    
    public java.math.BigDecimal getFreightAmount() {
        return freightAmount;
    }
    
    public void setFreightAmount(java.math.BigDecimal freightAmount) {
        this.freightAmount = freightAmount;
    }
    
    public java.math.BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(java.math.BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public String getAddressSnapshot() {
        return addressSnapshot;
    }
    
    public void setAddressSnapshot(String addressSnapshot) {
        this.addressSnapshot = addressSnapshot;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
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
    
    public String getReceiverAddress() {
        return receiverAddress;
    }
    
    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }
    
    public LocalDateTime getPayTime() {
        return payTime;
    }
    
    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }
    
    public LocalDateTime getShipTime() {
        return shipTime;
    }
    
    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }
    
    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }
    
    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    public LocalDateTime getFinishTime() {
        return finishTime;
    }
    
    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }
    
    public LocalDateTime getCancelTime() {
        return cancelTime;
    }
    
    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
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
