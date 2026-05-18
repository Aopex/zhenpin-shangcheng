package com.miniprogram.backend.domain.Mapper;

import com.miniprogram.backend.domain.Entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Product MyBatis Mapper 接口
 * 用于处理复杂查询和批量操作
 */
@Mapper
public interface ProductMapper {

    /**
     * 根据价格范围查询产品（使用XML配置方式，在ProductMapper.xml中定义）
     */
    List<Product> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * 根据价格范围查询产品（分页）
     */
    List<Product> findByPriceRangeWithPage(@Param("minPrice") Double minPrice, 
                                          @Param("maxPrice") Double maxPrice,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);

    /**
     * 统计价格范围内的产品总数
     */
    long countByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);

    /**
     * 批量插入产品（使用XML配置方式）
     */
    int batchInsert(@Param("products") List<Product> products);
}
