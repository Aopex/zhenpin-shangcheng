-- ============================================
-- 微信小程序电商商城数据库设计
-- 共13张表：user, category, product, product_image, product_sku, spec, spec_value, banner, cart_item, order, order_item, address, refund
-- ============================================

-- 如果数据库已存在则删除
DROP DATABASE IF EXISTS miniprogram;

-- 创建数据库
CREATE DATABASE miniprogram 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE miniprogram;

-- ============================================
-- 1. user - 用户表
-- ============================================
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键',
    openid VARCHAR(64) NOT NULL UNIQUE COMMENT '微信openid，小程序登录凭证',
    union_id VARCHAR(64) DEFAULT NULL COMMENT '微信unionid，跨平台标识（可选）',
    nickname VARCHAR(64) DEFAULT '微信用户' COMMENT '用户昵称',
    avatar_url VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号（需用户授权）',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    role VARCHAR(16) DEFAULT 'USER' COMMENT '用户角色：USER-普通用户 ADMIN-管理员',
    last_login_time TIMESTAMP NULL DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (id),
    INDEX idx_openid (openid),
    INDEX idx_union_id (union_id),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. category - 商品分类表
-- ============================================
CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类主键',
    name VARCHAR(32) NOT NULL COMMENT '分类名称',
    icon_url VARCHAR(512) DEFAULT NULL COMMENT '分类图标地址（SVG/PNG）',
    sort_order INT DEFAULT 0 COMMENT '排序权重，数值越大越靠前',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏 1-显示',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================
-- 3. product - 商品表
-- ============================================
CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品主键',
    product_no VARCHAR(32) NOT NULL UNIQUE COMMENT '商品编号（如 P101、D101）',
    title VARCHAR(256) NOT NULL COMMENT '商品标题',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价（划线价）',
    category_id BIGINT DEFAULT NULL COMMENT '所属分类',
    image_url VARCHAR(512) NOT NULL COMMENT '商品图片URL（如：/pics/iphone15.jpg）',
    stock INT DEFAULT 0 COMMENT '总库存',
    sales INT DEFAULT 0 COMMENT '累计销量',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    description TEXT DEFAULT NULL COMMENT '商品描述（富文本）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_product_no (product_no),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order),
    INDEX idx_sales (sales),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 4. product_image - 商品图片表
-- ============================================
CREATE TABLE product_images (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '关联商品',
    image_url VARCHAR(512) NOT NULL COMMENT '图片地址',
    image_type TINYINT DEFAULT 1 COMMENT '类型：1-轮播主图 2-详情长图',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_product_id (product_id),
    INDEX idx_image_type (image_type),
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- ============================================
-- 5. product_sku - 商品SKU表（规格）
-- ============================================
CREATE TABLE product_skus (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SKU主键',
    product_id BIGINT NOT NULL COMMENT '关联商品',
    sku_no VARCHAR(32) NOT NULL UNIQUE COMMENT 'SKU编号',
    spec_values VARCHAR(256) NOT NULL COMMENT '规格值组合（如 "朱砂红,M"）',
    price DECIMAL(10,2) DEFAULT NULL COMMENT 'SKU级别定价（为空则取商品价）',
    stock INT DEFAULT 0 COMMENT '该规格库存',
    image_url VARCHAR(512) DEFAULT NULL COMMENT '规格对应图片',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_product_id (product_id),
    INDEX idx_sku_no (sku_no),
    INDEX idx_status (status),
    CONSTRAINT fk_product_sku_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- ============================================
-- 6. spec - 规格名表
-- ============================================
CREATE TABLE specs (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '关联商品',
    name VARCHAR(32) NOT NULL COMMENT '规格名（如"颜色"、"尺码"）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    INDEX idx_product_id (product_id),
    CONSTRAINT fk_spec_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规格名表';

-- ============================================
-- 7. spec_value - 规格值表
-- ============================================
CREATE TABLE spec_values (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    spec_id BIGINT NOT NULL COMMENT '关联规格名',
    value VARCHAR(32) NOT NULL COMMENT '规格值（如"朱砂红"、"M"）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (id),
    INDEX idx_spec_id (spec_id),
    CONSTRAINT fk_spec_value_spec FOREIGN KEY (spec_id) REFERENCES specs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规格值表';

-- ============================================
-- 8. banner - 轮播图表
-- ============================================
CREATE TABLE banners (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(64) DEFAULT NULL COMMENT '轮播图标题（管理用）',
    image_url VARCHAR(512) NOT NULL COMMENT '图片地址',
    link_type TINYINT DEFAULT 1 COMMENT '跳转类型：1-商品详情 2-分类页 3-小程序页面 4-外部链接',
    link_value VARCHAR(256) DEFAULT NULL COMMENT '跳转目标（商品编号/分类ID/页面路径/URL）',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏 1-显示',
    start_time DATETIME DEFAULT NULL COMMENT '生效开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '生效结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- ============================================
-- 9. address - 收货地址表
-- ============================================
CREATE TABLE addresses (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '所属用户',
    name VARCHAR(32) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(32) NOT NULL COMMENT '省',
    city VARCHAR(32) NOT NULL COMMENT '市',
    district VARCHAR(32) NOT NULL COMMENT '区/县',
    detail VARCHAR(256) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址：0-否 1-是',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_default (is_default),
    INDEX idx_is_deleted (is_deleted),
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ============================================
-- 10. cart_item - 购物车表
-- ============================================
CREATE TABLE cart_items (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '所属用户',
    product_id BIGINT NOT NULL COMMENT '关联商品',
    sku_id BIGINT DEFAULT NULL COMMENT '关联SKU',
    quantity INT DEFAULT 1 COMMENT '购买数量',
    checked TINYINT DEFAULT 1 COMMENT '是否选中：0-未选 1-选中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_sku_id (sku_id),
    UNIQUE INDEX idx_user_product_sku (user_id, product_id, sku_id),
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_sku FOREIGN KEY (sku_id) REFERENCES product_skus(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 11. order - 订单表
-- ============================================
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单主键',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号（如 DD20231001001）',
    user_id BIGINT NOT NULL COMMENT '下单用户',
    status VARCHAR(16) NOT NULL COMMENT '订单状态：UNPAID-待付款 PAID-已付款 SHIPPED-已发货 FINISHED-已完成 CANCELLED-已取消 DELETED-已删除',
    total_count INT NOT NULL COMMENT '商品总件数',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    actual_amount DECIMAL(10,2) DEFAULT NULL COMMENT '实际支付金额（优惠后）',
    freight_amount DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    address_snapshot JSON DEFAULT NULL COMMENT '下单时地址快照',
    remark VARCHAR(256) DEFAULT NULL COMMENT '买家备注',
    receiver_name VARCHAR(32) DEFAULT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) DEFAULT NULL COMMENT '收货人电话',
    receiver_address VARCHAR(512) DEFAULT NULL COMMENT '收货地址',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    ship_time DATETIME DEFAULT NULL COMMENT '发货时间',
    receive_time DATETIME DEFAULT NULL COMMENT '收货时间',
    finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 12. order_item - 订单商品表
-- ============================================
CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '关联订单',
    product_id BIGINT NOT NULL COMMENT '关联商品',
    sku_id BIGINT DEFAULT NULL COMMENT '关联SKU',
    product_no VARCHAR(32) NOT NULL COMMENT '商品编号（冗余快照）',
    title VARCHAR(256) NOT NULL COMMENT '商品标题（下单时快照）',
    image_url VARCHAR(512) NOT NULL COMMENT '商品图片（下单时快照）',
    spec_text VARCHAR(128) DEFAULT NULL COMMENT '规格描述（如"朱砂红,M"）',
    price DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
    quantity INT NOT NULL COMMENT '购买数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    INDEX idx_sku_id (sku_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_sku FOREIGN KEY (sku_id) REFERENCES product_skus(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品表';

-- ============================================
-- 13. refund - 退款表
-- ============================================
CREATE TABLE refunds (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    refund_no VARCHAR(32) NOT NULL UNIQUE COMMENT '退款单号',
    order_id BIGINT NOT NULL COMMENT '关联订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号（冗余）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    reason VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
    status VARCHAR(16) NOT NULL COMMENT '退款状态：PENDING-待审核 APPROVED-已批准 REJECTED-已拒绝 REFUNDED-已退款',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    refund_time DATETIME DEFAULT NULL COMMENT '退款时间',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handler_id BIGINT DEFAULT NULL COMMENT '处理人ID（管理员）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_refund_no (refund_no),
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    CONSTRAINT fk_refund_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_refund_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款表';
