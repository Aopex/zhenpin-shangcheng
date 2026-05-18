package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // ==================== 商品查询相关 ====================
    
    /**
     * 查找上架的商品（分页）
     */
    Page<Product> findByStatusOrderBySortOrderDesc(Integer status, Pageable pageable);
    
    /**
     * 根据ID查找上架的商品
     */
    Optional<Product> findByIdAndStatus(Long id, Integer status);
    
    /**
     * 根据商品编号查找
     */
    Optional<Product> findByProductNo(String productNo);
    
    /**
     * 根据标题模糊搜索商品（分页）
     */
    Page<Product> findByTitleContainingAndStatus(String title, Integer status, Pageable pageable);
    
    /**
     * 根据分类ID查询商品（分页）
     */
    Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
    
    /**
     * 按销量排序查找上架的商品（分页）
     */
    Page<Product> findAllByStatusOrderBySalesDesc(Integer status, Pageable pageable);
    
    /**
     * 根据库存查询商品
     */
    Page<Product> findByStockGreaterThanEqualAndStatus(Integer stock, Integer status, Pageable pageable);

    /**
     * 组合搜索上架商品（关键词、分类、价格、库存）
     */
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = 1
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.productNo) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR p.categoryId = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStockOnly = false OR p.stock > 0)
            """)
    Page<Product> searchActiveProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("inStockOnly") boolean inStockOnly,
            Pageable pageable);
    
    // ==================== 并发安全的原子操作 ====================
    
    /**
     * 原子增加销量（避免并发问题）
     * 使用 JPQL 直接更新数据库，保证原子性
     */
    @Modifying
    @Query("UPDATE Product p SET p.sales = p.sales + :amount, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.status = 1")
    void increaseSales(@Param("id") Long id, @Param("amount") Integer amount);
    
    /**
     * 原子扣减库存（为后续订单功能预留）
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.stock >= :quantity AND p.status = 1")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 原子增加库存（用于取消订单时恢复库存）
     * 使用 JPQL 直接更新数据库，保证原子性
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.status = 1")
    void increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
