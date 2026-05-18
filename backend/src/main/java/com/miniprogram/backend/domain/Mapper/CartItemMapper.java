package com.miniprogram.backend.domain.Mapper;

import com.miniprogram.backend.domain.Entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 购物车 MyBatis Mapper
 * 用于复杂查询和批量操作
 */
@Mapper
public interface CartItemMapper {
    
    /**
     * 查询用户购物车项列表
     * 注意：产品信息通过Service层单独查询，不在SQL中JOIN
     */
    @Select("SELECT * FROM cart_items WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<CartItem> findCartItemsWithProduct(@Param("userId") Long userId);
    
    /**
     * 查询用户选中的购物车项
     * 注意：产品信息通过Service层单独查询，不在SQL中JOIN
     */
    @Select("SELECT * FROM cart_items WHERE user_id = #{userId} AND checked = 1 ORDER BY created_at DESC")
    List<CartItem> findSelectedCartItemsWithProduct(@Param("userId") Long userId);
    
    /**
     * 批量插入购物车项
     */
    @Insert("<script>" +
            "INSERT INTO cart_items (user_id, product_id, quantity, checked) VALUES " +
            "<foreach collection='cartItems' item='item' separator=','>" +
            "(#{item.userId}, #{item.productId}, #{item.quantity}, #{item.checked})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("cartItems") List<CartItem> cartItems);
    
    /**
     * 批量删除购物车项
     */
    @Delete("<script>" +
            "DELETE FROM cart_items WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids);
}
