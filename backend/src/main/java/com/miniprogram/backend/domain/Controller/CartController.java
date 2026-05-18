package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.UserContext;
import com.miniprogram.backend.domain.Entity.AddToCartRequest;
import com.miniprogram.backend.domain.Entity.CartItemDTO;
import com.miniprogram.backend.domain.Entity.UpdateQuantityRequest;
import com.miniprogram.backend.domain.Entity.UpdateSelectedRequest;
import com.miniprogram.backend.domain.Service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    /**
     * 添加商品到购物车
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        // 从JWT Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        
        CartItemDTO cartItem = cartService.addToCart(
                userId,  // 使用Token中的userId
                request.getProductId(), 
                request.getSkuId(), 
                request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }
    
    /**
     * 获取用户购物车列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getUserCart() {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        List<CartItemDTO> cartItems = cartService.getUserCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cartItems));
    }
    
    /**
     * 更新购物车项数量
     */
    @PutMapping("/{cartItemId}/quantity")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        CartItemDTO cartItem = cartService.updateCartItemQuantity(cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }
    
    /**
     * 删除购物车项
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(@PathVariable Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 更新购物车项选中状态
     */
    @PutMapping("/{cartItemId}/selected")
    public ResponseEntity<ApiResponse<Void>> updateItemSelected(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateSelectedRequest request) {
        // 验证购物车项属于当前用户
        Long userId = UserContext.getCurrentUserId();
        cartService.updateItemSelected(cartItemId, request.getSelected(), userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 批量更新用户购物车项选中状态
     */
    @PutMapping("/select-all")
    public ResponseEntity<ApiResponse<Void>> selectAllItems(@Valid @RequestBody UpdateSelectedRequest request) {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        cartService.updateAllItemsSelected(userId, request.getSelected());
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 获取用户选中的购物车项
     */
    @GetMapping("/selected")
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getSelectedItems() {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        List<CartItemDTO> cartItems = cartService.getSelectedCartItems(userId);
        return ResponseEntity.ok(ApiResponse.success(cartItems));
    }
    
    /**
     * 计算选中商品的总金额
     */
    @GetMapping("/total")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> calculateSelectedTotal() {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        java.math.BigDecimal total = cartService.calculateSelectedTotal(userId);
        return ResponseEntity.ok(ApiResponse.success(total));
    }
    
    /**
     * 清空用户购物车
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        // 从Token中获取用户ID
        Long userId = UserContext.getCurrentUserId();
        cartService.clearUserCart(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 批量删除购物车项
     */
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse<Void>> batchDeleteItems(@RequestBody List<Long> cartItemIds) {
        cartService.batchDeleteCartItems(cartItemIds);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
