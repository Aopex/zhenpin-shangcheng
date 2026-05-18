package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户 JPA Repository
 * 用于简单的增删改查操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据openid查找用户
     */
    Optional<User> findByOpenid(String openid);
    
    /**
     * 根据unionId查找用户
     */
    Optional<User> findByUnionId(String unionId);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 根据状态查找用户（分页）
     */
    Page<User> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据昵称模糊搜索用户（分页）
     */
    Page<User> findByNicknameContaining(String nickname, Pageable pageable);
    
    /**
     * 更新最后登录时间
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :lastLoginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);
    
    /**
     * 禁用用户
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 0 WHERE u.id = :userId")
    void disableUser(@Param("userId") Long userId);
    
    /**
     * 启用用户
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 1 WHERE u.id = :userId")
    void enableUser(@Param("userId") Long userId);
}
