package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.UserAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户收货地址 JPA Repository
 * 用于简单的增删改查操作
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    /**
     * 查找用户的所有地址（分页）
     */
    Page<UserAddress> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据ID查找未删除的地址
     */
    Optional<UserAddress> findByIdAndIsDeletedFalse(Long id);
    
    /**
     * 查找用户的所有未删除地址（分页）
     */
    Page<UserAddress> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    
    /**
     * 查找用户的所有未删除地址（列表），默认地址排在前面
     */
    List<UserAddress> findByUserIdAndIsDeletedFalseOrderByIsDefaultDesc(Long userId);
    
    /**
     * 查找用户的默认未删除地址
     */
    Optional<UserAddress> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(Long userId);
    
    /**
     * 查找用户的所有地址（列表），默认地址排在前面
     */
    List<UserAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    /**
     * 根据ID查找地址
     */
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
    
    /**
     * 查找用户的默认地址
     */
    Optional<UserAddress> findByUserIdAndIsDefault(Long userId, Integer isDefault);
    
    /**
     * 取消用户的所有默认地址
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = 0 WHERE ua.userId = :userId")
    void clearDefaultAddresses(@Param("userId") Long userId);
    
    /**
     * 设置指定地址为默认地址
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = 1 WHERE ua.id = :addressId")
    void setAsDefault(@Param("addressId") Long addressId);
    
    /**
     * 统计用户未删除的地址数量
     */
    long countByUserIdAndIsDeletedFalse(Long userId);
    
    /**
     * 软删除地址
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDeleted = 1 WHERE ua.id = :id")
    void softDelete(@Param("id") Long id);
}
