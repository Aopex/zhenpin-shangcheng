package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.Spec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 规格名数据访问接口
 */
@Repository
public interface SpecRepository extends JpaRepository<Spec, Long> {
    
    /**
     * 根据商品ID查询所有规格名
     */
    List<Spec> findByProductIdOrderBySortOrderAsc(Long productId);
}
