-- ============================================
-- 数据库迁移脚本：添加缺失字段
-- 执行日期: 2026-05-16
-- 说明: 
--   1. 为 addresses 表添加 is_deleted 字段（软删除支持）
--   2. 更新 orders 表状态注释，添加 DELETED 状态
-- ============================================

USE miniprogram;

-- ========== 第一部分：addresses 表添加 is_deleted 字段 ==========

-- 1. 添加 is_deleted 字段
-- 注意：如果字段已存在会报错，请先检查或注释掉已执行的语句
ALTER TABLE addresses 
ADD COLUMN is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除' AFTER is_default;

-- 2. 添加索引
ALTER TABLE addresses ADD INDEX idx_is_deleted (is_deleted);

-- 4. 验证修改结果
DESCRIBE addresses;


-- ========== 第二部分：更新 orders 表状态注释 ==========

-- 5. 更新 status 字段注释，添加 DELETED 状态说明
ALTER TABLE orders 
MODIFY COLUMN status VARCHAR(16) NOT NULL COMMENT '订单状态：UNPAID-待付款 PAID-已付款 SHIPPED-已发货 FINISHED-已完成 CANCELLED-已取消 DELETED-已删除';

-- 6. 验证修改结果
DESCRIBE orders;


-- ========== 第三部分：数据验证 ==========

-- 7. 查看 addresses 表示例数据
SELECT id, user_id, name, phone, is_default, is_deleted FROM addresses LIMIT 5;

-- 8. 查看 orders 表状态分布
SELECT status, COUNT(*) as count 
FROM orders 
GROUP BY status
ORDER BY status;


-- ============================================
-- 回滚脚本（如果需要恢复）
-- ============================================
-- -- 回滚 addresses 表
-- ALTER TABLE addresses DROP COLUMN is_deleted;
-- ALTER TABLE addresses DROP INDEX idx_is_deleted;

-- -- 回滚 orders 表注释
-- ALTER TABLE orders 
-- MODIFY COLUMN status VARCHAR(16) NOT NULL COMMENT '订单状态：UNPAID-待付款 PAID-已付款 SHIPPED-已发货 FINISHED-已完成 CANCELLED-已取消';
