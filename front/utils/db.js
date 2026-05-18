/**
 * utils/db.js
 * 统一 Mock 数据源 — 12 张表，保证外键关联
 * 所有页面共享这份数据，不再各自硬编码
 */

// ==================== 1. user 用户表 ====================
const user = [
  {
    id: 1,
    openid: 'mock_openid_001',
    union_id: null,
    nickname: 'zmjjKK',
    avatar_url: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=140&h=140&fit=crop&auto=format',
    phone: '138****8888',
    gender: 1,
    status: 1,
    created_at: '2024-01-15 10:00:00',
    updated_at: '2024-06-01 12:00:00'
  }
]

// ==================== 2. category 商品分类表 ====================
const category = [
  { id: 1, name: '数码家电', icon_url: '/assets/icon/home/shouji.svg', sort_order: 100, parent_id: 0, status: 1 },
  { id: 2, name: '服饰鞋包', icon_url: '/assets/icon/home/yifu.svg',   sort_order: 90,  parent_id: 0, status: 1 },
  { id: 3, name: '居家日用', icon_url: '/assets/icon/home/jiaju.svg',   sort_order: 80,  parent_id: 0, status: 1 },
  { id: 4, name: '食品饮料', icon_url: '/assets/icon/home/shiwu.svg',   sort_order: 70,  parent_id: 0, status: 1 },
  { id: 5, name: '美妆个护', icon_url: '/assets/icon/home/meizhuang.svg', sort_order: 60, parent_id: 0, status: 1 },
  { id: 6, name: '运动户外', icon_url: '/assets/icon/home/yundong.svg', sort_order: 50,  parent_id: 0, status: 1 },
  { id: 7, name: '母婴宠物', icon_url: '/assets/icon/home/muying.svg',  sort_order: 40,  parent_id: 0, status: 1 },
  { id: 8, name: '文化办公', icon_url: '/assets/icon/home/gengduo.svg', sort_order: 30,  parent_id: 0, status: 1 },
  { id: 9, name: '虚拟服务', icon_url: '/assets/icon/home/shouji.svg', sort_order: 20,  parent_id: 0, status: 1 },
  { id: 10, name: '其他综合', icon_url: '/assets/icon/home/gengduo.svg', sort_order: 10,  parent_id: 0, status: 1 }
]

// ==================== 3. product 商品表 ====================
const product = [
  // ---- 数码家电 (category_id=1) ----
  { id: 1,  product_no: 'D101', title: '全新旗舰 5G 智能手机 120Hz 高刷屏 256G',    price: 4999.00, original_price: 5999.00, category_id: 1, main_image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=335&h=335&fit=crop&auto=format', stock: 300, sales: 1258, status: 1, sort_order: 100, created_at: '2024-06-12 09:20:00', updated_at: '2024-06-12 09:20:00' },
  { id: 2,  product_no: 'D102', title: '轻薄笔记本电脑 16G+512G 全金属机身',        price: 5299.00, original_price: 6299.00, category_id: 1, main_image: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=335&h=335&fit=crop&auto=format', stock: 200, sales: 867,  status: 1, sort_order: 90, created_at: '2024-06-10 14:15:00', updated_at: '2024-06-10 14:15:00' },
  { id: 3,  product_no: 'D103', title: '降噪蓝牙耳机 主动降噪 超长续航',            price: 299.00,  original_price: 399.00,  category_id: 1, main_image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=335&h=335&fit=crop&auto=format', stock: 500, sales: 2340, status: 1, sort_order: 80, created_at: '2024-06-08 16:40:00', updated_at: '2024-06-08 16:40:00' },
  // ---- 服饰鞋包 (category_id=2) ----
  { id: 4,  product_no: 'F201', title: '夏季纯棉短袖T恤 宽松潮流百搭',              price: 59.00,   original_price: 99.00,   category_id: 2, main_image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=335&h=335&fit=crop&auto=format', stock: 800, sales: 5621, status: 1, sort_order: 100, created_at: '2024-06-13 11:05:00', updated_at: '2024-06-13 11:05:00' },
  { id: 5,  product_no: 'F202', title: '韩版修身牛仔裤 九分直筒显瘦',                price: 129.00,  original_price: 199.00,  category_id: 2, main_image: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=335&h=335&fit=crop&auto=format', stock: 600, sales: 3210, status: 1, sort_order: 90, created_at: '2024-06-09 10:30:00', updated_at: '2024-06-09 10:30:00' },
  { id: 6,  product_no: 'F203', title: '真皮手提单肩包 通勤百搭大容量',              price: 359.00,  original_price: 499.00,  category_id: 2, main_image: 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=335&h=335&fit=crop&auto=format', stock: 150, sales: 980,  status: 1, sort_order: 80, created_at: '2024-06-02 08:45:00', updated_at: '2024-06-02 08:45:00' },
  // ---- 居家日用 (category_id=3) ----
  { id: 7,  product_no: 'J301', title: '北欧风全棉四件套 纯色亲肤透气',              price: 269.00,  original_price: 399.00,  category_id: 3, main_image: 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=335&h=335&fit=crop&auto=format', stock: 400, sales: 1876, status: 1, sort_order: 100, created_at: '2024-06-07 13:20:00', updated_at: '2024-06-07 13:20:00' },
  { id: 8,  product_no: 'J302', title: '304不锈钢保温杯 大容量商务 500ml',           price: 128.00,  original_price: 168.00,  category_id: 3, main_image: 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=335&h=335&fit=crop&auto=format', stock: 700, sales: 4523, status: 1, sort_order: 90, created_at: '2024-06-05 18:10:00', updated_at: '2024-06-05 18:10:00' },
  { id: 9,  product_no: 'J303', title: '日式陶瓷餐具套装 碗碟筷子8件套',              price: 89.00,   original_price: 129.00,  category_id: 3, main_image: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=335&h=335&fit=crop&auto=format', stock: 350, sales: 2100, status: 1, sort_order: 80, created_at: '2024-06-01 12:00:00', updated_at: '2024-06-01 12:00:00' },
  // ---- 食品饮料 (category_id=4) ----
  { id: 10, product_no: 'S401', title: '进口车厘子 鲜甜多汁 顺丰直达 2斤',           price: 88.00,   original_price: 128.00,  category_id: 4, main_image: 'https://images.unsplash.com/photo-1528821128474-27f963b062bf?w=335&h=335&fit=crop&auto=format', stock: 200, sales: 8932, status: 1, sort_order: 100, created_at: '2024-06-14 09:35:00', updated_at: '2024-06-14 09:35:00' },
  { id: 11, product_no: 'S402', title: '原味坚果大礼包 每日坚果混合装 750g',          price: 139.00,  original_price: 199.00,  category_id: 4, main_image: 'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=335&h=335&fit=crop&auto=format', stock: 500, sales: 6540, status: 1, sort_order: 90, created_at: '2024-06-11 17:25:00', updated_at: '2024-06-11 17:25:00' },
  { id: 12, product_no: 'S403', title: '西湖龙井 明前特级 礼盒装',                   price: 499.00,  original_price: 699.00,  category_id: 4, main_image: 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=335&h=335&fit=crop&auto=format', stock: 100, sales: 1230, status: 1, sort_order: 80, created_at: '2024-06-03 15:50:00', updated_at: '2024-06-03 15:50:00' },
  // ---- 美妆个护 (category_id=5) ----
  { id: 13, product_no: 'M501', title: '保湿补水面膜 烟酰胺提亮 10片装',              price: 99.00,   original_price: 159.00,  category_id: 5, main_image: 'https://images.unsplash.com/photo-1596755389378-c31d21fd1273?w=335&h=335&fit=crop&auto=format', stock: 1000, sales: 12050, status: 1, sort_order: 100, created_at: '2024-06-15 10:18:00', updated_at: '2024-06-15 10:18:00' },
  { id: 14, product_no: 'M502', title: '氨基酸洗面奶 温和洁净 敏感肌适用',            price: 69.00,   original_price: 99.00,   category_id: 5, main_image: 'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=335&h=335&fit=crop&auto=format', stock: 800, sales: 9870, status: 1, sort_order: 90, created_at: '2024-06-06 09:55:00', updated_at: '2024-06-06 09:55:00' },
  // ---- 运动户外 (category_id=6) ----
  { id: 15, product_no: 'Y601', title: '智能运动手环 心率血氧监测 IP68防水',          price: 199.00,  original_price: 299.00,  category_id: 6, main_image: 'https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=335&h=335&fit=crop&auto=format', stock: 600, sales: 4320, status: 1, sort_order: 100, created_at: '2024-06-04 20:05:00', updated_at: '2024-06-04 20:05:00' },
  { id: 16, product_no: 'Y602', title: '专业跑鞋 碳板助力 超轻透气',                  price: 459.00,  original_price: 599.00,  category_id: 6, main_image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=335&h=335&fit=crop&auto=format', stock: 300, sales: 2780, status: 1, sort_order: 90, created_at: '2024-05-30 16:15:00', updated_at: '2024-05-30 16:15:00' },
  // ---- 母婴宠物 (category_id=7) ----
  { id: 17, product_no: 'B701', title: '婴儿纯棉连体衣 新生儿A类标准',                price: 79.00,   original_price: 119.00,  category_id: 7, main_image: 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=335&h=335&fit=crop&auto=format', stock: 500, sales: 3450, status: 1, sort_order: 100, created_at: '2024-05-28 11:40:00', updated_at: '2024-05-28 11:40:00' },
  { id: 18, product_no: 'B702', title: '全价猫粮 鲜鸡肉配方 10kg装',                  price: 189.00,  original_price: 259.00,  category_id: 7, main_image: 'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=335&h=335&fit=crop&auto=format', stock: 400, sales: 5670, status: 1, sort_order: 90, created_at: '2024-05-26 09:10:00', updated_at: '2024-05-26 09:10:00' },
  // ---- 文化办公 (category_id=8) ----
  { id: 19, product_no: 'W801', title: '儿童绘画套装 水彩颜料36色 全套画材',          price: 59.00,   original_price: 89.00,   category_id: 8, main_image: 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=335&h=335&fit=crop&auto=format', stock: 300, sales: 2100, status: 1, sort_order: 100, created_at: '2024-05-24 19:30:00', updated_at: '2024-05-24 19:30:00' },
  { id: 20, product_no: 'W802', title: '人体工学办公椅 网布透气 可躺午休',            price: 899.00,  original_price: 1299.00, category_id: 8, main_image: 'https://images.unsplash.com/photo-1580480055273-228ff5388ef8?w=335&h=335&fit=crop&auto=format', stock: 100, sales: 890,  status: 1, sort_order: 90, created_at: '2024-05-20 10:00:00', updated_at: '2024-05-20 10:00:00' },
  // ---- 虚拟服务 (category_id=9) ----
  { id: 21, product_no: 'X901', title: '视频平台年度VIP会员 全屏通用',                price: 198.00,  original_price: 258.00,  category_id: 9, main_image: 'https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=335&h=335&fit=crop&auto=format', stock: 9999, sales: 8760, status: 1, sort_order: 100, created_at: '2024-06-16 10:00:00', updated_at: '2024-06-16 10:00:00' },
  { id: 22, product_no: 'X902', title: '手机话费充值 100元面值 即时到账',              price: 98.50,   original_price: 100.00,  category_id: 9, main_image: 'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=335&h=335&fit=crop&auto=format', stock: 9999, sales: 15230, status: 1, sort_order: 90, created_at: '2024-06-16 10:00:00', updated_at: '2024-06-16 10:00:00' },
  { id: 23, product_no: 'X903', title: '在线编程课程 Python全栈开发 120课时',           price: 299.00,  original_price: 599.00,  category_id: 9, main_image: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=335&h=335&fit=crop&auto=format', stock: 9999, sales: 3420, status: 1, sort_order: 80, created_at: '2024-06-16 10:00:00', updated_at: '2024-06-16 10:00:00' },
  // ---- 其他综合 (category_id=10) ----
  { id: 24, product_no: 'Q101', title: '汽车临时停车号码牌 创意磁吸式',                price: 29.90,   original_price: 49.90,   category_id: 10, main_image: 'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=335&h=335&fit=crop&auto=format', stock: 500, sales: 4210, status: 1, sort_order: 100, created_at: '2024-06-16 10:00:00', updated_at: '2024-06-16 10:00:00' },
  { id: 25, product_no: 'Q102', title: '创意生日礼物盒 手工DIY材料包',                 price: 39.00,   original_price: 69.00,   category_id: 10, main_image: 'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?w=335&h=335&fit=crop&auto=format', stock: 300, sales: 1890, status: 1, sort_order: 90, created_at: '2024-06-16 10:00:00', updated_at: '2024-06-16 10:00:00' }
]

// ==================== 4. product_image 商品图片表 ====================
const product_image = [
  // D101 手机 (product_id=1)
  { id: 1,  product_id: 1, image_url: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 2,  product_id: 1, image_url: 'https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 3,  product_id: 1, image_url: 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 3 },
  { id: 4,  product_id: 1, image_url: 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  { id: 5,  product_id: 1, image_url: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 2 },
  // D102 笔记本 (product_id=2)
  { id: 6,  product_id: 2, image_url: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 7,  product_id: 2, image_url: 'https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 8,  product_id: 2, image_url: 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // D103 耳机 (product_id=3)
  { id: 9,  product_id: 3, image_url: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 10, product_id: 3, image_url: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 11, product_id: 3, image_url: 'https://images.unsplash.com/photo-1583394838336-acd977736f90?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // F201 T恤 (product_id=4)
  { id: 12, product_id: 4, image_url: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 13, product_id: 4, image_url: 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 14, product_id: 4, image_url: 'https://images.unsplash.com/photo-1622445275463-afa2ab738c34?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // F202 牛仔裤 (product_id=5)
  { id: 15, product_id: 5, image_url: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 16, product_id: 5, image_url: 'https://images.unsplash.com/photo-1582552938357-32b906df40cb?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 17, product_id: 5, image_url: 'https://images.unsplash.com/photo-1604176354204-9268737828e4?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // F203 包 (product_id=6)
  { id: 18, product_id: 6, image_url: 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 19, product_id: 6, image_url: 'https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 20, product_id: 6, image_url: 'https://images.unsplash.com/photo-1594223274512-ad4803739b7c?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // J301 四件套 (product_id=7)
  { id: 21, product_id: 7, image_url: 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 22, product_id: 7, image_url: 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 23, product_id: 7, image_url: 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // J302 保温杯 (product_id=8)
  { id: 24, product_id: 8, image_url: 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 25, product_id: 8, image_url: 'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 26, product_id: 8, image_url: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // J303 餐具 (product_id=9)
  { id: 27, product_id: 9, image_url: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 28, product_id: 9, image_url: 'https://images.unsplash.com/photo-1584568694244-14fbdf83bd30?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 29, product_id: 9, image_url: 'https://images.unsplash.com/photo-1590794056226-79ef3a8147e1?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // S401 车厘子 (product_id=10)
  { id: 30, product_id: 10, image_url: 'https://images.unsplash.com/photo-1528821128474-27f963b062bf?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 31, product_id: 10, image_url: 'https://images.unsplash.com/photo-1559181567-c3190ca9959b?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 32, product_id: 10, image_url: 'https://images.unsplash.com/photo-1490885578174-acda8905c2c6?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // S402 坚果 (product_id=11)
  { id: 33, product_id: 11, image_url: 'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 34, product_id: 11, image_url: 'https://images.unsplash.com/photo-1551024601-bec78aea704b?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 35, product_id: 11, image_url: 'https://images.unsplash.com/photo-1551024601-bec78aea704b?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // S403 龙井 (product_id=12)
  { id: 36, product_id: 12, image_url: 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 37, product_id: 12, image_url: 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 38, product_id: 12, image_url: 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // M501 面膜 (product_id=13)
  { id: 39, product_id: 13, image_url: 'https://images.unsplash.com/photo-1596755389378-c31d21fd1273?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 40, product_id: 13, image_url: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 41, product_id: 13, image_url: 'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // M502 洗面奶 (product_id=14)
  { id: 42, product_id: 14, image_url: 'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 43, product_id: 14, image_url: 'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 44, product_id: 14, image_url: 'https://images.unsplash.com/photo-1571781926291-c477ebfd024b?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // Y601 手环 (product_id=15)
  { id: 45, product_id: 15, image_url: 'https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 46, product_id: 15, image_url: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 47, product_id: 15, image_url: 'https://images.unsplash.com/photo-1434494878577-86c23bcb06b9?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // Y602 跑鞋 (product_id=16)
  { id: 48, product_id: 16, image_url: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 49, product_id: 16, image_url: 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 50, product_id: 16, image_url: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // B701 连体衣 (product_id=17)
  { id: 51, product_id: 17, image_url: 'https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 52, product_id: 17, image_url: 'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 53, product_id: 17, image_url: 'https://images.unsplash.com/photo-1504439904031-93ded9f93e4e?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // B702 猫粮 (product_id=18)
  { id: 54, product_id: 18, image_url: 'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 55, product_id: 18, image_url: 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 56, product_id: 18, image_url: 'https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // W801 画笔 (product_id=19)
  { id: 57, product_id: 19, image_url: 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 58, product_id: 19, image_url: 'https://images.unsplash.com/photo-1560421683-6856ea585c78?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 59, product_id: 19, image_url: 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // W802 办公椅 (product_id=20)
  { id: 60, product_id: 20, image_url: 'https://images.unsplash.com/photo-1580480055273-228ff5388ef8?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 61, product_id: 20, image_url: 'https://images.unsplash.com/photo-1567016432779-094069958ea5?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  { id: 62, product_id: 20, image_url: 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // X901 视频VIP (product_id=21)
  { id: 63, product_id: 21, image_url: 'https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 64, product_id: 21, image_url: 'https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  // X902 话费充值 (product_id=22)
  { id: 65, product_id: 22, image_url: 'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  // X903 编程课程 (product_id=23)
  { id: 66, product_id: 23, image_url: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  { id: 67, product_id: 23, image_url: 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 2 },
  // Q101 停车牌 (product_id=24)
  { id: 68, product_id: 24, image_url: 'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  // Q102 DIY礼物 (product_id=25)
  { id: 69, product_id: 25, image_url: 'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?w=750&h=750&fit=crop&auto=format', image_type: 1, sort_order: 1 },
  // ---- 详情长图 (image_type=2) ----
  // X901 视频VIP (product_id=21)
  { id: 70, product_id: 21, image_url: 'https://images.unsplash.com/photo-1586899028174-e7098604235b?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  { id: 71, product_id: 21, image_url: 'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 2 },
  // X902 话费充值 (product_id=22)
  { id: 72, product_id: 22, image_url: 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // X903 编程课程 (product_id=23)
  { id: 73, product_id: 23, image_url: 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  { id: 74, product_id: 23, image_url: 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 2 },
  // Q101 停车牌 (product_id=24)
  { id: 75, product_id: 24, image_url: 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  // Q102 DIY礼物 (product_id=25)
  { id: 76, product_id: 25, image_url: 'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 1 },
  { id: 77, product_id: 25, image_url: 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=750&h=1200&fit=crop&auto=format', image_type: 2, sort_order: 2 }
]

// ==================== 5. product_sku 商品 SKU 表 ====================
const product_sku = [
  // D101 手机 (product_id=1): 颜色 × 存储
  { id: 1,  product_id: 1, sku_no: 'SKU-D101-BK-128',  spec_values: '曜石黑,128G',  price: 4999.00, stock: 100, image_url: null, status: 1 },
  { id: 2,  product_id: 1, sku_no: 'SKU-D101-BK-256',  spec_values: '曜石黑,256G',  price: 5299.00, stock: 80,  image_url: null, status: 1 },
  { id: 3,  product_id: 1, sku_no: 'SKU-D101-SL-128',  spec_values: '星光银,128G',  price: 4999.00, stock: 60,  image_url: null, status: 1 },
  { id: 4,  product_id: 1, sku_no: 'SKU-D101-SL-256',  spec_values: '星光银,256G',  price: 5299.00, stock: 60,  image_url: null, status: 1 },
  // D102 笔记本 (product_id=2): 颜色 × 内存
  { id: 5,  product_id: 2, sku_no: 'SKU-D102-GR-16',   spec_values: '深空灰,16G',   price: 5299.00, stock: 80,  image_url: null, status: 1 },
  { id: 6,  product_id: 2, sku_no: 'SKU-D102-GR-32',   spec_values: '深空灰,32G',   price: 5999.00, stock: 50,  image_url: null, status: 1 },
  { id: 7,  product_id: 2, sku_no: 'SKU-D102-SV-16',   spec_values: '银色,16G',     price: 5299.00, stock: 70,  image_url: null, status: 1 },
  // D103 耳机 (product_id=3): 颜色
  { id: 8,  product_id: 3, sku_no: 'SKU-D103-BK',      spec_values: '典雅黑',       price: 299.00,  stock: 200, image_url: null, status: 1 },
  { id: 9,  product_id: 3, sku_no: 'SKU-D103-WH',      spec_values: '云白色',       price: 299.00,  stock: 200, image_url: null, status: 1 },
  { id: 10, product_id: 3, sku_no: 'SKU-D103-BU',      spec_values: '雾霾蓝',       price: 319.00,  stock: 100, image_url: null, status: 1 },
  // F201 T恤 (product_id=4): 颜色 × 尺码
  { id: 11, product_id: 4, sku_no: 'SKU-F201-BK-S',    spec_values: '经典黑,S',     price: 59.00,  stock: 100, image_url: null, status: 1 },
  { id: 12, product_id: 4, sku_no: 'SKU-F201-BK-M',    spec_values: '经典黑,M',     price: 59.00,  stock: 150, image_url: null, status: 1 },
  { id: 13, product_id: 4, sku_no: 'SKU-F201-BK-L',    spec_values: '经典黑,L',     price: 59.00,  stock: 120, image_url: null, status: 1 },
  { id: 14, product_id: 4, sku_no: 'SKU-F201-WH-S',    spec_values: '纯白,S',       price: 59.00,  stock: 80,  image_url: null, status: 1 },
  { id: 15, product_id: 4, sku_no: 'SKU-F201-WH-M',    spec_values: '纯白,M',       price: 59.00,  stock: 130, image_url: null, status: 1 },
  { id: 16, product_id: 4, sku_no: 'SKU-F201-WH-L',    spec_values: '纯白,L',       price: 59.00,  stock: 100, image_url: null, status: 1 },
  { id: 17, product_id: 4, sku_no: 'SKU-F201-GY-M',    spec_values: '灰色,M',       price: 59.00,  stock: 0,   image_url: null, status: 1 },
  // F202 牛仔裤 (product_id=5): 颜色 × 尺码
  { id: 18, product_id: 5, sku_no: 'SKU-F202-BL-28',   spec_values: '经典蓝,28',    price: 129.00, stock: 100, image_url: null, status: 1 },
  { id: 19, product_id: 5, sku_no: 'SKU-F202-BL-29',   spec_values: '经典蓝,29',    price: 129.00, stock: 150, image_url: null, status: 1 },
  { id: 20, product_id: 5, sku_no: 'SKU-F202-BL-30',   spec_values: '经典蓝,30',    price: 129.00, stock: 120, image_url: null, status: 1 },
  { id: 21, product_id: 5, sku_no: 'SKU-F202-BK-29',   spec_values: '深黑,29',      price: 129.00, stock: 80,  image_url: null, status: 1 },
  { id: 22, product_id: 5, sku_no: 'SKU-F202-BK-30',   spec_values: '深黑,30',      price: 129.00, stock: 100, image_url: null, status: 1 },
  // F203 包 (product_id=6): 颜色
  { id: 23, product_id: 6, sku_no: 'SKU-F203-BR',      spec_values: '复古棕',       price: 359.00, stock: 60,  image_url: null, status: 1 },
  { id: 24, product_id: 6, sku_no: 'SKU-F203-BK',      spec_values: '经典黑',       price: 359.00, stock: 90,  image_url: null, status: 1 },
  // J301 四件套 (product_id=7): 颜色 × 尺码
  { id: 25, product_id: 7, sku_no: 'SKU-J301-GY-15',   spec_values: '高级灰,1.5m',  price: 269.00, stock: 100, image_url: null, status: 1 },
  { id: 26, product_id: 7, sku_no: 'SKU-J301-GY-18',   spec_values: '高级灰,1.8m',  price: 299.00, stock: 80,  image_url: null, status: 1 },
  { id: 27, product_id: 7, sku_no: 'SKU-J301-BG-15',   spec_values: '米杏色,1.5m',  price: 269.00, stock: 120, image_url: null, status: 1 },
  { id: 28, product_id: 7, sku_no: 'SKU-J301-BG-18',   spec_values: '米杏色,1.8m',  price: 299.00, stock: 100, image_url: null, status: 1 },
  // J302 保温杯 (product_id=8): 颜色
  { id: 29, product_id: 8, sku_no: 'SKU-J302-BK',      spec_values: '经典黑',       price: 128.00, stock: 300, image_url: null, status: 1 },
  { id: 30, product_id: 8, sku_no: 'SKU-J302-SV',      spec_values: '银色',         price: 128.00, stock: 400, image_url: null, status: 1 },
  // J303 餐具 (product_id=9): 无规格 (单一SKU)
  { id: 31, product_id: 9, sku_no: 'SKU-J303-DF',      spec_values: '默认',         price: 89.00,  stock: 350, image_url: null, status: 1 },
  // S401 车厘子 (product_id=10): 规格
  { id: 32, product_id: 10, sku_no: 'SKU-S401-2J',     spec_values: '2斤装',        price: 88.00,  stock: 100, image_url: null, status: 1 },
  { id: 33, product_id: 10, sku_no: 'SKU-S401-4J',     spec_values: '4斤装',        price: 158.00, stock: 100, image_url: null, status: 1 },
  // S402 坚果 (product_id=11): 规格
  { id: 34, product_id: 11, sku_no: 'SKU-S402-750',    spec_values: '750g罐装',     price: 139.00, stock: 300, image_url: null, status: 1 },
  { id: 35, product_id: 11, sku_no: 'SKU-S402-1500',   spec_values: '1500g礼盒',    price: 239.00, stock: 200, image_url: null, status: 1 },
  // S403 龙井 (product_id=12): 规格
  { id: 36, product_id: 12, sku_no: 'SKU-S403-100',    spec_values: '100g罐装',     price: 199.00, stock: 50,  image_url: null, status: 1 },
  { id: 37, product_id: 12, sku_no: 'SKU-S403-250',    spec_values: '250g礼盒',     price: 499.00, stock: 50,  image_url: null, status: 1 },
  // M501 面膜 (product_id=13): 规格
  { id: 38, product_id: 13, sku_no: 'SKU-M501-10',     spec_values: '10片装',       price: 99.00,  stock: 500, image_url: null, status: 1 },
  { id: 39, product_id: 13, sku_no: 'SKU-M501-30',     spec_values: '30片装',       price: 249.00, stock: 500, image_url: null, status: 1 },
  // M502 洗面奶 (product_id=14): 规格
  { id: 40, product_id: 14, sku_no: 'SKU-M502-100',    spec_values: '100ml',        price: 69.00,  stock: 500, image_url: null, status: 1 },
  { id: 41, product_id: 14, sku_no: 'SKU-M502-200',    spec_values: '200ml',        price: 109.00, stock: 300, image_url: null, status: 1 },
  // Y601 手环 (product_id=15): 颜色
  { id: 42, product_id: 15, sku_no: 'SKU-Y601-BK',     spec_values: '典雅黑',       price: 199.00, stock: 300, image_url: null, status: 1 },
  { id: 43, product_id: 15, sku_no: 'SKU-Y601-BU',     spec_values: '深海蓝',       price: 199.00, stock: 300, image_url: null, status: 1 },
  // Y602 跑鞋 (product_id=16): 颜色 × 尺码
  { id: 44, product_id: 16, sku_no: 'SKU-Y602-BK-42',  spec_values: '经典黑,42',    price: 459.00, stock: 50,  image_url: null, status: 1 },
  { id: 45, product_id: 16, sku_no: 'SKU-Y602-BK-43',  spec_values: '经典黑,43',    price: 459.00, stock: 80,  image_url: null, status: 1 },
  { id: 46, product_id: 16, sku_no: 'SKU-Y602-WH-42',  spec_values: '纯白,42',      price: 459.00, stock: 60,  image_url: null, status: 1 },
  { id: 47, product_id: 16, sku_no: 'SKU-Y602-WH-43',  spec_values: '纯白,43',      price: 459.00, stock: 0,   image_url: null, status: 1 },
  // B701 连体衣 (product_id=17): 颜色 × 尺码
  { id: 48, product_id: 17, sku_no: 'SKU-B701-PK-60',  spec_values: '粉色,60cm',    price: 79.00,  stock: 100, image_url: null, status: 1 },
  { id: 49, product_id: 17, sku_no: 'SKU-B701-PK-70',  spec_values: '粉色,70cm',    price: 79.00,  stock: 120, image_url: null, status: 1 },
  { id: 50, product_id: 17, sku_no: 'SKU-B701-BL-60',  spec_values: '蓝色,60cm',    price: 79.00,  stock: 100, image_url: null, status: 1 },
  { id: 51, product_id: 17, sku_no: 'SKU-B701-BL-70',  spec_values: '蓝色,70cm',    price: 79.00,  stock: 130, image_url: null, status: 1 },
  // B702 猫粮 (product_id=18): 规格
  { id: 52, product_id: 18, sku_no: 'SKU-B702-5',      spec_values: '5kg',          price: 109.00, stock: 200, image_url: null, status: 1 },
  { id: 53, product_id: 18, sku_no: 'SKU-B702-10',     spec_values: '10kg',         price: 189.00, stock: 200, image_url: null, status: 1 },
  // W801 画笔 (product_id=19): 规格
  { id: 54, product_id: 19, sku_no: 'SKU-W801-24',     spec_values: '24色基础款',   price: 59.00,  stock: 200, image_url: null, status: 1 },
  { id: 55, product_id: 19, sku_no: 'SKU-W801-48',     spec_values: '48色专业款',   price: 99.00,  stock: 100, image_url: null, status: 1 },
  // W802 办公椅 (product_id=20): 颜色
  { id: 56, product_id: 20, sku_no: 'SKU-W802-BK',     spec_values: '经典黑',       price: 899.00, stock: 50,  image_url: null, status: 1 },
  { id: 57, product_id: 20, sku_no: 'SKU-W802-GY',     spec_values: '高级灰',       price: 899.00, stock: 50,  image_url: null, status: 1 },
  // X901 视频VIP (product_id=21)
  { id: 58, product_id: 21, sku_no: 'SKU-X901-Y',      spec_values: '年度会员',     price: 198.00, stock: 9999, image_url: null, status: 1 },
  { id: 59, product_id: 21, sku_no: 'SKU-X901-Q',      spec_values: '季度会员',     price: 68.00,  stock: 9999, image_url: null, status: 1 },
  // X902 话费充值 (product_id=22)
  { id: 60, product_id: 22, sku_no: 'SKU-X902-50',     spec_values: '50元',         price: 49.00,  stock: 9999, image_url: null, status: 1 },
  { id: 61, product_id: 22, sku_no: 'SKU-X902-100',    spec_values: '100元',        price: 98.50,  stock: 9999, image_url: null, status: 1 },
  // X903 编程课程 (product_id=23)
  { id: 62, product_id: 23, sku_no: 'SKU-X903-B',      spec_values: '基础版',       price: 299.00, stock: 9999, image_url: null, status: 1 },
  { id: 63, product_id: 23, sku_no: 'SKU-X903-P',      spec_values: '专业版',       price: 499.00, stock: 9999, image_url: null, status: 1 },
  // Q101 停车牌 (product_id=24)
  { id: 64, product_id: 24, sku_no: 'SKU-Q101-DF',     spec_values: '默认',         price: 29.90,  stock: 500,  image_url: null, status: 1 },
  // Q102 DIY礼物 (product_id=25)
  { id: 65, product_id: 25, sku_no: 'SKU-Q102-S',      spec_values: '标准版',       price: 39.00,  stock: 200,  image_url: null, status: 1 },
  { id: 66, product_id: 25, sku_no: 'SKU-Q102-H',      spec_values: '豪华版',       price: 69.00,  stock: 100,  image_url: null, status: 1 }
]

// ==================== 6. spec 规格名表 ====================
const spec = [
  // D101 手机
  { id: 1,  product_id: 1,  name: '颜色',  sort_order: 1 },
  { id: 2,  product_id: 1,  name: '存储',  sort_order: 2 },
  // D102 笔记本
  { id: 3,  product_id: 2,  name: '颜色',  sort_order: 1 },
  { id: 4,  product_id: 2,  name: '内存',  sort_order: 2 },
  // D103 耳机
  { id: 5,  product_id: 3,  name: '颜色',  sort_order: 1 },
  // F201 T恤
  { id: 6,  product_id: 4,  name: '颜色',  sort_order: 1 },
  { id: 7,  product_id: 4,  name: '尺码',  sort_order: 2 },
  // F202 牛仔裤
  { id: 8,  product_id: 5,  name: '颜色',  sort_order: 1 },
  { id: 9,  product_id: 5,  name: '尺码',  sort_order: 2 },
  // F203 包
  { id: 10, product_id: 6,  name: '颜色',  sort_order: 1 },
  // J301 四件套
  { id: 11, product_id: 7,  name: '颜色',  sort_order: 1 },
  { id: 12, product_id: 7,  name: '尺寸',  sort_order: 2 },
  // J302 保温杯
  { id: 13, product_id: 8,  name: '颜色',  sort_order: 1 },
  // S401 车厘子
  { id: 14, product_id: 10, name: '规格',  sort_order: 1 },
  // S402 坚果
  { id: 15, product_id: 11, name: '规格',  sort_order: 1 },
  // S403 龙井
  { id: 16, product_id: 12, name: '规格',  sort_order: 1 },
  // M501 面膜
  { id: 17, product_id: 13, name: '规格',  sort_order: 1 },
  // M502 洗面奶
  { id: 18, product_id: 14, name: '规格',  sort_order: 1 },
  // Y601 手环
  { id: 19, product_id: 15, name: '颜色',  sort_order: 1 },
  // Y602 跑鞋
  { id: 20, product_id: 16, name: '颜色',  sort_order: 1 },
  { id: 21, product_id: 16, name: '尺码',  sort_order: 2 },
  // B701 连体衣
  { id: 22, product_id: 17, name: '颜色',  sort_order: 1 },
  { id: 23, product_id: 17, name: '尺码',  sort_order: 2 },
  // B702 猫粮
  { id: 24, product_id: 18, name: '规格',  sort_order: 1 },
  // W801 画笔
  { id: 25, product_id: 19, name: '规格',  sort_order: 1 },
  // W802 办公椅
  { id: 26, product_id: 20, name: '颜色',  sort_order: 1 },
  // X901 视频VIP
  { id: 27, product_id: 21, name: '时长',  sort_order: 1 },
  // X902 话费充值
  { id: 28, product_id: 22, name: '面额',  sort_order: 1 },
  // X903 编程课程
  { id: 29, product_id: 23, name: '版本',  sort_order: 1 },
  // Q102 DIY礼物
  { id: 30, product_id: 25, name: '版本',  sort_order: 1 }
]

// ==================== 7. spec_value 规格值表 ====================
const spec_value = [
  // spec_id=1  手机颜色
  { id: 1,  spec_id: 1,  value: '曜石黑', sort_order: 1 },
  { id: 2,  spec_id: 1,  value: '星光银', sort_order: 2 },
  // spec_id=2  手机存储
  { id: 3,  spec_id: 2,  value: '128G',   sort_order: 1 },
  { id: 4,  spec_id: 2,  value: '256G',   sort_order: 2 },
  // spec_id=3  笔记本颜色
  { id: 5,  spec_id: 3,  value: '深空灰', sort_order: 1 },
  { id: 6,  spec_id: 3,  value: '银色',   sort_order: 2 },
  // spec_id=4  笔记本内存
  { id: 7,  spec_id: 4,  value: '16G',    sort_order: 1 },
  { id: 8,  spec_id: 4,  value: '32G',    sort_order: 2 },
  // spec_id=5  耳机颜色
  { id: 9,  spec_id: 5,  value: '典雅黑', sort_order: 1 },
  { id: 10, spec_id: 5,  value: '云白色', sort_order: 2 },
  { id: 11, spec_id: 5,  value: '雾霾蓝', sort_order: 3 },
  // spec_id=6  T恤颜色
  { id: 12, spec_id: 6,  value: '经典黑', sort_order: 1 },
  { id: 13, spec_id: 6,  value: '纯白',   sort_order: 2 },
  { id: 14, spec_id: 6,  value: '灰色',   sort_order: 3 },
  // spec_id=7  T恤尺码
  { id: 15, spec_id: 7,  value: 'S',      sort_order: 1 },
  { id: 16, spec_id: 7,  value: 'M',      sort_order: 2 },
  { id: 17, spec_id: 7,  value: 'L',      sort_order: 3 },
  // spec_id=8  牛仔裤颜色
  { id: 18, spec_id: 8,  value: '经典蓝', sort_order: 1 },
  { id: 19, spec_id: 8,  value: '深黑',   sort_order: 2 },
  // spec_id=9  牛仔裤尺码
  { id: 20, spec_id: 9,  value: '28',     sort_order: 1 },
  { id: 21, spec_id: 9,  value: '29',     sort_order: 2 },
  { id: 22, spec_id: 9,  value: '30',     sort_order: 3 },
  // spec_id=10 包颜色
  { id: 23, spec_id: 10, value: '复古棕', sort_order: 1 },
  { id: 24, spec_id: 10, value: '经典黑', sort_order: 2 },
  // spec_id=11 四件套颜色
  { id: 25, spec_id: 11, value: '高级灰', sort_order: 1 },
  { id: 26, spec_id: 11, value: '米杏色', sort_order: 2 },
  // spec_id=12 四件套尺寸
  { id: 27, spec_id: 12, value: '1.5m',   sort_order: 1 },
  { id: 28, spec_id: 12, value: '1.8m',   sort_order: 2 },
  // spec_id=13 保温杯颜色
  { id: 29, spec_id: 13, value: '经典黑', sort_order: 1 },
  { id: 30, spec_id: 13, value: '银色',   sort_order: 2 },
  // spec_id=14 车厘子规格
  { id: 31, spec_id: 14, value: '2斤装',  sort_order: 1 },
  { id: 32, spec_id: 14, value: '4斤装',  sort_order: 2 },
  // spec_id=15 坚果规格
  { id: 33, spec_id: 15, value: '750g罐装',  sort_order: 1 },
  { id: 34, spec_id: 15, value: '1500g礼盒', sort_order: 2 },
  // spec_id=16 龙井规格
  { id: 35, spec_id: 16, value: '100g罐装',  sort_order: 1 },
  { id: 36, spec_id: 16, value: '250g礼盒',  sort_order: 2 },
  // spec_id=17 面膜规格
  { id: 37, spec_id: 17, value: '10片装',    sort_order: 1 },
  { id: 38, spec_id: 17, value: '30片装',    sort_order: 2 },
  // spec_id=18 洗面奶规格
  { id: 39, spec_id: 18, value: '100ml',     sort_order: 1 },
  { id: 40, spec_id: 18, value: '200ml',     sort_order: 2 },
  // spec_id=19 手环颜色
  { id: 41, spec_id: 19, value: '典雅黑',    sort_order: 1 },
  { id: 42, spec_id: 19, value: '深海蓝',    sort_order: 2 },
  // spec_id=20 跑鞋颜色
  { id: 43, spec_id: 20, value: '经典黑',    sort_order: 1 },
  { id: 44, spec_id: 20, value: '纯白',      sort_order: 2 },
  // spec_id=21 跑鞋尺码
  { id: 45, spec_id: 21, value: '42',        sort_order: 1 },
  { id: 46, spec_id: 21, value: '43',        sort_order: 2 },
  // spec_id=22 连体衣颜色
  { id: 47, spec_id: 22, value: '粉色',      sort_order: 1 },
  { id: 48, spec_id: 22, value: '蓝色',      sort_order: 2 },
  // spec_id=23 连体衣尺码
  { id: 49, spec_id: 23, value: '60cm',      sort_order: 1 },
  { id: 50, spec_id: 23, value: '70cm',      sort_order: 2 },
  // spec_id=24 猫粮规格
  { id: 51, spec_id: 24, value: '5kg',       sort_order: 1 },
  { id: 52, spec_id: 24, value: '10kg',      sort_order: 2 },
  // spec_id=25 画笔规格
  { id: 53, spec_id: 25, value: '24色基础款', sort_order: 1 },
  { id: 54, spec_id: 25, value: '48色专业款', sort_order: 2 },
  // spec_id=26 办公椅颜色
  { id: 55, spec_id: 26, value: '经典黑',    sort_order: 1 },
  { id: 56, spec_id: 26, value: '高级灰',    sort_order: 2 },
  // spec_id=27 视频VIP时长
  { id: 57, spec_id: 27, value: '年度会员',  sort_order: 1 },
  { id: 58, spec_id: 27, value: '季度会员',  sort_order: 2 },
  // spec_id=28 话费面额
  { id: 59, spec_id: 28, value: '50元',      sort_order: 1 },
  { id: 60, spec_id: 28, value: '100元',     sort_order: 2 },
  // spec_id=29 编程课程版本
  { id: 61, spec_id: 29, value: '基础版',    sort_order: 1 },
  { id: 62, spec_id: 29, value: '专业版',    sort_order: 2 },
  // spec_id=30 DIY礼物版本
  { id: 63, spec_id: 30, value: '标准版',    sort_order: 1 },
  { id: 64, spec_id: 30, value: '豪华版',    sort_order: 2 }
]

// ==================== 8. banner 轮播图表 ====================
const banner = [
  { id: 1, title: '旗舰手机新品', image_url: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=750&h=300&fit=crop&auto=format', link_type: 1, link_value: 'D101', sort_order: 100, status: 1 },
  { id: 2, title: '夏季纯棉短袖', image_url: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=750&h=300&fit=crop&auto=format', link_type: 1, link_value: 'F201', sort_order: 90,  status: 1 },
  { id: 3, title: '明前龙井礼盒', image_url: 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?w=750&h=300&fit=crop&auto=format', link_type: 1, link_value: 'S403', sort_order: 80,  status: 1 }
]

// ==================== 9. cart_item 购物车表 ====================
const cart_item = [
  { id: 1, user_id: 1, product_id: 1, sku_id: 2,  quantity: 1, checked: 1, created_at: '2024-06-01 10:00:00', updated_at: '2024-06-01 10:00:00' },
  { id: 2, user_id: 1, product_id: 4, sku_id: 15, quantity: 2, checked: 1, created_at: '2024-06-01 11:00:00', updated_at: '2024-06-01 11:00:00' },
  { id: 3, user_id: 1, product_id: 10, sku_id: 32, quantity: 1, checked: 0, created_at: '2024-06-02 09:00:00', updated_at: '2024-06-02 09:00:00' }
]

// ==================== 10. order 订单表 ====================
const order = [
  {
    id: 1, order_no: 'DD20240601001', user_id: 1, status: 'UNPAID',
    total_count: 1, total_amount: 5299.00, freight_amount: 0, discount_amount: 0,
    address_snapshot: null, remark: null, is_deleted: 0,
    pay_time: null, ship_time: null, receive_time: null, finish_time: null, cancel_time: null,
    created_at: '2024-06-01 14:00:00', updated_at: '2024-06-01 14:00:00'
  },
  {
    id: 2, order_no: 'DD20240602002', user_id: 1, status: 'UNSHIPPED',
    total_count: 2, total_amount: 287.00, freight_amount: 0, discount_amount: 0,
    address_snapshot: { name: '张三', phone: '138****8888', address: '浙江省杭州市西湖区文三路138号' }, remark: null, is_deleted: 0,
    pay_time: '2024-06-02 10:30:00', ship_time: null, receive_time: null, finish_time: null, cancel_time: null,
    created_at: '2024-06-02 10:00:00', updated_at: '2024-06-02 10:30:00'
  },
  {
    id: 3, order_no: 'DD20240530003', user_id: 1, status: 'FINISHED',
    total_count: 1, total_amount: 88.00, freight_amount: 0, discount_amount: 0,
    address_snapshot: { name: '张三', phone: '138****8888', address: '浙江省杭州市西湖区文三路138号' }, remark: null, is_deleted: 0,
    pay_time: '2024-05-30 09:00:00', ship_time: '2024-05-30 15:00:00', receive_time: '2024-06-01 10:00:00', finish_time: '2024-06-01 10:00:00', cancel_time: null,
    created_at: '2024-05-30 08:30:00', updated_at: '2024-06-01 10:00:00'
  }
]

// ==================== 11. order_item 订单商品表 ====================
const order_item = [
  // 订单1: 手机
  { id: 1, order_id: 1, product_id: 1, sku_id: 2, product_no: 'D101', title: '全新旗舰 5G 智能手机 120Hz 高刷屏 256G', image_url: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=140&h=140&fit=crop&auto=format', spec_text: '曜石黑,256G', price: 5299.00, quantity: 1, subtotal: 5299.00, created_at: '2024-06-01 14:00:00' },
  // 订单2: T恤 + 牛仔裤
  { id: 2, order_id: 2, product_id: 4, sku_id: 15, product_no: 'F201', title: '夏季纯棉短袖T恤 宽松潮流百搭', image_url: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=140&h=140&fit=crop&auto=format', spec_text: '纯白,M', price: 59.00, quantity: 2, subtotal: 118.00, created_at: '2024-06-02 10:00:00' },
  { id: 3, order_id: 2, product_id: 5, sku_id: 20, product_no: 'F202', title: '韩版修身牛仔裤 九分直筒显瘦', image_url: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=140&h=140&fit=crop&auto=format', spec_text: '经典蓝,30', price: 129.00, quantity: 1, subtotal: 129.00, created_at: '2024-06-02 10:00:00' },
  // 订单3: 车厘子
  { id: 4, order_id: 3, product_id: 10, sku_id: 32, product_no: 'S401', title: '进口车厘子 鲜甜多汁 顺丰直达', image_url: 'https://images.unsplash.com/photo-1528821128474-27f963b062bf?w=140&h=140&fit=crop&auto=format', spec_text: '2斤装', price: 88.00, quantity: 1, subtotal: 88.00, created_at: '2024-05-30 08:30:00' }
]

// ==================== 12. address 收货地址表 ====================
const address = [
  { id: 1, user_id: 1, name: '张三', phone: '13812348888', province: '浙江省', city: '杭州市', district: '西湖区', detail: '文三路138号浙江大学科技园', is_default: 1, created_at: '2024-01-15 10:00:00', updated_at: '2024-01-15 10:00:00' },
  { id: 2, user_id: 1, name: '张三', phone: '13812348888', province: '北京市', city: '北京市', district: '海淀区', detail: '中关村大街1号', is_default: 0, created_at: '2024-03-01 12:00:00', updated_at: '2024-03-01 12:00:00' }
]

// ==================== Mock 商品数据补齐 ====================
// 保留上方手写样例数据，再按分类补足到每类至少 20 件，并同步生成图片、规格、规格值和 SKU。
const MOCK_PRODUCT_TARGET_PER_CATEGORY = 20

const categoryMockConfig = {
  1: {
    prefix: 'D',
    titles: ['折叠屏 5G 手机 轻薄旗舰版', '智能电视 4K 超清护眼大屏', '扫地机器人 激光导航自动集尘', '无线机械键盘 三模热插拔', '高速固态硬盘 2TB 移动存储', '智能空气炸锅 大容量可视窗口', '家用投影仪 1080P 自动对焦', '便携蓝牙音箱 重低音防水', '电竞显示器 27英寸 165Hz', '智能门锁 指纹密码双开', '桌面空气净化器 除醛除味', '多功能充电宝 20000mAh', '智能台灯 国AA护眼调光', '家用破壁机 低噪冷热双打', '洗地机 干湿两用自清洁', '智能摄像头 2K 夜视版', '无线充电器 三合一快充', '迷你电饭煲 宿舍家用', '智能手表 NFC通话版', '游戏手柄 蓝牙低延迟'],
    specs: [{ name: '颜色', values: ['曜石黑', '星光银'] }, { name: '版本', values: ['标准版', '高配版'] }],
    basePrice: 299,
    priceStep: 180
  },
  2: {
    prefix: 'F',
    titles: ['法式碎花连衣裙 收腰显瘦', '休闲运动外套 防风透气', '真皮小白鞋 软底百搭', '通勤托特包 大容量简约', '高腰阔腿裤 垂感西装面料', '纯棉衬衫 宽松长袖', '轻奢链条包 单肩斜挎', '针织开衫 柔软亲肤', '防晒冰丝袖套 夏季透气', '运动速干T恤 男女同款', '羊毛围巾 秋冬保暖', '棒球帽 遮阳百搭', '复古帆布鞋 低帮耐磨', '抽绳卫裤 宽松束脚', '牛皮腰带 自动扣商务', '轻薄羽绒马甲 便携保暖', '蕾丝内搭背心 亲肤透气', '旅行双肩包 防泼水', '简约手拿包 日常通勤', '厚底凉拖 居家外穿'],
    specs: [{ name: '颜色', values: ['经典黑', '米白色'] }, { name: '尺码', values: ['M', 'L'] }],
    basePrice: 49,
    priceStep: 35
  },
  3: {
    prefix: 'J',
    titles: ['香薰无火藤条 家用卧室持久留香', '北欧陶瓷花瓶 客厅软装摆件', '厨房置物架 免打孔多层收纳', '记忆棉枕头 慢回弹护颈', '加厚浴巾 纯棉吸水速干', '玻璃密封罐 五谷杂粮收纳', '可折叠晾衣架 阳台落地', '家用收纳箱 大号带轮', '硅胶锅铲套装 不粘锅专用', '卧室遮光窗帘 简约纯色', '日式垃圾桶 带盖按压式', '懒人沙发 单人小户型', '陶瓷马克杯 早餐咖啡杯', '多功能插线板 USB快充', '防滑地垫 入户吸水耐脏', '衣柜分层隔板 可伸缩', '餐桌桌布 防水防油', '厨房沥水篮 双层收纳', '床头小夜灯 触控调光', '竹纤维纸巾 家用实惠装'],
    specs: [{ name: '颜色', values: ['暖白色', '高级灰'] }, { name: '规格', values: ['基础款', '升级款'] }],
    basePrice: 29,
    priceStep: 28
  },
  4: {
    prefix: 'S',
    titles: ['云南小粒咖啡豆 中深烘焙', '无糖黑芝麻丸 独立小包装', '低脂鸡胸肉 即食健身餐', '新疆灰枣 大颗粒免洗', '蜂蜜柚子茶 果肉饮品', '手工蛋黄酥 礼盒装', '东北五常大米 香软新米', '冷萃茶包 水果花茶组合', '即食燕麦片 早餐冲饮', '黑巧克力 低糖可可', '海苔肉松卷 儿童零食', '高山苹果 脆甜多汁', '原切牛排 家庭套餐', '酸奶坚果麦片 混合装', '麻辣牛肉干 休闲零食', '椰子水 低卡电解质', '冻干草莓 酸甜酥脆', '有机玉米糁 粗粮早餐', '手冲挂耳咖啡 便携装', '桂花乌龙茶 清香礼盒'],
    specs: [{ name: '规格', values: ['尝鲜装', '家庭装'] }],
    basePrice: 19,
    priceStep: 22
  },
  5: {
    prefix: 'M',
    titles: ['玻尿酸精华液 保湿修护', '清爽防晒霜 SPF50+', '柔润身体乳 秋冬滋养', '氨基酸沐浴露 温和留香', '修护护手霜 便携装', '控油散粉 细腻定妆', '眉笔 防水不易脱色', '润唇膏 淡纹保湿', '卸妆水 敏感肌可用', '爽肤水 补水舒缓', '发膜 柔顺修护干枯', '香氛洗发水 持久留香', '眼霜 淡化细纹', '洁面巾 加厚珍珠纹', '粉底液 轻薄持妆', '腮红盘 自然提气色', '男士洁面乳 控油清爽', '牙膏 美白清新口气', '面部按摩仪 温热导入', '护发精油 防毛躁'],
    specs: [{ name: '规格', values: ['单件装', '组合装'] }],
    basePrice: 39,
    priceStep: 18
  },
  6: {
    prefix: 'Y',
    titles: ['瑜伽垫 加厚防滑初学者', '运动水壶 大容量便携', '筋膜枪 深层放松静音', '跳绳 智能计数无绳两用', '登山杖 轻量铝合金', '骑行头盔 一体成型', '健身弹力带 多阻力组合', '露营折叠椅 便携承重', '户外野餐垫 防潮加厚', '篮球 室内外耐磨', '羽毛球拍 双拍套装', '游泳镜 防雾高清', '运动护膝 透气支撑', '跑步腰包 防水贴身', '哑铃 可调节家用', '速干运动短裤 轻薄透气', '冲锋衣 防风防泼水', '户外营地灯 充电长续航', '蛋白摇摇杯 防漏刻度', '飞盘 户外亲子训练'],
    specs: [{ name: '颜色', values: ['经典黑', '活力蓝'] }, { name: '规格', values: ['标准款', '专业款'] }],
    basePrice: 39,
    priceStep: 42
  },
  7: {
    prefix: 'B',
    titles: ['婴儿湿巾 加厚柔润80抽', '儿童水杯 防摔吸管杯', '宠物猫砂 除臭低尘', '狗狗牵引绳 反光耐拉', '儿童积木 大颗粒益智', '宝宝辅食碗 防摔吸盘', '宠物自动喂食器 定时定量', '儿童雨衣 书包位防水', '婴儿睡袋 春秋薄款', '宠物梳毛器 去浮毛', '宝宝学步鞋 软底防滑', '儿童安全座椅 增高垫', '宠物窝 四季通用可拆洗', '奶瓶清洁剂 温和无香', '婴儿浴巾 纯棉包被', '宠物磨牙棒 鸡肉味', '儿童书包 护脊轻量', '宝宝围兜 防水易清洗', '猫抓板 耐磨不掉屑', '儿童餐具套装 卡通防摔'],
    specs: [{ name: '颜色', values: ['浅粉色', '天空蓝'] }, { name: '规格', values: ['小号', '大号'] }],
    basePrice: 19,
    priceStep: 30
  },
  8: {
    prefix: 'W',
    titles: ['中性笔 黑色速干12支', 'A5笔记本 横线加厚纸', '桌面文件架 多层收纳', '便签纸 莫兰迪色组合', '绘画马克笔 软头双头', '学生书立 金属稳固', '订书机 省力办公款', '计算器 大按键商务', '鼠标垫 超大桌面防滑', '文件袋 A4透明拉链', '白板笔 可擦低味', '美工刀 安全锁扣', '胶带切割器 桌面款', '阅读架 可调节折叠', '台历计划本 周月管理', '彩色长尾夹 办公套装', '儿童剪纸手工材料包', '会议记录本 皮面磁扣', '电脑支架 铝合金散热', '打印纸 A4加厚整箱'],
    specs: [{ name: '规格', values: ['基础款', '加量装'] }],
    basePrice: 9,
    priceStep: 16
  },
  9: {
    prefix: 'X',
    titles: ['云盘会员 月度高速空间', '音乐会员 季卡权益包', '在线设计课程 入门到进阶', '电子书畅读 年度会员', '办公软件激活码 家庭版', '游戏点卡 直充到账', '视频剪辑课 实战项目制', '语言学习会员 月卡', '简历模板 高级套装', '网课题库 30天通用', '图片素材包 商用授权', '数据分析课程 零基础', '少儿编程体验课 10课时', '手机流量包 全国通用', '云服务器体验券 轻量版', 'AI绘画课程 基础班', '线上健身课 月度计划', '知识付费专栏 年度订阅', '设计软件插件套装', '电子贺卡 定制模板'],
    specs: [{ name: '版本', values: ['基础版', '专业版'] }],
    basePrice: 29,
    priceStep: 38
  },
  10: {
    prefix: 'Q',
    titles: ['便携雨伞 晴雨两用加固骨架', '创意钥匙扣 金属挂件', '汽车香薰 出风口持久淡香', '旅行收纳袋 分区防水', '手机支架 桌面折叠款', '防水手机袋 漂流游泳', '一次性压缩毛巾 旅行装', '车载纸巾盒 简约皮革', '多功能开瓶器 家用便携', '桌面小风扇 USB静音', '暖手宝 迷你充电款', '行李牌 防丢信息卡', '眼罩 遮光睡眠柔软', '耳塞 降噪睡眠旅行', '手机挂绳 可调节斜挎', '厨房计时器 磁吸大屏', '补光灯 直播拍照便携', '门后挂钩 免打孔', '零钱包 小巧拉链款', '应急手电筒 USB充电'],
    specs: [{ name: '颜色', values: ['黑色', '白色'] }, { name: '规格', values: ['单件装', '双件装'] }],
    basePrice: 12,
    priceStep: 14
  }
}

function getMaxId(list) {
  return list.reduce((max, item) => Math.max(max, item.id || 0), 0)
}

function formatMockDate(offset) {
  const day = String(16 - (offset % 16)).padStart(2, '0')
  const hour = String(9 + (offset % 10)).padStart(2, '0')
  const minute = String((offset * 7) % 60).padStart(2, '0')
  return `2024-06-${day} ${hour}:${minute}:00`
}

const categoryUnsplashPhotoPools = {
  1: ['photo-1516321318423-f06f85e504b3', 'photo-1517336714731-489689fd1ca8', 'photo-1531297484001-80022131f5a1', 'photo-1541807084-5c52b6b3adef', 'photo-1550009158-9ebf69173e03', 'photo-1527443224154-c4a3942d3acf', 'photo-1588508065123-287b28e013da', 'photo-1550745165-9bc0b252726f'],
  2: ['photo-1483985988355-763728e1935b', 'photo-1496747611176-843222e1e57c', 'photo-1529139574466-a303027c1d8b', 'photo-1515886657613-9f3515b0c78f', 'photo-1558769132-cb1aea458c5e', 'photo-1525507119028-ed4c629a60a3', 'photo-1520975954732-35dd22299614', 'photo-1523398002811-999ca8dec234'],
  3: ['photo-1484101403633-562f891dc89a', 'photo-1616486338812-3dadae4b4ace', 'photo-1618221195710-dd6b41faaea6', 'photo-1586023492125-27b2c045efd7', 'photo-1513694203232-719a280e022f', 'photo-1524758631624-e2822e304c36', 'photo-1616046229478-9901c5536a45', 'photo-1567016432779-094069958ea5'],
  4: ['photo-1498837167922-ddd27525d352', 'photo-1504674900247-0877df9cc836', 'photo-1512621776951-a57141f2eefd', 'photo-1546069901-ba9599a7e63c', 'photo-1476224203421-9ac39bcb3327', 'photo-1504754524776-8f4f37790ca0', 'photo-1555939594-58d7cb561ad1', 'photo-1482049016688-2d3e1b311543'],
  5: ['photo-1596462502278-27bfdc403348', 'photo-1522335789203-aabd1fc54bc9', 'photo-1598440947619-2c35fc9aa908', 'photo-1512496015851-a90fb38ba796', 'photo-1580870069867-74c57ee1bb07', 'photo-1601049541289-9b1b7bbbfe19', 'photo-1522335789203-aabd1fc54bc9', 'photo-1612817288484-6f916006741a'],
  6: ['photo-1517836357463-d25dfeac3438', 'photo-1571019613454-1cb2f99b2d8b', 'photo-1544367567-0f2fcb009e0b', 'photo-1517649763962-0c623066013b', 'photo-1599058917212-d750089bc07e', 'photo-1518611012118-696072aa579a', 'photo-1530549387789-4c1017266635', 'photo-1526506118085-60ce8714f8c5'],
  7: ['photo-1514888286974-6c03e2ca1dba', 'photo-1587300003388-59208cc962cb', 'photo-1537151608828-ea2b11777ee8', 'photo-1515488042361-ee00e0ddd4e4', 'photo-1548199973-03cce0bbc87b', 'photo-1560807707-8cc77767d783', 'photo-1544717305-2782549b5136', 'photo-1514888286974-6c03e2ca1dba'],
  8: ['photo-1455390582262-044cdead277a', 'photo-1497032628192-86f99bcd76bc', 'photo-1454165804606-c3d57bc86b40', 'photo-1510935813936-763eb6fbc613', 'photo-1517971129774-8a2b38fa128e', 'photo-1521587760476-6c12a4b040da', 'photo-1586281380349-632531db7ed4', 'photo-1503676260728-1c00da094a0b'],
  9: ['photo-1516321318423-f06f85e504b3', 'photo-1451187580459-43490279c0fa', 'photo-1485827404703-89b55fcc595e', 'photo-1504384308090-c894fdcc538d', 'photo-1519389950473-47ba0277781c', 'photo-1550751827-4bd374c3f58b', 'photo-1555949963-aa79dcee981c', 'photo-1504384308090-c894fdcc538d'],
  10: ['photo-1526170375885-4d8ecf77b99f', 'photo-1500530855697-b586d89ba3ee', 'photo-1525966222134-fcfa99b8ae77', 'photo-1491637639811-60e2756cc1c7', 'photo-1526947425960-945c6e72858f', 'photo-1536431311719-398b6704d4cc', 'photo-1503602642458-232111445657', 'photo-1526170375885-4d8ecf77b99f']
}

function pickMockPhotoId(categoryId, productId, slot) {
  const pool = categoryUnsplashPhotoPools[categoryId] || categoryUnsplashPhotoPools[10]
  return pool[(productId + slot) % pool.length]
}

function buildMockImage(photoId, width, height, signature) {
  return `https://images.unsplash.com/${photoId}?w=${width}&h=${height}&fit=crop&auto=format&q=80&ixid=${signature}`
}

function createSkuCombinations(groups) {
  return groups.reduce((acc, group) => {
    const values = group.values
    if (acc.length === 0) return values.map(value => [value])
    return acc.flatMap(parts => values.map(value => parts.concat(value)))
  }, [])
}

function ensureCategoryProductMockData() {
  let nextProductId = getMaxId(product) + 1
  let nextImageId = getMaxId(product_image) + 1
  let nextSkuId = getMaxId(product_sku) + 1
  let nextSpecId = getMaxId(spec) + 1
  let nextSpecValueId = getMaxId(spec_value) + 1

  category.forEach(cat => {
    const config = categoryMockConfig[cat.id]
    if (!config) return

    const existingCount = product.filter(item => item.category_id === cat.id).length
    const missingCount = Math.max(MOCK_PRODUCT_TARGET_PER_CATEGORY - existingCount, 0)

    for (let i = 0; i < missingCount; i += 1) {
      const displayIndex = existingCount + i + 1
      const productId = nextProductId++
      const productNoBase = cat.id === 10 ? 100 : cat.id * 100
      const productNo = `${config.prefix}${String(productNoBase + displayIndex).padStart(3, '0')}`
      const title = config.titles[i % config.titles.length]
      const price = Number((config.basePrice + ((displayIndex - 1) % 12) * config.priceStep + (i % 3) * 6).toFixed(2))
      const stockBase = cat.id === 9 ? 9999 : 120 + ((displayIndex * 23) % 420)
      const createdAt = formatMockDate(displayIndex + cat.id)
      const imageSignature = `${productNo}-${productId}`
      const mainPhotoId = pickMockPhotoId(cat.id, productId, 0)

      product.push({
        id: productId,
        product_no: productNo,
        title,
        price,
        original_price: Number((price * 1.28).toFixed(2)),
        category_id: cat.id,
        main_image: buildMockImage(mainPhotoId, 335, 335, `${imageSignature}-main`),
        stock: stockBase,
        sales: 300 + ((productId * 137) % 9800),
        status: 1,
        sort_order: Math.max(1, 100 - displayIndex),
        created_at: createdAt,
        updated_at: createdAt
      })

      product_image.push(
        { id: nextImageId++, product_id: productId, image_url: buildMockImage(mainPhotoId, 750, 750, `${imageSignature}-banner-1`), image_type: 1, sort_order: 1 },
        { id: nextImageId++, product_id: productId, image_url: buildMockImage(pickMockPhotoId(cat.id, productId, 1), 750, 750, `${imageSignature}-banner-2`), image_type: 1, sort_order: 2 },
        { id: nextImageId++, product_id: productId, image_url: buildMockImage(pickMockPhotoId(cat.id, productId, 2), 750, 1200, `${imageSignature}-detail-1`), image_type: 2, sort_order: 1 },
        { id: nextImageId++, product_id: productId, image_url: buildMockImage(pickMockPhotoId(cat.id, productId, 3), 750, 1200, `${imageSignature}-detail-2`), image_type: 2, sort_order: 2 }
      )

      const specIds = config.specs.map((group, groupIndex) => {
        const specId = nextSpecId++
        spec.push({ id: specId, product_id: productId, name: group.name, sort_order: groupIndex + 1 })
        group.values.forEach((value, valueIndex) => {
          spec_value.push({ id: nextSpecValueId++, spec_id: specId, value, sort_order: valueIndex + 1 })
        })
        return specId
      })

      const combinations = createSkuCombinations(config.specs)
      combinations.forEach((values, skuIndex) => {
        const skuPrice = Number((price + skuIndex * Math.max(6, Math.round(price * 0.08))).toFixed(2))
        const skuStock = cat.id === 9 ? 9999 : Math.max(20, Math.floor(stockBase / combinations.length) - skuIndex * 2)
        product_sku.push({
          id: nextSkuId++,
          product_id: productId,
          sku_no: `SKU-${productNo}-${skuIndex + 1}`,
          spec_values: values.join(','),
          price: skuPrice,
          stock: skuStock,
          image_url: null,
          status: 1
        })
      })
    }
  })

  product.forEach(item => {
    if (spec.some(s => s.product_id === item.id)) return

    const values = product_sku
      .filter(sku => sku.product_id === item.id)
      .map(sku => sku.spec_values || '默认')
      .filter((value, index, list) => list.indexOf(value) === index)

    const specId = nextSpecId++
    spec.push({ id: specId, product_id: item.id, name: '规格', sort_order: 1 })
    ;(values.length ? values : ['默认']).forEach((value, index) => {
      spec_value.push({ id: nextSpecValueId++, spec_id: specId, value, sort_order: index + 1 })
    })
  })
}

ensureCategoryProductMockData()

// ==================== 导出 ====================
module.exports = {
  user,
  category,
  product,
  product_image,
  product_sku,
  spec,
  spec_value,
  banner,
  cart_item,
  order,
  order_item,
  address
}
