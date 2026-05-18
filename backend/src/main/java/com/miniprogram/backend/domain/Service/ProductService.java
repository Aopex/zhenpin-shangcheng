package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.common.PageResponse;
import com.miniprogram.backend.domain.Entity.Product;
import com.miniprogram.backend.domain.Entity.ProductDetailDTO;
import com.miniprogram.backend.domain.Entity.ProductDTO;
import com.miniprogram.backend.domain.Entity.ProductImage;
import com.miniprogram.backend.domain.Entity.ProductSku;
import com.miniprogram.backend.domain.Entity.Spec;
import com.miniprogram.backend.domain.DAO.ProductRepository;
import com.miniprogram.backend.domain.DAO.ProductImageRepository;
import com.miniprogram.backend.domain.DAO.ProductSkuRepository;
import com.miniprogram.backend.domain.DAO.SpecRepository;
import com.miniprogram.backend.domain.DAO.SpecValueRepository;
import com.miniprogram.backend.domain.Mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductSkuRepository productSkuRepository;

    @Autowired
    private SpecRepository specRepository;

    @Autowired
    private SpecValueRepository specValueRepository;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired(required = false)
    private com.miniprogram.backend.service.CacheService cacheService;
    
    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;
    
    // ==================== JPA 方法（简单 CRUD）====================
    
    /**
     * 获取所有产品（返回分页数据）
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> getAllProducts(Integer page, Integer pageSize) {
        // 参数验证和默认值处理
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 5;
        
        log.debug("Fetching all active products - page: {}, pageSize: {}", validPage, validPageSize);
        
        // 创建分页对象（Spring Data JPA 页码从0开始，所以要减1）
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<Product> productPage = productRepository.findByStatusOrderBySortOrderDesc(1, pageable);
        
        // 转换为 DTO
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
                validPage,
                validPageSize,
                productPage.getTotalElements(),
                productDTOs
        );
    }
    
    /**
     * 根据ID获取产品（返回 DTO）
     * 使用缓存优化，减少数据库查询
     * 使用分布式锁防止缓存击穿
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product by id: {}", id);
        
        String cacheKey = "product:" + id;
        String lockKey = "lock:product:" + id;
        
        // 尝试从缓存获取
        if (cacheService != null) {
            ProductDTO cachedProduct = cacheService.get(cacheKey, ProductDTO.class);
            if (cachedProduct != null) {
                log.debug("Cache hit for product id: {}", id);
                return cachedProduct;
            }
        }
        
        // 缓存未命中，尝试获取分布式锁
        if (cacheService != null && cacheService.tryLock(lockKey, 10)) {
            try {
                // 双重检查：获取锁后再次检查缓存（其他线程可能已经填充了缓存）
                ProductDTO cachedProduct = cacheService.get(cacheKey, ProductDTO.class);
                if (cachedProduct != null) {
                    log.debug("Cache hit after acquiring lock for product id: {}", id);
                    return cachedProduct;
                }
                
                // 从数据库查询
                ProductDTO product = productRepository.findByIdAndStatus(id, 1)
                        .map(ProductDTO::new)
                        .orElseThrow(() -> new BusinessException(404, "Product not found with id: " + id));
                
                // 存入缓存（30分钟过期）
                cacheService.set(cacheKey, product, 30, java.util.concurrent.TimeUnit.MINUTES);
                log.debug("Cached product id: {}", id);
                
                return product;
            } finally {
                // 释放锁
                cacheService.unlock(lockKey);
            }
        } else {
            // 获取锁失败，说明其他线程正在加载数据，短暂等待后重试
            log.debug("Failed to acquire lock for product id: {}, waiting and retrying...", id);
            try {
                Thread.sleep(50); // 等待50毫秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 重试从缓存获取
            ProductDTO cachedProduct = cacheService != null ? cacheService.get(cacheKey, ProductDTO.class) : null;
            if (cachedProduct != null) {
                return cachedProduct;
            }
            
            // 如果还是没有缓存，直接从数据库查询（降级策略）
            log.warn("Cache miss and lock failed for product id: {}, querying database directly", id);
            return productRepository.findByIdAndStatus(id, 1)
                    .map(ProductDTO::new)
                    .orElseThrow(() -> new BusinessException(404, "Product not found with id: " + id));
        }
    }

    /**
     * 根据商品编号获取小程序商品详情页所需的完整数据。
     */
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductDetailByNo(String productNo) {
        Product product = productRepository.findByProductNo(productNo)
                .filter(p -> p.getStatus() == 1)
                .orElseThrow(() -> new BusinessException(404, "Product not found with no: " + productNo));

        List<String> banners = productImageRepository
                .findByProductIdAndImageTypeOrderBySortOrderAsc(product.getId(), 1)
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        if (banners.isEmpty() && product.getImageUrl() != null) {
            banners = java.util.Collections.singletonList(product.getImageUrl());
        }

        List<String> detailImages = productImageRepository
                .findByProductIdAndImageTypeOrderBySortOrderAsc(product.getId(), 2)
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());

        List<ProductSku> skus = productSkuRepository
                .findByProductIdAndStatusOrderByCreatedAtAsc(product.getId(), 1);

        List<ProductDetailDTO.SpecGroupDTO> specs = specRepository
                .findByProductIdOrderBySortOrderAsc(product.getId())
                .stream()
                .map(spec -> {
                    ProductDetailDTO.SpecGroupDTO group = new ProductDetailDTO.SpecGroupDTO();
                    group.setId(spec.getId());
                    group.setName(spec.getName());
                    group.setValues(specValueRepository.findBySpecIdOrderBySortOrderAsc(spec.getId())
                            .stream()
                            .map(value -> {
                                ProductDetailDTO.SpecValueDTO item = new ProductDetailDTO.SpecValueDTO();
                                item.setId(value.getId());
                                item.setValue(value.getValue());
                                return item;
                            })
                            .collect(Collectors.toList()));
                    return group;
                })
                .collect(Collectors.toList());

        ProductDetailDTO detail = new ProductDetailDTO();
        detail.setId(product.getId());
        detail.setNo(product.getProductNo());
        detail.setTitle(product.getTitle());
        detail.setPrice(product.getPrice());
        detail.setOriginalPrice(product.getOriginalPrice());
        detail.setSales(product.getSales());
        detail.setStock(product.getStock());
        detail.setBanners(banners);
        detail.setDetailImages(detailImages);
        detail.setSkus(skus);
        detail.setSpecs(specs);
        return detail;
    }
    
    /**
     * 批量获取产品信息（用于优化N+1查询）
     * @param productIds 产品ID集合
     * @return Map<productId, Product>
     */
    @Transactional(readOnly = true)
    public java.util.Map<Long, Product> getProductsByIds(java.util.Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        
        log.debug("Batch fetching products for {} ids", productIds.size());
        
        List<Product> products = productRepository.findAllById(productIds);
        
        // 过滤出状态为上架的产品
        return products.stream()
                .filter(p -> p.getStatus() == 1)
                .collect(Collectors.toMap(Product::getId, p -> p));
    }
    
    /**
     * 创建新产品（接收 DTO，返回 DTO）
     */
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getTitle());
        
        // 将 DTO 转换为 Entity
        Product product = productDTO.toEntity();
        
        // 字段校验
        validateProduct(product, true);
        
        // 保存并返回 DTO（createdAt 和 updatedAt 由 JPA 自动管理）
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        
        // 清除相关缓存
        if (cacheService != null) {
            cacheService.delete("products:all");
            cacheService.delete("products:category:" + savedProduct.getCategoryId());
        }
        
        return new ProductDTO(savedProduct);
    }
    
    /**
     * 更新产品（接收 DTO，返回 DTO）
     */
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with id: {}", id);
        
        Product existingProduct = productRepository.findByIdAndStatus(id, 1)
                .orElseThrow(() -> new BusinessException(404, "Product not found with id: " + id));
        
        // 将 DTO 转换为临时 Entity 用于校验
        Product tempProduct = productDTO.toEntity();
        
        // 字段校验（更新时不需要校验所有字段）
        validateProductForUpdate(tempProduct);
        
        // 只更新传入的非空字段
        boolean updated = false;
        if (StringUtils.hasText(productDTO.getTitle())) {
            existingProduct.setTitle(productDTO.getTitle());
            updated = true;
        }
        if (productDTO.getPrice() != null) {
            existingProduct.setPrice(productDTO.getPrice());
            updated = true;
        }
        if (productDTO.getStock() != null) {
            existingProduct.setStock(productDTO.getStock());
            updated = true;
        }
        if (StringUtils.hasText(productDTO.getImageUrl())) {
            existingProduct.setImageUrl(productDTO.getImageUrl());
            updated = true;
        }
        if (StringUtils.hasText(productDTO.getDescription())) {
            existingProduct.setDescription(productDTO.getDescription());
            updated = true;
        }
        if (productDTO.getCategoryId() != null) {
            existingProduct.setCategoryId(productDTO.getCategoryId());
            updated = true;
        }
        if (productDTO.getOriginalPrice() != null) {
            existingProduct.setOriginalPrice(productDTO.getOriginalPrice());
            updated = true;
        }
        if (productDTO.getStatus() != null) {
            existingProduct.setStatus(productDTO.getStatus());
            updated = true;
        }
        if (productDTO.getSortOrder() != null) {
            existingProduct.setSortOrder(productDTO.getSortOrder());
            updated = true;
        }
        
        if (!updated) {
            log.warn("No fields to update for product id: {}", id);
            throw new BusinessException(400, "No fields to update");
        }
        
        // 自动更新修改时间
        existingProduct.setUpdatedAt(LocalDateTime.now());
        
        // 保存并返回 DTO
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("product:" + id);
            log.debug("Cache cleared for product id: {}", id);
        }
        
        return new ProductDTO(updatedProduct);
    }
    
    /**
     * 删除产品（下架处理）
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        
        Product product = productRepository.findByIdAndStatus(id, 1)
                .orElseThrow(() -> new BusinessException(404, "Product not found with id: " + id));
        
        // 设置状态为下架
        product.setStatus(0);
        
        // 保存更新
        productRepository.save(product);
        log.info("Product deleted successfully with id: {}", id);
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("product:" + id);
            log.debug("Cache cleared for deleted product id: {}", id);
        }
        
        // 清理购物车中的该商品
        try {
            // 注入CartItemRepository来清理购物车
            com.miniprogram.backend.domain.DAO.CartItemRepository cartItemRepository = 
                applicationContext.getBean(com.miniprogram.backend.domain.DAO.CartItemRepository.class);
            cartItemRepository.deleteByProductId(id);
            log.info("Cleared cart items for deleted product id: {}", id);
        } catch (Exception e) {
            log.warn("Failed to clear cart items for product id: {}, error: {}", id, e.getMessage());
        }
    }
    
    /**
     * 根据名称搜索产品（返回分页数据）
     * @param name 搜索关键词
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> searchProductsByName(String name, Integer page, Integer pageSize) {
        // 参数验证和默认值处理
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 5;
        
        log.debug("Searching products by name: {} - page: {}, pageSize: {}", name, validPage, validPageSize);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<Product> productPage = productRepository.findByTitleContainingAndStatus(name, 1, pageable);
        
        // 转换为 DTO
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
                validPage,
                validPageSize,
                productPage.getTotalElements(),
                productDTOs
        );
    }
    
    /**
     * 根据数量查找产品（返回分页数据）
     * @param quantity 最小数量
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> findProductsByQuantity(Integer quantity, Integer page, Integer pageSize) {
        // 参数验证和默认值处理
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 5;
        
        log.debug("Finding products with quantity >= {} - page: {}, pageSize: {}", quantity, validPage, validPageSize);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<Product> productPage = productRepository.findByStockGreaterThanEqualAndStatus(quantity, 1, pageable);
        
        // 转换为 DTO
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
                validPage,
                validPageSize,
                productPage.getTotalElements(),
                productDTOs
        );
    }
    
    // ==================== MyBatis 方法（复杂查询）====================
    
    /**
     * 根据价格范围查询产品（复杂查询使用 MyBatis，返回分页数据）
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> findProductsByPriceRange(Double minPrice, Double maxPrice, Integer page, Integer pageSize) {
        // 参数验证和默认值处理
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 5;
        
        log.debug("Finding products with price range: {} - {} - page: {}, pageSize: {}", minPrice, maxPrice, validPage, validPageSize);
        
        // 计算偏移量
        int offset = (validPage - 1) * validPageSize;
        
        // 使用 MyBatis 进行分页查询
        List<Product> products = productMapper.findByPriceRangeWithPage(minPrice, maxPrice, offset, validPageSize);
        
        // 获取总数（需要额外查询）
        long total = getCountByPriceRange(minPrice, maxPrice);
        
        // 转换为 DTO
        List<ProductDTO> productDTOs = products.stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
                validPage,
                validPageSize,
                total,
                productDTOs
        );
    }
    
    /**
     * 根据价格范围查询产品总数
     */
    @Transactional(readOnly = true)
    public long getCountByPriceRange(Double minPrice, Double maxPrice) {
        return productMapper.countByPriceRange(minPrice, maxPrice);
    }
    
    /**
     * 按销量排序获取产品列表（返回分页数据）
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> getProductsSortedBySales(Integer page, Integer pageSize) {
        // 参数验证和默认值处理
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 5;
        
        log.debug("Fetching products sorted by sales - page: {}, pageSize: {}", validPage, validPageSize);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<Product> productPage = productRepository.findAllByStatusOrderBySalesDesc(1, pageable);
        
        // 转换为 DTO
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
                validPage,
                validPageSize,
                productPage.getTotalElements(),
                productDTOs
        );
    }
    
    /**
     * 增加产品销量（并发安全版本）
     */
    @Transactional
    public void increaseSales(Long id, Integer amount) {
        log.info("Increasing sales for product id: {}, amount: {}", id, amount);
        
        Product product = productRepository.findByIdAndStatus(id, 1)
                .orElseThrow(() -> new BusinessException(404, "Product not found with id: " + id));
        
        if (amount <= 0) {
            throw new BusinessException(400, "Sales amount must be positive");
        }
        
        // 使用原子操作更新销量，避免并发问题
        productRepository.increaseSales(id, amount);
        log.info("Sales increased successfully for product id: {}", id);
    }
    
    /**
     * 批量插入产品（批量操作使用 MyBatis，接收 DTO 列表）
     */
    @Transactional
    public int batchInsertProducts(List<ProductDTO> productDTOs) {
        log.info("Batch inserting {} products", productDTOs.size());
        
        // 将 DTO 列表转换为 Entity 列表
        List<Product> products = productDTOs.stream()
                .map(ProductDTO::toEntity)
                .collect(Collectors.toList());
        
        // 为每个产品设置时间戳并校验
        products.forEach(product -> {
            validateProduct(product, true);
        });
        
        int count = productMapper.batchInsert(products);
        log.info("Successfully inserted {} products", count);
        return count;
    }
    
    // ==================== 私有校验方法 ====================
    
    /**
     * 校验产品字段（用于新增）
     * 只校验数据库中 NOT NULL 的字段：title, price, stock
     * @param product 待校验的产品对象
     */
    private void validateProduct(Product product, boolean isCreate) {
        if (product == null) {
            throw new BusinessException("Product cannot be null");
        }
        
        // 标题校验（NOT NULL）
        if (!StringUtils.hasText(product.getTitle())) {
            throw new BusinessException("Product title cannot be empty");
        }
        
        // 价格校验（NOT NULL）
        if (product.getPrice() == null) {
            throw new BusinessException("Product price cannot be null");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Product price cannot be negative");
        }
        
        // 库存校验（NOT NULL）
        if (product.getStock() == null) {
            throw new BusinessException("Product stock cannot be null");
        }
        if (product.getStock() < 0) {
            throw new BusinessException("Product stock cannot be negative");
        }
        
        // imageUrl 和 description 允许为 null，不做校验
    }
    
    /**
     * 校验产品字段（用于更新）
     * 只校验传入的非空字段，确保数据有效性
     * @param product 待校验的产品对象
     */
    private void validateProductForUpdate(Product product) {
        if (product == null) {
            throw new BusinessException("Product cannot be null");
        }
        
        // 如果提供了标题，则校验标题不为空字符串
        if (StringUtils.hasText(product.getTitle()) && product.getTitle().trim().isEmpty()) {
            throw new BusinessException("Product title cannot be empty");
        }
        
        // 如果提供了价格，则校验价格不为负数
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Product price cannot be negative");
        }
        
        // 如果提供了库存，则校验库存不为负数
        if (product.getStock() != null && product.getStock() < 0) {
            throw new BusinessException("Product stock cannot be negative");
        }
        
        // imageUrl 和 description 允许为 null 或空字符串，不做额外校验
    }
    
    /**
     * 扣减库存（并发安全版本）
     * 使用数据库原子操作，避免超卖问题
     */
    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        log.info("Reducing stock for product: {}, quantity: {}", productId, quantity);
        
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "Quantity must be greater than 0");
        }
        
        // 使用原子操作扣减库存，保证并发安全
        // SQL: UPDATE products SET stock = stock - #{quantity} 
        //      WHERE id = #{id} AND stock >= #{quantity} AND status = 1
        int affectedRows = productRepository.decreaseStock(productId, quantity);
        
        if (affectedRows == 0) {
            // 扣减失败，可能是库存不足或商品不存在/已下架
            Product product = productRepository.findByIdAndStatus(productId, 1).orElse(null);
            if (product == null) {
                throw new BusinessException(404, "Product not found with id: " + productId);
            } else {
                throw new BusinessException(400, "Insufficient stock for product: " + product.getTitle() + 
                    ". Available: " + product.getStock() + ", Requested: " + quantity);
            }
        }
        
        // 库存变化后清除缓存，确保下次查询获取最新库存
        if (cacheService != null) {
            cacheService.delete("product:" + productId);
            log.debug("Cache cleared for product after stock reduction: {}", productId);
        }
        
        log.info("Stock reduced successfully for product: {}, affected rows: {}", productId, affectedRows);
    }
    
    /**
     * 增加库存（并发安全版本）
     * 使用数据库原子操作，避免并发问题
     */
    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        log.info("Increasing stock for product: {}, quantity: {}", productId, quantity);
        
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "Quantity must be greater than 0");
        }
        
        // 先验证商品是否存在
        Product product = productRepository.findByIdAndStatus(productId, 1)
                .orElseThrow(() -> new BusinessException(404, "Product not found with id: " + productId));
        
        // 使用原子操作增加库存
        productRepository.increaseStock(productId, quantity);
        
        // 库存变化后清除缓存，确保下次查询获取最新库存
        if (cacheService != null) {
            cacheService.delete("product:" + productId);
            log.debug("Cache cleared for product after stock increase: {}", productId);
        }
        
        log.info("Stock increased successfully for product: {}", productId);
    }
    
    /**
     * 批量扣减库存（优化版）
     * 一次性扣减多个商品的库存，减少数据库交互次数
     * @param stockMap key=productId, value=quantity
     */
    @Transactional
    public void batchReduceStock(java.util.Map<Long, Integer> stockMap) {
        log.info("Batch reducing stock for {} products", stockMap.size());
        
        if (stockMap == null || stockMap.isEmpty()) {
            return;
        }
        
        // 逐个扣减，但使用同一个事务
        for (java.util.Map.Entry<Long, Integer> entry : stockMap.entrySet()) {
            reduceStock(entry.getKey(), entry.getValue());
        }
        
        log.info("Batch stock reduction completed for {} products", stockMap.size());
    }
    
    /**
     * 按分类查询商品（分页）
     * @param categoryId 分类ID
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> getProductsByCategory(Long categoryId, Integer page, Integer pageSize) {
        int validPage = (page != null && page > 0) ? page : 1;
        int validPageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        
        log.debug("Fetching products by category: {} - page: {}, pageSize: {}", categoryId, validPage, validPageSize);
        
        Pageable pageable = PageRequest.of(validPage - 1, validPageSize);
        Page<Product> productPage = productRepository.findByCategoryIdAndStatus(categoryId, 1, pageable);
        
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(ProductDTO::new)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                validPage,
                validPageSize,
                productPage.getTotalElements(),
                productDTOs
        );
    }
}
