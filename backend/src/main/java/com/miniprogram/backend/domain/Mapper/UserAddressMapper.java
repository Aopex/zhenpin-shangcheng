package com.miniprogram.backend.domain.Mapper;

import com.miniprogram.backend.domain.Entity.UserAddress;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * UserAddress MyBatis Mapper 接口
 * 用于处理复杂查询和批量操作
 */
@Mapper
public interface UserAddressMapper {

    /**
     * 根据收货人姓名模糊搜索地址
     */
    List<UserAddress> findByReceiverNameContaining(@Param("receiverName") String receiverName);
    
    /**
     * 根据收货人姓名模糊搜索地址（分页）
     */
    List<UserAddress> findByReceiverNameContainingWithPage(
            @Param("receiverName") String receiverName,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);
    
    /**
     * 统计收货人姓名搜索结果总数
     */
    long countByReceiverNameContaining(@Param("receiverName") String receiverName);
    
    /**
     * 根据收货人手机号查找地址
     */
    List<UserAddress> findByReceiverPhone(@Param("receiverPhone") String receiverPhone);
    
    /**
     * 根据收货人手机号查找地址（分页）
     */
    List<UserAddress> findByReceiverPhoneWithPage(
            @Param("receiverPhone") String receiverPhone,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);
    
    /**
     * 统计手机号搜索结果总数
     */
    long countByReceiverPhone(@Param("receiverPhone") String receiverPhone);
    
    /**
     * 根据省市查询地址
     */
    List<UserAddress> findByProvinceAndCity(@Param("province") String province, 
                                           @Param("city") String city);
    
    /**
     * 根据省市查询地址（分页）
     */
    List<UserAddress> findByProvinceAndCityWithPage(
            @Param("province") String province,
            @Param("city") String city,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);
    
    /**
     * 统计省市查询结果总数
     */
    long countByProvinceAndCity(@Param("province") String province, @Param("city") String city);
    
    /**
     * 批量设置默认地址（先取消所有，再设置新的）
     */
    int batchSetDefault(@Param("userId") Long userId, @Param("addressId") Long addressId);
    
    /**
     * 批量逻辑删除用户的地址
     */
    int batchSoftDelete(@Param("userIds") List<Long> userIds);
    
    /**
     * 统计每个用户的地址数量
     */
    @Select("SELECT user_id, COUNT(*) as count FROM addresses WHERE is_deleted = 0 GROUP BY user_id")
    @Results({
        @Result(property = "userId", column = "user_id"),
        @Result(property = "count", column = "count")
    })
    List<UserAddressCount> countAddressesByUser();
    
    /**
     * 内部类：用户地址统计
     */
    class UserAddressCount {
        private Long userId;
        private Long count;
        
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public Long getCount() {
            return count;
        }
        
        public void setCount(Long count) {
            this.count = count;
        }
    }
}
