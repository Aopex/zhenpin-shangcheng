package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 商品分类数据访问接口
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * 查询所有显示的分类，按排序权重降序
     */
    List<Category> findByStatusOrderBySortOrderDesc(Integer status);
    
    /**
     * 根据父分类ID查询子分类
     */
    List<Category> findByParentIdAndStatusOrderBySortOrderDesc(Long parentId, Integer status);
}
