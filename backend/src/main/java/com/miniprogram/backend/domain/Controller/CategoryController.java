package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.domain.Entity.Category;
import com.miniprogram.backend.domain.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * 获取所有分类列表 - 公开访问
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * 根据父分类ID获取子分类列表 - 公开访问
     */
    @GetMapping("/sub/{parentId}")
    public ResponseEntity<ApiResponse<List<Category>>> getSubCategories(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getSubCategories(parentId);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * 根据ID获取分类详情 - 公开访问
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    /**
     * 创建分类 - 需要管理员权限
     */
    @RequireAdmin
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return ResponseEntity.ok(ApiResponse.success(created));
    }
    
    /**
     * 更新分类 - 需要管理员权限
     */
    @RequireAdmin
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
    
    /**
     * 删除分类 - 需要管理员权限
     */
    @RequireAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
