package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.common.UserContext;
import com.miniprogram.backend.domain.Entity.Refund;
import com.miniprogram.backend.domain.Service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 退款控制器
 */
@RestController
@RequestMapping("/api/refunds")
public class RefundController {
    
    @Autowired
    private RefundService refundService;
    
    /**
     * 申请退款
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> requestRefund(
            @RequestParam Long orderId,
            @RequestParam(required = false) String reason) {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        
        String refundNo = refundService.requestRefund(orderId, userId, reason);
        return ResponseEntity.ok(ApiResponse.success(refundNo));
    }
    
    /**
     * 获取用户的退款列表（分页）
     */
    @GetMapping("/my-refunds")
    public ResponseEntity<ApiResponse<PageResponse<Refund>>> getMyRefunds(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        PageResponse<Refund> refunds = refundService.getUserRefunds(userId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }
    
    /**
     * 根据订单ID获取退款记录
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<Refund>>> getRefundsByOrderId(@PathVariable Long orderId) {
        List<Refund> refunds = refundService.getRefundsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }
    
    /**
     * 获取退款详情
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<Refund>> getRefundDetail(@PathVariable Long refundId) {
        Refund refund = refundService.getRefundDetail(refundId);
        return ResponseEntity.ok(ApiResponse.success(refund));
    }
    
    /**
     * 批准退款（管理员功能）
     */
    @RequireAdmin
    @PostMapping("/{refundId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRefund(@PathVariable Long refundId) {
        Long handlerId = UserContext.getCurrentUserId();
        refundService.approveRefund(refundId, handlerId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 拒绝退款（管理员功能）
     */
    @RequireAdmin
    @PostMapping("/{refundId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRefund(
            @PathVariable Long refundId,
            @RequestParam String reason) {
        Long handlerId = UserContext.getCurrentUserId();
        refundService.rejectRefund(refundId, reason, handlerId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
