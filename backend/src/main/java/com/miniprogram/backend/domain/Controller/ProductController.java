package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.domain.Entity.ProductDetailDTO;
import com.miniprogram.backend.domain.Entity.ProductDTO;
import com.miniprogram.backend.domain.Entity.ProductSku;
import com.miniprogram.backend.domain.Service.ProductService;
import com.miniprogram.backend.domain.Service.ProductSkuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductSkuService productSkuService;
    
    // 获取所有产品（分页）- 公开访问
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        PageResponse<ProductDTO> products = productService.getAllProducts(page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    // 根据ID获取产品 - 公开访问
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    // 根据商品编号获取商品详情 - 公开访问
    @GetMapping("/no/{productNo}/detail")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetailByNo(@PathVariable String productNo) {
        ProductDetailDTO product = productService.getProductDetailByNo(productNo);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    // 创建新产品 - 需要管理员权限
    @RequireAdmin
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(ApiResponse.success(createdProduct));
    }
    
    // 更新产品 - 需要管理员权限
    @RequireAdmin
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct));
    }
    
    // 删除产品 - 需要管理员权限
    @RequireAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // 根据名称搜索产品（分页）- 公开访问
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") Boolean inStockOnly,
            @RequestParam(defaultValue = "comprehensive") String sortType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<ProductDTO> products = productService.searchProducts(
                keyword, categoryId, minPrice, maxPrice, inStockOnly, sortType, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    // 根据名称搜索产品（分页）- 公开访问
    @GetMapping("/search/{name}")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> searchProductsByName(
            @PathVariable String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        PageResponse<ProductDTO> products = productService.searchProductsByName(name, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    // 根据数量查找产品（分页）- 公开访问
    @GetMapping("/quantity/{quantity}")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> findProductsByQuantity(
            @PathVariable Integer quantity,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        PageResponse<ProductDTO> products = productService.findProductsByQuantity(quantity, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    // ==================== MyBatis 复杂查询接口 ====================
    
    /**
     * 根据价格范围查询产品（MyBatis 实现）- 带分页 - 公开访问
     */
    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> findProductsByPriceRange(
            @RequestParam Double minPrice, 
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        PageResponse<ProductDTO> products = productService.findProductsByPriceRange(minPrice, maxPrice, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * 批量插入产品（MyBatis 实现）- 需要管理员权限
     */
    @RequireAdmin
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Integer>> batchInsertProducts(@Valid @RequestBody List<ProductDTO> productDTOs) {
        int count = productService.batchInsertProducts(productDTOs);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    /**
     * 按销量排序获取产品列表（分页）- 公开访问
     */
    @GetMapping("/sorted-by-sales")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getProductsSortedBySales(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        PageResponse<ProductDTO> products = productService.getProductsSortedBySales(page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * 增加产品销量 - 需要管理员权限
     */
    @RequireAdmin
    @PostMapping("/{id}/increase-sales")
    public ResponseEntity<ApiResponse<Void>> increaseSales(@PathVariable Long id, @RequestParam Integer amount) {
        productService.increaseSales(id, amount);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    // ==================== SKU相关接口 ====================
    
    /**
     * 获取商品的所有SKU
     */
    @GetMapping("/{productId}/skus")
    public ResponseEntity<ApiResponse<List<ProductSku>>> getProductSkus(@PathVariable Long productId) {
        List<ProductSku> skus = productSkuService.getProductSkus(productId);
        return ResponseEntity.ok(ApiResponse.success(skus));
    }
    
    /**
     * 获取指定SKU详情
     */
    @GetMapping("/skus/{skuId}")
    public ResponseEntity<ApiResponse<ProductSku>> getSkuById(@PathVariable Long skuId) {
        ProductSku sku = productSkuService.getSkuById(skuId);
        return ResponseEntity.ok(ApiResponse.success(sku));
    }
    
    // ==================== 分类筛选接口 ====================
    
    /**
     * 按分类查询商品（分页）
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<ProductDTO> products = productService.getProductsByCategory(categoryId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
}
