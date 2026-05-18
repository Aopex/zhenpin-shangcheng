-- ============================================
-- 数据库迁移脚本：产品表和订单表字段更新
-- 执行日期: 2026-05-15
-- 更新日期: 2026-05-16
-- 说明: 
--   1. 将 products 表的 main_image 字段改为 image_url
--   2. 为 orders 表添加缺失字段（actual_amount, receiver_name, receiver_phone, receiver_address）
--   3. 更新订单状态命名规范（UNSHIPPED→PAID, UNRECEIVED→SHIPPED）
-- ============================================

-- ========== 第一部分：products 表字段重命名 ==========

-- 1. 重命名 main_image 为 image_url
ALTER TABLE products 
CHANGE COLUMN main_image image_url VARCHAR(512) NOT NULL COMMENT '商品图片URL（如：/pics/iphone15.jpg）';

-- 验证修改结果
DESCRIBE products;


-- ========== 第二部分：orders 表添加缺失字段 ==========

-- 2. 添加 actual_amount 字段
ALTER TABLE orders 
ADD COLUMN actual_amount DECIMAL(10,2) DEFAULT NULL COMMENT '实际支付金额（优惠后）' AFTER total_amount;

-- 3. 添加收货人信息字段
ALTER TABLE orders 
ADD COLUMN receiver_name VARCHAR(32) DEFAULT NULL COMMENT '收货人姓名' AFTER remark;

ALTER TABLE orders 
ADD COLUMN receiver_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人电话' AFTER receiver_name;

ALTER TABLE orders 
ADD COLUMN receiver_address VARCHAR(512) DEFAULT NULL COMMENT '收货地址' AFTER receiver_phone;

-- 4. 更新 total_amount 注释
ALTER TABLE orders 
MODIFY COLUMN total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额';

-- 验证修改结果
DESCRIBE orders;


-- ========== 第三部分：订单状态更新 ==========

-- 检查是否有需要迁移的数据
SELECT 
    status, 
    COUNT(*) as count 
FROM orders 
WHERE status IN ('UNSHIPPED', 'UNRECEIVED')
GROUP BY status;

-- 7. 将 UNSHIPPED（待发货）更新为 PAID（已支付）
UPDATE orders 
SET status = 'PAID' 
WHERE status = 'UNSHIPPED';

-- 8. 将 UNRECEIVED（待收货）更新为 SHIPPED（已发货）
UPDATE orders 
SET status = 'SHIPPED' 
WHERE status = 'UNRECEIVED';

-- 验证迁移结果
SELECT 
    status, 
    COUNT(*) as count 
FROM orders 
GROUP BY status
ORDER BY status;

-- 确认没有遗留的旧状态
SELECT * FROM orders WHERE status IN ('UNSHIPPED', 'UNRECEIVED');
-- 应该返回空结果集


-- ========== 第四部分：数据验证 ==========

-- 9. 查看 products 表示例数据
SELECT id, product_no, title, image_url FROM products LIMIT 5;

-- 10. 查看 orders 表示例数据
SELECT id, order_no, total_amount, actual_amount, receiver_name, receiver_phone, receiver_address 
FROM orders LIMIT 5;


-- ============================================
-- 回滚脚本（如果需要恢复）
-- ============================================
-- -- 回滚 products 表
-- ALTER TABLE products 
-- CHANGE COLUMN image_url main_image VARCHAR(512) NOT NULL COMMENT '商品主图URL';

-- -- 回滚 orders 表字段
-- ALTER TABLE orders DROP COLUMN actual_amount;
-- ALTER TABLE orders DROP COLUMN receiver_name;
-- ALTER TABLE orders DROP COLUMN receiver_phone;
-- ALTER TABLE orders DROP COLUMN receiver_address;
-- ALTER TABLE orders MODIFY COLUMN total_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额';

-- -- 回滚订单状态
-- UPDATE orders SET status = 'UNSHIPPED' WHERE status = 'PAID';
-- UPDATE orders SET status = 'UNRECEIVED' WHERE status = 'SHIPPED';
