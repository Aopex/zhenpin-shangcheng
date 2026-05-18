package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 商品图片数据访问接口
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    /**
     * 根据商品ID和图片类型查询图片列表
     */
    List<ProductImage> findByProductIdAndImageTypeOrderBySortOrderAsc(Long productId, Integer imageType);
    
    /**
     * 根据商品ID查询所有图片
     */
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
}
