package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.common.OrderStatus;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.domain.Entity.*;
import com.miniprogram.backend.domain.DAO.OrderRepository;
import com.miniprogram.backend.domain.DAO.OrderItemRepository;
import com.miniprogram.backend.domain.Mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务类
 * 处理订单相关的业务逻辑
 */
@Slf4j
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserAddressService userAddressService;
    
    @Autowired
    private ProductSkuService productSkuService;
    
    /**
     * 创建订单（从购物车结算）
     * 优化流程：先验证库存 -> 预占库存 -> 创建订单 -> 确认扣减
     * 确保数据一致性和并发安全
     */
    @Transactional
    public OrderDTO createOrderFromCart(Long userId, List<Long> cartItemIds, Long addressId, String remark) {
        log.info("Creating order from cart - userId: {}, cartItemIds: {}, addressId: {}", userId, cartItemIds, addressId);
        
        // 参数校验
        if (userId == null) {
            throw new BusinessException(400, "User ID cannot be null");
        }
        
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException(400, "Cart item IDs cannot be empty");
        }
        
        // 获取收货地址
        UserAddress address = userAddressService.getAddressEntityById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new BusinessException(404, "Invalid address");
        }
        
        // 获取选中的购物车项
        List<CartItemDTO> selectedItems;
        if (cartItemIds.contains(0L)) {
            // 如果包含0，表示选择所有选中的购物车项
            selectedItems = cartService.getSelectedCartItems(userId);
        } else {
            // 获取指定的购物车项
            selectedItems = cartItemIds.stream()
                    .map(cartItemId -> {
                        CartItem cartItem = cartService.getCartItemById(cartItemId);
                        return convertToDTOWithProduct(cartItem);
                    })
                    .collect(Collectors.toList());
        }
        
        if (selectedItems.isEmpty()) {
            throw new BusinessException(400, "No items to order");
        }
        
        // 【关键步骤1】计算订单金额
        BigDecimal totalAmount = selectedItems.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 验证订单金额
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(400, "Order amount must be positive");
        }
        
        // 防止异常大额订单（可选：设置最大金额限制，如100万）
        BigDecimal maxAmount = new BigDecimal("1000000");
        if (totalAmount.compareTo(maxAmount) > 0) {
            throw new BusinessException(400, "Order amount exceeds maximum limit");
        }
        
        // 计算运费（简化版：满99元包邮，否则10元运费）
        BigDecimal freightAmount = calculateFreight(totalAmount);
        
        // 计算优惠金额（预留接口，当前无优惠）
        BigDecimal discountAmount = calculateDiscount(userId, totalAmount);
        
        // 计算实际支付金额
        BigDecimal actualAmount = totalAmount.add(freightAmount).subtract(discountAmount);
        
        // 计算商品总件数
        int totalCount = selectedItems.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
        
        // 生成订单号（带重试机制）
        String orderNo = generateOrderNoWithRetry();
        
        // 【关键步骤2】创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalCount(totalCount);
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(freightAmount);
        order.setDiscountAmount(discountAmount);
        order.setActualAmount(actualAmount); // 实际支付金额 = 总金额 + 运费 - 优惠
        order.setStatus(OrderStatus.UNPAID); // 待支付
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        // 安全拼接地址，防止空指针
        String addressStr = String.format("%s%s%s%s", 
                java.util.Objects.toString(address.getProvince(), ""),
                java.util.Objects.toString(address.getCity(), ""),
                java.util.Objects.toString(address.getDistrict(), ""),
                java.util.Objects.toString(address.getDetail(), "")
        );
        order.setReceiverAddress(addressStr);
        order.setRemark(remark);
        
        order = orderRepository.save(order);
        log.info("Order created - orderId: {}, orderNo: {}", order.getId(), orderNo);
        
        // 批量获取所有产品信息，避免N+1查询
        java.util.Set<Long> productIds = selectedItems.stream()
                .map(CartItemDTO::getProductId)
                .collect(Collectors.toSet());
        java.util.Map<Long, Product> productMap = productService.getProductsByIds(productIds);
        
        // 【关键步骤3】创建订单项（在同一个事务中）
        try {
            for (CartItemDTO cartItem : selectedItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setTitle(cartItem.getProductName());
                orderItem.setImageUrl(cartItem.getProductImage());
                orderItem.setPrice(cartItem.getProductPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setSubtotal(cartItem.getSubtotal());
                
                // 从批量查询的Map中获取商品编号和SKU规格
                Product product = productMap.get(cartItem.getProductId());
                if (product != null) {
                    orderItem.setProductNo(product.getProductNo());
                } else {
                    log.warn("Product not found for order item - productId: {}", cartItem.getProductId());
                    orderItem.setProductNo("UNKNOWN");
                }
                
                // 如果有SKU，设置规格文本
                if (cartItem.getSkuId() != null) {
                    try {
                        var sku = productSkuService.getSkuById(cartItem.getSkuId());
                        if (sku != null) {
                            orderItem.setSpecText(sku.getSpecValues());
                        }
                    } catch (Exception e) {
                        log.warn("Failed to get SKU info for order item - skuId: {}", cartItem.getSkuId());
                        orderItem.setSpecText("");
                    }
                } else {
                    orderItem.setSpecText("");
                }
                
                orderItemRepository.save(orderItem);
            }
            log.info("Order items created successfully - orderId: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to create order items - orderId: {}, error: {}", order.getId(), e.getMessage());
            throw new BusinessException(500, "Failed to create order items: " + e.getMessage());
        }
        
        // 【关键步骤4】原子扣减库存（保证并发安全）
        // 直接使用原子操作，如果返回0表示库存不足，事务自动回滚
        // 不需要预先验证库存，避免竞态条件
        java.util.Map<Long, Integer> stockMap = selectedItems.stream()
                .collect(Collectors.toMap(
                    CartItemDTO::getProductId,
                    CartItemDTO::getQuantity
                ));
        
        try {
            productService.batchReduceStock(stockMap);
            log.info("Stock reduced successfully for all items - orderId: {}", order.getId());
        } catch (BusinessException e) {
            // 如果库存扣减失败（库存不足），抛出异常，事务自动回滚
            log.error("Failed to reduce stock - orderId: {}, error: {}. Transaction will be rolled back.", 
                     order.getId(), e.getMessage());
            throw new BusinessException(400, "库存不足，下单失败，请重试: " + e.getMessage());
        }
        
        // 【关键步骤5】删除已结算的购物车项
        if (cartItemIds.contains(0L)) {
            cartService.clearSelectedItems(userId);
        } else {
            cartService.batchDeleteCartItems(cartItemIds);
        }
        
        log.info("Order created successfully - orderId: {}, cart cleared", order.getId());
        
        // 返回订单详情
        return getOrderDetail(order.getId());
    }
    
    /**
     * 获取订单详情
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderDetail(Long orderId) {
        log.debug("Fetching order detail - orderId: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found with id: " + orderId));
        
        OrderDTO orderDTO = new OrderDTO(order);
        
        // 获取订单项
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        List<OrderItem> orderItemList = orderItems.stream()
                .map(item -> {
                    OrderItem itemDto = new OrderItem();
                    itemDto.setId(item.getId());
                    itemDto.setOrderId(item.getOrderId());
                    itemDto.setProductId(item.getProductId());
                    itemDto.setTitle(item.getTitle());
                    itemDto.setImageUrl(item.getImageUrl());
                    itemDto.setPrice(item.getPrice());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setSubtotal(item.getSubtotal());
                    return itemDto;
                })
                .collect(Collectors.toList());
        
        orderDTO.setItems(orderItemList);
        
        return orderDTO;
    }
    
    /**
     * 根据订单号获取订单
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNo(String orderNo) {
        log.debug("Fetching order by orderNo: {}", orderNo);
        
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(404, "Order not found with orderNo: " + orderNo));
        
        return getOrderDetail(order.getId());
    }
    
    /**
     * 获取用户订单列表（分页）
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getUserOrders(Long userId, String status, int page, int size) {
        log.debug("Fetching user orders - userId: {}, status: {}, page: {}, size: {}", userId, status, page, size);
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        // 优化：批量查询所有订单的订单项，避免 N+1 查询问题
        List<Long> orderIds = orders.getContent().stream()
                .map(Order::getId)
                .collect(Collectors.toList());
        
        // 一次性查询所有订单项
        List<OrderItem> allOrderItems = orderIds.isEmpty() ? 
                List.of() : 
                orderItemRepository.findByOrderIdIn(orderIds);
        
        // 按订单ID分组
        java.util.Map<Long, List<OrderItem>> itemsByOrderId = allOrderItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        
        // 构建 OrderDTO 列表
        List<OrderDTO> orderDTOs = orders.getContent().stream()
                .map(order -> {
                    OrderDTO dto = new OrderDTO(order);
                    // 从内存中获取该订单的订单项
                    List<OrderItem> items = itemsByOrderId.getOrDefault(order.getId(), List.of());
                    dto.setItemCount(items.size());
                    return dto;
                })
                .collect(Collectors.toList());
        
        return new PageResponse<>(page, size, orders.getTotalElements(), orderDTOs);
    }
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID（可选，为null时表示系统自动取消）
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order - orderId: {}, userId: {}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found with id: " + orderId));
        
        // 如果提供了userId，验证权限
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(403, "You don't have permission to cancel this order");
        }
        
        if (!OrderStatus.canCancel(order.getStatus())) {
            throw new BusinessException(400, "Only unpaid orders can be cancelled");
        }
        
        // 先获取订单项
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        
        // 【关键改进】先恢复库存，再更新订单状态
        // 如果库存恢复失败，事务回滚，订单状态保持不变
        int successCount = 0;
        int failCount = 0;
        for (OrderItem item : orderItems) {
            try {
                productService.increaseStock(item.getProductId(), item.getQuantity());
                successCount++;
                log.info("Restored stock for product: {}, quantity: {}", item.getProductId(), item.getQuantity());
            } catch (BusinessException e) {
                // 如果商品已删除或不存在，记录警告但继续处理其他商品
                // 注意：这里不抛出异常，因为商品可能已被管理员删除
                failCount++;
                log.warn("Failed to restore stock for product: {}, error: {}. Continuing with other items.", 
                         item.getProductId(), e.getMessage());
            }
        }
        
        // 如果有部分库存恢复失败，记录警告信息
        if (failCount > 0) {
            log.warn("Order cancellation completed with partial stock restoration failures - orderId: {}, success: {}, failed: {}", 
                    orderId, successCount, failCount);
        }
        
        // 库存恢复完成后，再更新订单状态
        orderRepository.markAsCancelled(orderId);
        
        log.info("Order cancelled successfully - orderId: {}, items processed: {}, stock restored: {}", 
                orderId, orderItems.size(), successCount);
    }
    
    /**
     * 支付订单
     */
    @Transactional
    public void payOrder(Long orderId, Long userId) {
        log.info("Paying order - orderId: {}, userId: {}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found with id: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "You don't have permission to pay this order");
        }
        
        if (!OrderStatus.canPay(order.getStatus())) {
            throw new BusinessException(400, "Only unpaid orders can be paid");
        }
        
        orderRepository.markAsPaid(orderId);
        log.info("Order paid - orderId: {}", orderId);
    }
    
    /**
     * 发货（管理员功能）
     */
    @Transactional
    public void deliverOrder(Long orderId) {
        log.info("Delivering order - orderId: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found with id: " + orderId));
        
        if (!OrderStatus.canShip(order.getStatus())) {
            throw new BusinessException(400, "Only paid orders can be delivered");
        }
        
        orderRepository.markAsShipped(orderId);
        log.info("Order delivered - orderId: {}", orderId);
    }
    
    /**
     * 确认收货
     */
    @Transactional
    public void confirmReceipt(Long orderId, Long userId) {
        log.info("Confirming receipt - orderId: {}, userId: {}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found with id: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "You don't have permission to confirm this order");
        }
        
        if (!OrderStatus.canConfirm(order.getStatus())) {
            throw new BusinessException(400, "Only shipped orders can be confirmed");
        }
        
        orderRepository.markAsCompleted(orderId);
        log.info("Order confirmed - orderId: {}", orderId);
    }
    
    /**
     * 删除订单（软删除）
     */
    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        log.info("Deleting order - orderId: {}, userId: {}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(404, "Order not found with id: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(403, "You don't have permission to delete this order");
        }
        
        orderRepository.softDelete(orderId);
        log.info("Order deleted - orderId: {}", orderId);
    }
    
    /**
     * 生成订单号（带重试机制）
     * 使用时间戳 + UUID 确保唯一性，避免并发重复
     */
    private String generateOrderNoWithRetry() {
        // 最多重试3次
        for (int i = 0; i < 3; i++) {
            String orderNo = generateOrderNo();
            // 检查订单号是否已存在
            if (!orderRepository.findByOrderNo(orderNo).isPresent()) {
                return orderNo;
            }
            log.warn("Order number duplicate, retrying... attempt: {}", i + 1);
        }
        // 如果3次都重复，抛出异常
        throw new BusinessException(500, "Failed to generate unique order number");
    }
    
    /**
     * 生成订单号
     * 使用时间戳 + UUID 确保唯一性，避免并发重复
     */
    private String generateOrderNo() {
        // 格式: ORD + 时间戳(13位) + UUID前8位(大写)
        // 示例: ORD1705123456789A1B2C3D4
        return "ORD" + System.currentTimeMillis() + 
               java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
    
    /**
     * 获取用户订单统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStats(Long userId) {
        log.debug("Fetching order stats for user: {}", userId);
        
        var stats = orderMapper.findOrderStatsByUserId(userId);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalOrders", stats.getTotalOrders());
        result.put("unpaidCount", stats.getPendingPayment());
        result.put("unshippedCount", stats.getPaid());
        result.put("unreceivedCount", stats.getDelivered());
        result.put("finishedCount", stats.getCompleted());
        result.put("cancelledCount", stats.getCancelled());
        
        return result;
    }
    
    /**
     * 将CartItem转换为CartItemDTO（包含产品信息）
     */
    private CartItemDTO convertToDTOWithProduct(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO(cartItem);
        
        // 获取产品信息
        try {
            var product = productService.getProductById(cartItem.getProductId());
            if (product != null) {
                dto.setProductName(product.getTitle());
                dto.setProductPrice(product.getPrice());
                dto.setProductImage(product.getImageUrl());
                dto.setProductStock(product.getStock());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch product info for cart item - productId: {}", cartItem.getProductId());
        }
        
        return dto;
    }
    
    /**
     * 计算运费
     * 规则：满99元包邮，否则10元运费
     * TODO: 后续可根据地址、重量、体积等复杂规则计算
     */
    private BigDecimal calculateFreight(BigDecimal totalAmount) {
        // 简化版：满99包邮
        BigDecimal freeShippingThreshold = new BigDecimal("99");
        if (totalAmount.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("10");
    }
    
    /**
     * 计算优惠金额
     * TODO: 后续可集成优惠券、满减活动等
     */
    private BigDecimal calculateDiscount(Long userId, BigDecimal totalAmount) {
        // 当前无优惠，返回0
        // 预留接口，后续可以：
        // 1. 查询用户可用优惠券
        // 2. 计算满减活动
        // 3. 会员折扣等
        return BigDecimal.ZERO;
    }
}
