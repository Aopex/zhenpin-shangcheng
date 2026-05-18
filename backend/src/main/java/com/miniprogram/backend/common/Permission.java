package com.miniprogram.backend.common;

/**
 * 权限枚举类
 * 定义系统中的各种权限
 */
public enum Permission {
    // 用户相关权限
    USER_VIEW("user:view", "查看用户信息"),
    USER_EDIT("user:edit", "编辑用户信息"),
    USER_DELETE("user:delete", "删除用户"),
    
    // 商品相关权限
    PRODUCT_VIEW("product:view", "查看商品"),
    PRODUCT_EDIT("product:edit", "编辑商品"),
    PRODUCT_DELETE("product:delete", "删除商品"),
    
    // 订单相关权限
    ORDER_VIEW("order:view", "查看订单"),
    ORDER_EDIT("order:edit", "编辑订单"),
    ORDER_CANCEL("order:cancel", "取消订单"),
    ORDER_SHIP("order:ship", "发货"),
    
    // 退款相关权限
    REFUND_VIEW("refund:view", "查看退款"),
    REFUND_APPROVE("refund:approve", "批准退款"),
    REFUND_REJECT("refund:reject", "拒绝退款"),
    
    // 分类相关权限
    CATEGORY_VIEW("category:view", "查看分类"),
    CATEGORY_EDIT("category:edit", "编辑分类"),
    CATEGORY_DELETE("category:delete", "删除分类"),
    
    // 轮播图相关权限
    BANNER_VIEW("banner:view", "查看轮播图"),
    BANNER_EDIT("banner:edit", "编辑轮播图"),
    BANNER_DELETE("banner:delete", "删除轮播图");
    
    private final String code;
    private final String description;
    
    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
