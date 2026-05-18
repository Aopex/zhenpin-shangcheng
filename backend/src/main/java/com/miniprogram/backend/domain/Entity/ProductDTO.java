package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 产品DTO（数据传输对象）- 用于前后端数据传输
public class ProductDTO {
    
    private Long id;
    
    @NotBlank(message = "商品标题不能为空")
    private String title;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
    
    private String imageUrl;
    private String description;
    private Integer sales;
    private Long categoryId;
    private String productNo;
    private BigDecimal originalPrice;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public ProductDTO() {}
    
    // 从 Entity 转换为 DTO
    public ProductDTO(Product product) {
        this.id = product.getId();
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.imageUrl = product.getImageUrl();
        this.description = product.getDescription();
        this.sales = product.getSales();
        this.categoryId = product.getCategoryId();
        this.productNo = product.getProductNo();
        this.originalPrice = product.getOriginalPrice();
        this.status = product.getStatus();
        this.sortOrder = product.getSortOrder();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }
    
    // 从 DTO 转换为 Entity（用于新增）
    public Product toEntity() {
        Product product = new Product();
        product.setTitle(this.title);
        product.setPrice(this.price);
        product.setStock(this.stock != null ? this.stock : 0);
        product.setImageUrl(this.imageUrl);
        product.setDescription(this.description);
        product.setSales(this.sales != null ? this.sales : 0);
        product.setCategoryId(this.categoryId);
        product.setProductNo(this.productNo);
        product.setOriginalPrice(this.originalPrice);
        product.setStatus(this.status != null ? this.status : 1);
        product.setSortOrder(this.sortOrder != null ? this.sortOrder : 0);
        // 注意：createdAt 和 updatedAt 由 JPA 自动管理
        return product;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getSales() {
        return sales;
    }
    
    public void setSales(Integer sales) {
        this.sales = sales;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getProductNo() {
        return productNo;
    }
    
    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}