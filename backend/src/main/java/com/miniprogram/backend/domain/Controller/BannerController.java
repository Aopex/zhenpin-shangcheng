package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.domain.Entity.Banner;
import com.miniprogram.backend.domain.Service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 */
@RestController
@RequestMapping("/api/banners")
public class BannerController {
    
    @Autowired
    private BannerService bannerService;
    
    /**
     * 获取所有轮播图列表 - 公开访问
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Banner>>> getAllBanners() {
        List<Banner> banners = bannerService.getAllBanners();
        return ResponseEntity.ok(ApiResponse.success(banners));
    }
    
    /**
     * 获取当前有效的轮播图列表 - 公开访问
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Banner>>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.success(banners));
    }
    
    /**
     * 根据ID获取轮播图详情 - 公开访问
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Banner>> getBannerById(@PathVariable Long id) {
        Banner banner = bannerService.getBannerById(id);
        return ResponseEntity.ok(ApiResponse.success(banner));
    }
    
    /**
     * 创建轮播图 - 需要管理员权限
     */
    @RequireAdmin
    @PostMapping
    public ResponseEntity<ApiResponse<Banner>> createBanner(@RequestBody Banner banner) {
        Banner created = bannerService.createBanner(banner);
        return ResponseEntity.ok(ApiResponse.success(created));
    }
    
    /**
     * 更新轮播图 - 需要管理员权限
     */
    @RequireAdmin
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Banner>> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        Banner updated = bannerService.updateBanner(id, banner);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
    
    /**
     * 删除轮播图 - 需要管理员权限
     */
    @RequireAdmin
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
