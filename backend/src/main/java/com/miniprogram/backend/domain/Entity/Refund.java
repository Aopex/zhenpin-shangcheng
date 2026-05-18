package com.miniprogram.backend.domain.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款实体类
 * 对应数据库refunds表
 */
@Entity
@Table(name = "refunds")
public class Refund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "refund_no", nullable = false, unique = true, length = 32)
    private String refundNo; // 退款单号
    
    @Column(name = "order_id", nullable = false)
    private Long orderId; // 关联订单ID
    
    @Column(name = "order_no", nullable = false, length = 32)
    private String orderNo; // 订单号（冗余）
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // 用户ID
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount; // 退款金额
    
    @Column(length = 500)
    private String reason; // 退款原因
    
    @Column(nullable = false, length = 16)
    private String status; // 退款状态：PENDING-待审核 APPROVED-已批准 REJECTED-已拒绝 REFUNDED-已退款
    
    @Column(name = "reject_reason", length = 500)
    private String rejectReason; // 拒绝原因
    
    @Column(name = "refund_time")
    private LocalDateTime refundTime; // 退款时间
    
    @Column(name = "handle_time")
    private LocalDateTime handleTime; // 处理时间
    
    @Column(name = "handler_id")
    private Long handlerId; // 处理人ID（管理员）
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Refund() {}
    
    public Refund(String refundNo, Long orderId, String orderNo, Long userId, 
                  BigDecimal refundAmount, String reason) {
        this.refundNo = refundNo;
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = "PENDING";
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRefundNo() {
        return refundNo;
    }
    
    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRejectReason() {
        return rejectReason;
    }
    
    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
    
    public LocalDateTime getRefundTime() {
        return refundTime;
    }
    
    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }
    
    public LocalDateTime getHandleTime() {
        return handleTime;
    }
    
    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }
    
    public Long getHandlerId() {
        return handlerId;
    }
    
    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
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
