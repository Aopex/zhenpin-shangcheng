package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.domain.DAO.CategoryRepository;
import com.miniprogram.backend.domain.Entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务类
 */
@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired(required = false)
    private com.miniprogram.backend.service.CacheService cacheService;
    
    /**
     * 获取所有显示的分类列表
     * 使用缓存优化
     */
    public List<Category> getAllCategories() {
        // 尝试从缓存获取
        if (cacheService != null) {
            String cacheKey = "categories:all";
            @SuppressWarnings("unchecked")
            List<Category> cachedCategories = (List<Category>) cacheService.get(cacheKey, List.class);
            if (cachedCategories != null) {
                return cachedCategories;
            }
        }
        
        // 从数据库查询
        List<Category> categories = categoryRepository.findByStatusOrderBySortOrderDesc(1);
        
        // 存入缓存（1小时过期）
        if (cacheService != null) {
            cacheService.set("categories:all", categories, 1, java.util.concurrent.TimeUnit.HOURS);
        }
        
        return categories;
    }
    
    /**
     * 根据父分类ID获取子分类列表
     */
    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findByParentIdAndStatusOrderBySortOrderDesc(parentId, 1);
    }
    
    /**
     * 根据ID获取分类详情
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在"));
    }
    
    /**
     * 创建分类
     */
    @Transactional
    public Category createCategory(Category category) {
        // 设置默认值
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        Category savedCategory = categoryRepository.save(category);
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("categories:all");
        }
        
        return savedCategory;
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);
        
        // 更新字段
        if (category.getName() != null) {
            existingCategory.setName(category.getName());
        }
        if (category.getIconUrl() != null) {
            existingCategory.setIconUrl(category.getIconUrl());
        }
        if (category.getSortOrder() != null) {
            existingCategory.setSortOrder(category.getSortOrder());
        }
        if (category.getStatus() != null) {
            existingCategory.setStatus(category.getStatus());
        }
        
        Category updatedCategory = categoryRepository.save(existingCategory);
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("categories:all");
        }
        
        return updatedCategory;
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("categories:all");
        }
    }
}
