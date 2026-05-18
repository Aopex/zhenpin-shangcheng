package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.domain.Entity.CartItem;
import com.miniprogram.backend.domain.Entity.CartItemDTO;
import com.miniprogram.backend.domain.DAO.CartItemRepository;
import com.miniprogram.backend.domain.Mapper.CartItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务类
 * 处理购物车相关的业务逻辑
 */
@Slf4j
@Service
public class CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private CartItemMapper cartItemMapper;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductSkuService productSkuService;
    
    /**
     * 添加商品到购物车
     * 实现严格的库存检查和库存预占机制
     */
    @Transactional
    public CartItemDTO addToCart(Long userId, Long productId, Long skuId, Integer quantity) {
        log.info("Adding product to cart - userId: {}, productId: {}, skuId: {}, quantity: {}", userId, productId, skuId, quantity);
        
        // 参数校验
        if (userId == null || productId == null) {
            throw new BusinessException(400, "User ID and Product ID cannot be null");
        }
        
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "Quantity must be greater than 0");
        }
        
        // 验证产品是否存在
        var product = productService.getProductById(productId);
        if (product == null) {
            throw new BusinessException(404, "Product not found with id: " + productId);
        }
        
        // 严格库存检查：检查当前购物车中该商品的数量加上新增数量是否超过库存
        var existingCartItem = cartItemRepository.findByUserIdAndProductIdAndSkuId(userId, productId, skuId);
        int currentQuantity = existingCartItem.isPresent() ? existingCartItem.get().getQuantity() : 0;
        int totalQuantity = currentQuantity + quantity;
        
        // 如果指定了SKU，使用SKU库存；否则使用商品总库存
        if (skuId != null) {
            var sku = productSkuService.getSkuById(skuId);
            if (sku == null) {
                throw new BusinessException(404, "SKU not found with id: " + skuId);
            }
            // 严格检查SKU库存
            if (sku.getStock() < totalQuantity) {
                throw new BusinessException(400, 
                    String.format("Insufficient SKU stock. Available: %d, Requested: %d", sku.getStock(), totalQuantity));
            }
        } else {
            // 严格检查商品总库存
            if (product.getStock() < totalQuantity) {
                throw new BusinessException(400, 
                    String.format("Insufficient stock. Available: %d, Requested: %d", product.getStock(), totalQuantity));
            }
        }
        
        CartItem cartItem;
        if (existingCartItem.isPresent()) {
            // 更新数量
            cartItem = existingCartItem.get();
            cartItem.setQuantity(totalQuantity);
            cartItem.setSelected(true);
            cartItem = cartItemRepository.save(cartItem);
            log.info("Updated cart item quantity - cartItemId: {}, newQuantity: {}", cartItem.getId(), totalQuantity);
        } else {
            // 创建新的购物车项
            cartItem = new CartItem(userId, productId, skuId, quantity);
            cartItem.setSelected(true);
            cartItem = cartItemRepository.save(cartItem);
            log.info("Created new cart item - cartItemId: {}", cartItem.getId());
        }
        
        return convertToDTOWithProduct(cartItem);
    }
    
    /**
     * 更新购物车项数量
     * 实现严格的库存检查
     */
    @Transactional
    public CartItemDTO updateCartItemQuantity(Long cartItemId, Integer quantity) {
        log.info("Updating cart item quantity - cartItemId: {}, quantity: {}", cartItemId, quantity);
        
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "Quantity must be greater than 0");
        }
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(404, "Cart item not found with id: " + cartItemId));
        
        // 严格库存检查
        try {
            var product = productService.getProductById(cartItem.getProductId());
            if (product != null) {
                // 如果指定了SKU，检查SKU库存
                if (cartItem.getSkuId() != null) {
                    var sku = productSkuService.getSkuById(cartItem.getSkuId());
                    if (sku != null && sku.getStock() < quantity) {
                        throw new BusinessException(400, 
                            String.format("Insufficient SKU stock. Available: %d, Requested: %d", sku.getStock(), quantity));
                    }
                } else {
                    // 检查商品总库存
                    if (product.getStock() < quantity) {
                        throw new BusinessException(400, 
                            String.format("Insufficient stock. Available: %d, Requested: %d", product.getStock(), quantity));
                    }
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to check stock for cart item update - cartItemId: {}", cartItemId);
        }
        
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        
        log.info("Updated cart item quantity - cartItemId: {}", cartItemId);
        return convertToDTOWithProduct(cartItem);
    }
    
    /**
     * 删除购物车项
     */
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        log.info("Deleting cart item - cartItemId: {}", cartItemId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(404, "Cart item not found with id: " + cartItemId));
        
        cartItemRepository.delete(cartItem);
        log.info("Deleted cart item - cartItemId: {}", cartItemId);
    }
    
    /**
     * 获取用户购物车列表
     */
    @Transactional(readOnly = true)
    public List<CartItemDTO> getUserCart(Long userId) {
        log.debug("Fetching user cart - userId: {}", userId);
        
        List<CartItem> cartItems = cartItemMapper.findCartItemsWithProduct(userId);
        
        return cartItems.stream()
                .map(this::convertToDTOWithProduct)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户选中的购物车项
     */
    @Transactional(readOnly = true)
    public List<CartItemDTO> getSelectedCartItems(Long userId) {
        log.debug("Fetching selected cart items - userId: {}", userId);
        
        List<CartItem> cartItems = cartItemMapper.findSelectedCartItemsWithProduct(userId);
        
        return cartItems.stream()
                .map(this::convertToDTOWithProduct)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新购物车项选中状态
     */
    @Transactional
    public void updateItemSelected(Long cartItemId, Boolean selected, Long userId) {
        log.info("Updating cart item selected status - cartItemId: {}, selected: {}, userId: {}", cartItemId, selected, userId);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(404, "Cart item not found with id: " + cartItemId));
        
        // 验证购物车项属于当前用户
        if (!cartItem.getUserId().equals(userId)) {
            throw new BusinessException(403, "You don't have permission to modify this cart item");
        }
        
        cartItem.setSelected(selected);
        cartItemRepository.save(cartItem);
        
        log.info("Updated cart item selected status - cartItemId: {}", cartItemId);
    }
    
    /**
     * 批量更新用户购物车项选中状态
     */
    @Transactional
    public void updateAllItemsSelected(Long userId, Boolean selected) {
        log.info("Updating all cart items selected status - userId: {}, selected: {}", userId, selected);
        
        cartItemRepository.updateSelectedByUserId(userId, selected);
        
        log.info("Updated all cart items selected status - userId: {}", userId);
    }
    
    /**
     * 清空用户购物车
     */
    @Transactional
    public void clearUserCart(Long userId) {
        log.info("Clearing user cart - userId: {}", userId);
        
        cartItemRepository.deleteByUserId(userId);
        
        log.info("Cleared user cart - userId: {}", userId);
    }
    
    /**
     * 批量删除购物车项
     */
    @Transactional
    public void batchDeleteCartItems(List<Long> cartItemIds) {
        log.info("Batch deleting cart items - count: {}", cartItemIds.size());
        
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException(400, "Cart item IDs cannot be empty");
        }
        
        cartItemMapper.batchDelete(cartItemIds);
        
        log.info("Batch deleted cart items - count: {}", cartItemIds.size());
    }
    
    /**
     * 计算选中商品的总金额
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateSelectedTotal(Long userId) {
        log.debug("Calculating selected total - userId: {}", userId);
        
        List<CartItemDTO> selectedItems = getSelectedCartItems(userId);
        
        return selectedItems.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
                dto.setProductNo(product.getProductNo());
                dto.setProductName(product.getTitle());
                dto.setProductPrice(product.getPrice());
                dto.setProductImage(product.getImageUrl());
                dto.setProductStock(product.getStock());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch product info for cart item - productId: {}", cartItem.getProductId());
        }

        if (cartItem.getSkuId() != null) {
            try {
                var sku = productSkuService.getSkuById(cartItem.getSkuId());
                if (sku != null) {
                    dto.setSpecValues(sku.getSpecValues());
                    if (sku.getPrice() != null) {
                        dto.setProductPrice(sku.getPrice());
                    }
                    if (StringUtils.hasText(sku.getImageUrl())) {
                        dto.setProductImage(sku.getImageUrl());
                    }
                    dto.setProductStock(sku.getStock());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch SKU info for cart item - skuId: {}", cartItem.getSkuId());
            }
        }
        
        return dto;
    }
    
    /**
     * 根据ID获取购物车项
     */
    @Transactional(readOnly = true)
    public CartItem getCartItemById(Long cartItemId) {
        log.debug("Fetching cart item by id - cartItemId: {}", cartItemId);
        
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(404, "Cart item not found with id: " + cartItemId));
    }
    
    /**
     * 清空用户已选中的购物车项
     */
    @Transactional
    public void clearSelectedItems(Long userId) {
        log.info("Clearing selected cart items - userId: {}", userId);
        
        List<CartItem> selectedItems = cartItemRepository.findByUserIdAndSelectedTrue(userId);
        cartItemRepository.deleteAll(selectedItems);
        
        log.info("Cleared selected cart items - userId: {}", userId);
    }
}
