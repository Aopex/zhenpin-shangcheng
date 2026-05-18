package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.common.OrderStatus;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.domain.Entity.Order;
import com.miniprogram.backend.domain.Entity.OrderItem;
import com.miniprogram.backend.domain.Entity.Refund;
import com.miniprogram.backend.domain.DAO.OrderRepository;
import com.miniprogram.backend.domain.DAO.OrderItemRepository;
import com.miniprogram.backend.domain.DAO.RefundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款服务类
 * 处理订单退款相关业务逻辑
 */
@Slf4j
@Service
public class RefundService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private RefundRepository refundRepository;
    
    @Autowired
    private ProductService productService;
    
    /**
     * 申请退款
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 退款原因
     * @return 退款单号
     */
    @Transactional
    public String requestRefund(Long orderId, Long userId, String reason) {
        log.info("Requesting refund - orderId: {}, userId: {}, reason: {}", orderId, userId, reason);
        
        // 查询订单
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found with id: " + orderId));
        
        // 验证订单归属
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "You don't have permission to refund this order");
        }
        
        // 验证订单状态（只有已支付、已发货、已收货的订单可以退款）
        if (!OrderStatus.PAID.equals(order.getStatus()) && 
            !OrderStatus.SHIPPED.equals(order.getStatus()) && 
            !OrderStatus.FINISHED.equals(order.getStatus())) {
            throw new BusinessException(400, "Only paid, shipped or finished orders can be refunded");
        }
        
        // 检查是否已经申请过退款
        List<Refund> existingRefunds = refundRepository.findByOrderId(orderId);
        for (Refund refund : existingRefunds) {
            if (!"REJECTED".equals(refund.getStatus())) {
                throw new BusinessException(400, "A refund request already exists for this order");
            }
        }
        
        // 生成退款单号
        String refundNo = generateRefundNo();
        
        // 创建退款记录
        Refund refund = new Refund(
            refundNo,
            orderId,
            order.getOrderNo(),
            userId,
            order.getActualAmount() != null ? order.getActualAmount() : order.getTotalAmount(),
            reason
        );
        
        refundRepository.save(refund);
        
        log.info("Refund request created - refundNo: {}, orderId: {}", refundNo, orderId);
        return refundNo;
    }
    
    /**
     * 批准退款（管理员功能）
     * @param refundId 退款ID
     * @param handlerId 处理人ID
     */
    @Transactional
    public void approveRefund(Long refundId, Long handlerId) {
        log.info("Approving refund - refundId: {}, handlerId: {}", refundId, handlerId);
        
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(404, "Refund not found with id: " + refundId));
        
        if (!"PENDING".equals(refund.getStatus())) {
            throw new BusinessException(400, "Only pending refunds can be approved");
        }
        
        // 更新退款状态为已批准
        refundRepository.approveRefund(refundId, handlerId);
        
        // 恢复库存
        restoreStock(refund.getOrderId());
        
        // TODO: 调用微信支付退款API执行实际退款
        // 这里应该集成微信支付SDK进行实际退款操作
        
        // 临时实现：直接标记为已退款
        refundRepository.markAsRefunded(refundId);
        
        // 更新订单状态为已取消
        orderRepository.markAsCancelled(refund.getOrderId());
        
        log.info("Refund approved and processed - refundId: {}", refundId);
    }
    
    /**
     * 拒绝退款（管理员功能）
     * @param refundId 退款ID
     * @param rejectReason 拒绝原因
     * @param handlerId 处理人ID
     */
    @Transactional
    public void rejectRefund(Long refundId, String rejectReason, Long handlerId) {
        log.info("Rejecting refund - refundId: {}, handlerId: {}", refundId, handlerId);
        
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(404, "Refund not found with id: " + refundId));
        
        if (!"PENDING".equals(refund.getStatus())) {
            throw new BusinessException(400, "Only pending refunds can be rejected");
        }
        
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new BusinessException(400, "Reject reason cannot be empty");
        }
        
        refundRepository.rejectRefund(refundId, rejectReason, handlerId);
        
        log.info("Refund rejected - refundId: {}", refundId);
    }
    
    /**
     * 获取用户的退款列表（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<Refund> getUserRefunds(Long userId, Integer page, Integer pageSize) {
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        log.debug("Fetching refunds for user id: {} - page: {}, pageSize: {}", userId, validPage, validPageSize);
        
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<Refund> refundPage = refundRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return new PageResponse<>(validPage, validPageSize, refundPage.getTotalElements(), refundPage.getContent());
    }
    
    /**
     * 根据订单ID获取退款记录
     */
    @Transactional(readOnly = true)
    public List<Refund> getRefundsByOrderId(Long orderId) {
        log.debug("Fetching refunds for order id: {}", orderId);
        return refundRepository.findByOrderId(orderId);
    }
    
    /**
     * 获取退款详情
     */
    @Transactional(readOnly = true)
    public Refund getRefundDetail(Long refundId) {
        log.debug("Fetching refund detail - refundId: {}", refundId);
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(404, "Refund not found with id: " + refundId));
    }
    
    /**
     * 恢复订单商品库存
     */
    private void restoreStock(Long orderId) {
        log.info("Restoring stock for order: {}", orderId);
        
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        for (OrderItem item : orderItems) {
            try {
                productService.increaseStock(item.getProductId(), item.getQuantity());
                log.info("Restored stock for product: {}, quantity: {}", item.getProductId(), item.getQuantity());
            } catch (BusinessException e) {
                log.warn("Failed to restore stock for product: {}, error: {}", 
                         item.getProductId(), e.getMessage());
            }
        }
    }
    
    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        // 格式: RFD + 时间戳(13位) + UUID前8位(大写)
        return "RFD" + System.currentTimeMillis() + 
               java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
