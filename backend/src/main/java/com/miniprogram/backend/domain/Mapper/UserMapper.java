package com.miniprogram.backend.domain.Mapper;

import com.miniprogram.backend.domain.Entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User MyBatis Mapper 接口
 * 用于处理复杂查询和批量操作
 */
@Mapper
public interface UserMapper {

    /**
     * 根据注册时间范围查询用户
     */
    List<User> findByCreateTimeRange(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据注册时间范围查询用户（分页）
     */
    List<User> findByCreateTimeRangeWithPage(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("offset") Integer offset,
                                            @Param("limit") Integer limit);
    
    /**
     * 统计注册时间范围内的用户总数
     */
    long countByCreateTimeRange(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 批量插入用户
     */
    int batchInsert(@Param("users") List<User> users);
    
    /**
     * 批量更新用户状态
     */
    int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);
    
    /**
     * 查询活跃用户（最近有登录的用户）
     */
    List<User> findActiveUsers(@Param("days") Integer days);
}
