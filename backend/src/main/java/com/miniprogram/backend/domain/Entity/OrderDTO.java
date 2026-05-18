package com.miniprogram.backend.domain.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单DTO类
 * 包含订单信息和订单项列表
 */
public class OrderDTO {
    
    private Long id;
    private String orderNo;
    private Long userId;
    private Integer totalCount; // 商品总件数
    private BigDecimal totalAmount;
    private BigDecimal actualAmount;
    private String status; // UNPAID-待付款 PAID-已付款 SHIPPED-已发货 FINISHED-已完成 CANCELLED-已取消 DELETED-已删除
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime completionTime;
    private LocalDateTime cancellationTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 订单项列表
    private List<OrderItem> orderItems;
    
    // 订单项数量（用于列表展示）
    private Integer itemCount;
    
    // 构造函数
    public OrderDTO() {}
    
    public OrderDTO(Order order) {
        this.id = order.getId();
        this.orderNo = order.getOrderNo();
        this.userId = order.getUserId();
        this.totalCount = order.getTotalCount();
        this.totalAmount = order.getTotalAmount();
        this.actualAmount = order.getActualAmount();
        this.status = order.getStatus();
        this.receiverName = order.getReceiverName();
        this.receiverPhone = order.getReceiverPhone();
        this.receiverAddress = order.getReceiverAddress();
        this.remark = order.getRemark();
        this.paymentTime = order.getPayTime();
        this.deliveryTime = order.getShipTime();
        this.completionTime = order.getReceiveTime();
        this.cancellationTime = order.getCancelTime();
        this.createTime = order.getCreatedAt();
        this.updateTime = order.getUpdatedAt();
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
    
    public Integer getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getActualAmount() {
        return actualAmount;
    }
    
    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
    
    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }
    
    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
    
    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }
    
    public LocalDateTime getCancellationTime() {
        return cancellationTime;
    }
    
    public void setCancellationTime(LocalDateTime cancellationTime) {
        this.cancellationTime = cancellationTime;
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
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    /**
     * 设置订单项列表（别名方法）
     */
    public void setItems(List<OrderItem> items) {
        this.orderItems = items;
    }
    
    public Integer getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
    
    /**
     * 获取订单状态描述
     */
    public String getStatusDescription() {
        return com.miniprogram.backend.common.OrderStatus.getDescription(this.status);
    }
}
