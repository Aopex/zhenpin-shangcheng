package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.domain.DAO.ProductSkuRepository;
import com.miniprogram.backend.domain.DAO.SpecRepository;
import com.miniprogram.backend.domain.DAO.SpecValueRepository;
import com.miniprogram.backend.domain.Entity.ProductSku;
import com.miniprogram.backend.domain.Entity.Spec;
import com.miniprogram.backend.domain.Entity.SpecValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品SKU和规格服务类
 */
@Service
public class ProductSkuService {
    
    @Autowired
    private ProductSkuRepository productSkuRepository;
    
    @Autowired
    private SpecRepository specRepository;
    
    @Autowired
    private SpecValueRepository specValueRepository;
    
    // ==================== SKU相关方法 ====================
    
    /**
     * 根据商品ID获取所有SKU列表
     */
    public List<ProductSku> getProductSkus(Long productId) {
        return productSkuRepository.findByProductIdAndStatusOrderByCreatedAtAsc(productId, 1);
    }
    
    /**
     * 根据SKU编号获取SKU详情
     */
    public ProductSku getSkuByNo(String skuNo) {
        return productSkuRepository.findBySkuNo(skuNo)
                .orElseThrow(() -> new BusinessException("SKU不存在"));
    }
    
    /**
     * 根据ID获取SKU详情
     */
    public ProductSku getSkuById(Long id) {
        return productSkuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SKU不存在"));
    }
    
    /**
     * 创建SKU
     */
    @Transactional
    public ProductSku createSku(ProductSku sku) {
        // 检查SKU编号是否已存在
        if (productSkuRepository.findBySkuNo(sku.getSkuNo()).isPresent()) {
            throw new BusinessException("SKU编号已存在");
        }
        
        // 设置默认值
        if (sku.getStatus() == null) {
            sku.setStatus(1);
        }
        if (sku.getStock() == null) {
            sku.setStock(0);
        }
        
        return productSkuRepository.save(sku);
    }
    
    /**
     * 更新SKU库存
     */
    @Transactional
    public void updateSkuStock(Long skuId, Integer stock) {
        ProductSku sku = getSkuById(skuId);
        sku.setStock(stock);
        productSkuRepository.save(sku);
    }
    
    /**
     * 扣减SKU库存
     */
    @Transactional
    public void decreaseSkuStock(Long skuId, Integer quantity) {
        ProductSku sku = getSkuById(skuId);
        if (sku.getStock() < quantity) {
            throw new BusinessException("库存不足");
        }
        sku.setStock(sku.getStock() - quantity);
        productSkuRepository.save(sku);
    }
    
    // ==================== 规格相关方法 ====================
    
    /**
     * 根据商品ID获取所有规格名列表
     */
    public List<Spec> getProductSpecs(Long productId) {
        return specRepository.findByProductIdOrderBySortOrderAsc(productId);
    }
    
    /**
     * 创建规格名
     */
    @Transactional
    public Spec createSpec(Spec spec) {
        // 设置默认值
        if (spec.getSortOrder() == null) {
            spec.setSortOrder(0);
        }
        
        return specRepository.save(spec);
    }
    
    // ==================== 规格值相关方法 ====================
    
    /**
     * 根据规格ID获取所有规格值列表
     */
    public List<SpecValue> getSpecValues(Long specId) {
        return specValueRepository.findBySpecIdOrderBySortOrderAsc(specId);
    }
    
    /**
     * 创建规格值
     */
    @Transactional
    public SpecValue createSpecValue(SpecValue specValue) {
        // 设置默认值
        if (specValue.getSortOrder() == null) {
            specValue.setSortOrder(0);
        }
        
        return specValueRepository.save(specValue);
    }
}
