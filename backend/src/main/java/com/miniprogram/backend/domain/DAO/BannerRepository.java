package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图数据访问接口
 */
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    
    /**
     * 查询所有显示的轮播图，按排序权重降序
     */
    List<Banner> findByStatusOrderBySortOrderDesc(Integer status);
    
    /**
     * 查询在有效期内的轮播图
     */
    List<Banner> findByStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderBySortOrderDesc(
            Integer status, LocalDateTime now, LocalDateTime now2);
}
