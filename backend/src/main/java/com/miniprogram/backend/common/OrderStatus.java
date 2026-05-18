package com.miniprogram.backend.common;

/**
 * 订单状态常量类
 * 统一管理订单状态，避免硬编码字符串
 */
public class OrderStatus {
    
    /**
     * 待支付
     */
    public static final String UNPAID = "UNPAID";
    
    /**
     * 已支付（待发货）
     */
    public static final String PAID = "PAID";
    
    /**
     * 已发货（待收货）
     */
    public static final String SHIPPED = "SHIPPED";
    
    /**
     * 已完成
     */
    public static final String FINISHED = "FINISHED";
    
    /**
     * 已取消
     */
    public static final String CANCELLED = "CANCELLED";
    
    /**
     * 已删除（软删除）
     */
    public static final String DELETED = "DELETED";
    
    /**
     * 获取状态描述
     */
    public static String getDescription(String status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case UNPAID:
                return "待支付";
            case PAID:
                return "已支付";
            case SHIPPED:
                return "已发货";
            case FINISHED:
                return "已完成";
            case CANCELLED:
                return "已取消";
            case DELETED:
                return "已删除";
            default:
                return "未知状态";
        }
    }
    
    /**
     * 检查状态是否可以取消
     */
    public static boolean canCancel(String status) {
        return UNPAID.equals(status);
    }
    
    /**
     * 检查状态是否可以支付
     */
    public static boolean canPay(String status) {
        return UNPAID.equals(status);
    }
    
    /**
     * 检查状态是否可以发货
     */
    public static boolean canShip(String status) {
        return PAID.equals(status);
    }
    
    /**
     * 检查状态是否可以确认收货
     */
    public static boolean canConfirm(String status) {
        return SHIPPED.equals(status);
    }
}
