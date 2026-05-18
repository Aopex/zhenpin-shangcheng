package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.domain.DAO.BannerRepository;
import com.miniprogram.backend.domain.Entity.Banner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务类
 */
@Service
public class BannerService {
    
    @Autowired
    private BannerRepository bannerRepository;
    
    @Autowired(required = false)
    private com.miniprogram.backend.service.CacheService cacheService;
    
    /**
     * 获取所有显示的轮播图列表
     * 使用缓存优化
     */
    public List<Banner> getAllBanners() {
        // 尝试从缓存获取
        if (cacheService != null) {
            String cacheKey = "banners:all";
            @SuppressWarnings("unchecked")
            List<Banner> cachedBanners = (List<Banner>) cacheService.get(cacheKey, List.class);
            if (cachedBanners != null) {
                return cachedBanners;
            }
        }
        
        // 从数据库查询
        List<Banner> banners = bannerRepository.findByStatusOrderBySortOrderDesc(1);
        
        // 存入缓存（30分钟过期）
        if (cacheService != null) {
            cacheService.set("banners:all", banners, 30, java.util.concurrent.TimeUnit.MINUTES);
        }
        
        return banners;
    }
    
    /**
     * 获取当前有效的轮播图列表
     * 使用缓存优化
     */
    public List<Banner> getActiveBanners() {
        // 尝试从缓存获取
        if (cacheService != null) {
            String cacheKey = "banners:active";
            @SuppressWarnings("unchecked")
            List<Banner> cachedBanners = (List<Banner>) cacheService.get(cacheKey, List.class);
            if (cachedBanners != null) {
                return cachedBanners;
            }
        }
        
        // 从数据库查询
        LocalDateTime now = LocalDateTime.now();
        List<Banner> banners = bannerRepository.findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderBySortOrderDesc(1, now, now);
        
        // 存入缓存（10分钟过期，因为时间敏感）
        if (cacheService != null) {
            cacheService.set("banners:active", banners, 10, java.util.concurrent.TimeUnit.MINUTES);
        }
        
        return banners;
    }
    
    /**
     * 根据ID获取轮播图详情
     */
    public Banner getBannerById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("轮播图不存在"));
    }
    
    /**
     * 创建轮播图
     */
    @Transactional
    public Banner createBanner(Banner banner) {
        // 设置默认值
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getLinkType() == null) {
            banner.setLinkType(1);
        }
        
        Banner savedBanner = bannerRepository.save(banner);
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("banners:all");
            cacheService.delete("banners:active");
        }
        
        return savedBanner;
    }
    
    /**
     * 更新轮播图
     */
    @Transactional
    public Banner updateBanner(Long id, Banner banner) {
        Banner existingBanner = getBannerById(id);
        
        // 更新字段
        if (banner.getTitle() != null) {
            existingBanner.setTitle(banner.getTitle());
        }
        if (banner.getImageUrl() != null) {
            existingBanner.setImageUrl(banner.getImageUrl());
        }
        if (banner.getLinkType() != null) {
            existingBanner.setLinkType(banner.getLinkType());
        }
        if (banner.getLinkValue() != null) {
            existingBanner.setLinkValue(banner.getLinkValue());
        }
        if (banner.getSortOrder() != null) {
            existingBanner.setSortOrder(banner.getSortOrder());
        }
        if (banner.getStatus() != null) {
            existingBanner.setStatus(banner.getStatus());
        }
        if (banner.getStartTime() != null) {
            existingBanner.setStartTime(banner.getStartTime());
        }
        if (banner.getEndTime() != null) {
            existingBanner.setEndTime(banner.getEndTime());
        }
        
        Banner updatedBanner = bannerRepository.save(existingBanner);
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("banners:all");
            cacheService.delete("banners:active");
        }
        
        return updatedBanner;
    }
    
    /**
     * 删除轮播图
     */
    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = getBannerById(id);
        bannerRepository.delete(banner);
        
        // 清除缓存
        if (cacheService != null) {
            cacheService.delete("banners:all");
            cacheService.delete("banners:active");
        }
    }
}
