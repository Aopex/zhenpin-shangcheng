package com.miniprogram.backend.domain.DAO;

import com.miniprogram.backend.domain.Entity.Refund;
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
 * 退款 JPA Repository
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    /**
     * 根据退款单号查询
     */
    Optional<Refund> findByRefundNo(String refundNo);
    
    /**
     * 根据订单ID查询退款记录
     */
    List<Refund> findByOrderId(Long orderId);
    
    /**
     * 根据用户ID查询退款记录（分页）
     */
    Page<Refund> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据状态查询退款记录（分页）
     */
    Page<Refund> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * 根据用户ID和状态查询退款记录（分页）
     */
    Page<Refund> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);
    
    /**
     * 更新退款状态为已批准
     */
    @Modifying
    @Query("UPDATE Refund r SET r.status = 'APPROVED', r.handleTime = CURRENT_TIMESTAMP, r.handlerId = :handlerId WHERE r.id = :refundId")
    void approveRefund(@Param("refundId") Long refundId, @Param("handlerId") Long handlerId);
    
    /**
     * 更新退款状态为已拒绝
     */
    @Modifying
    @Query("UPDATE Refund r SET r.status = 'REJECTED', r.rejectReason = :rejectReason, r.handleTime = CURRENT_TIMESTAMP, r.handlerId = :handlerId WHERE r.id = :refundId")
    void rejectRefund(@Param("refundId") Long refundId, @Param("rejectReason") String rejectReason, @Param("handlerId") Long handlerId);
    
    /**
     * 更新退款状态为已退款
     */
    @Modifying
    @Query("UPDATE Refund r SET r.status = 'REFUNDED', r.refundTime = CURRENT_TIMESTAMP WHERE r.id = :refundId")
    void markAsRefunded(@Param("refundId") Long refundId);
}
