package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.SpecValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 规格值数据访问接口
 */
@Repository
public interface SpecValueRepository extends JpaRepository<SpecValue, Long> {
    
    /**
     * 根据规格ID查询所有规格值
     */
    List<SpecValue> findBySpecIdOrderBySortOrderAsc(Long specId);
}
