package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 商品SKU数据访问接口
 */
@Repository
public interface ProductSkuRepository extends JpaRepository<ProductSku, Long> {
    
    /**
     * 根据商品ID查询所有SKU
     */
    List<ProductSku> findByProductIdAndStatusOrderByCreatedAtAsc(Long productId, Integer status);
    
    /**
     * 根据SKU编号查询
     */
    Optional<ProductSku> findBySkuNo(String skuNo);
    
    /**
     * 根据商品ID和规格值查询SKU
     */
    Optional<ProductSku> findByProductIdAndSpecValues(Long productId, String specValues);
}
