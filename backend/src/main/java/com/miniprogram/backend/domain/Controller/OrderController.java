package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.OrderStatus;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.common.UserContext;
import com.miniprogram.backend.domain.Entity.OrderActionRequest;
import com.miniprogram.backend.domain.Entity.CreateOrderRequest;
import com.miniprogram.backend.domain.Entity.OrderDTO;
import com.miniprogram.backend.domain.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单（从购物车结算）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        // 从JWT Token中获取用户ID，确保安全性
        Long userId = UserContext.getCurrentUserId();
        
        OrderDTO order = orderService.createOrderFromCart(
                userId,  // 使用Token中的userId，忽略请求体中的userId
                request.getCartItemIds(), 
                request.getAddressId(), 
                request.getRemark());
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetail(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        OrderDTO order = orderService.getOrderDetail(orderId);
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).body(ApiResponse.error(403, "You don't have permission to access this order"));
        }
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    /**
     * 根据订单号获取订单
     */
    @GetMapping("/no/{orderNo}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderByOrderNo(@PathVariable String orderNo) {
        OrderDTO order = orderService.getOrderByOrderNo(orderNo);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    /**
     * 获取当前用户订单列表（分页）
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getUserOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        PageResponse<OrderDTO> orders = orderService.getUserOrders(userId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    /**
     * 获取当前用户待付款订单
     */
    @GetMapping("/my-orders/unpaid")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getUnpaidOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getCurrentUserId();
        PageResponse<OrderDTO> orders = orderService.getUserOrders(userId, OrderStatus.UNPAID, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    /**
     * 获取当前用户待发货订单（已支付）
     */
    @GetMapping("/my-orders/unshipped")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getUnshippedOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getCurrentUserId();
        PageResponse<OrderDTO> orders = orderService.getUserOrders(userId, OrderStatus.PAID, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    /**
     * 获取当前用户待收货订单（已发货）
     */
    @GetMapping("/my-orders/unreceived")
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getUnreceivedOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getCurrentUserId();
        PageResponse<OrderDTO> orders = orderService.getUserOrders(userId, OrderStatus.SHIPPED, page, size);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long orderId) {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 支付订单
     */
    @PutMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<Void>> payOrder(@PathVariable Long orderId) {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        orderService.payOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 提醒发货（课设模拟：允许用户在演示流程中触发自动发货）
     */
    @PutMapping("/{orderId}/remind-ship")
    public ResponseEntity<ApiResponse<Void>> remindShip(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        orderService.mockDeliverOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 发货（管理员功能）
     */
    @RequireAdmin  // 需要管理员权限
    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse<Void>> deliverOrder(@PathVariable Long orderId) {
        orderService.deliverOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 确认收货
     */
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReceipt(@PathVariable Long orderId) {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        orderService.confirmReceipt(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 删除订单（软删除）
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long orderId) {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // ==================== 订单统计接口 ====================
    
    /**
     * 获取当前用户订单统计信息
     */
    @GetMapping("/my-orders/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStats() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Object> stats = orderService.getOrderStats(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
