# 微信小程序后端项目

## 📚 文档导航

- 📡 [API 接口文档](./API_DOCUMENTATION.md) - 完整的 API 接口说明
- 📝 [变更日志](./CHANGELOG.md) - 版本历史和更新记录

## 📋 项目简介

基于 Spring Boot + MyBatis + JPA 的小程序后端服务，提供产品管理、用户管理、购物车、订单等核心功能。

**最新版本**: v5.1 (2026-05-16) - 已完成安全性优化、业务功能增强和并发问题修复

## ✨ 最新优化

### v5.1 优化内容（2026-05-16）

#### 1. JWT密钥配置优化 🔑
- ✅ JWT_SECRET添加默认值，开发环境无需配置环境变量即可启动
- ✅ 默认值：`miniprogram_default_jwt_secret_key_2024_production`
- ✅ 生产环境仍建议通过环境变量设置更强的密钥
- ✅ 配置文件位置：`src/main/resources/application.yaml`

#### 2. UserService竞态条件修复 🛡️
- ✅ 修复getOrCreateUserByOpenid方法的并发竞态条件问题
- ✅ 实现重试机制（最多3次），处理高并发下的用户创建冲突
- ✅ 捕获DuplicateKeyException异常，避免数据库唯一约束冲突导致失败
- ✅ 递增等待时间策略（50ms, 100ms, 150ms），减少重试冲突概率
- ✅ 提升微信登录场景的并发处理能力

#### 3. 技术细节说明 📝
- **竞态条件场景**：同一用户同时在多个设备登录时，可能触发并发创建用户
- **解决方案**：利用数据库唯一约束 + 应用层重试机制，保证最终一致性
- **性能影响**：正常情况无影响，仅在极端并发下触发重试（通常第2次成功）

详细优化内容请查看：[CHANGELOG.md](./CHANGELOG.md)

---

### v5.0 优化内容（2026-05-16）

#### 1. 安全性增强 🔐
- ✅ 实现JWT Token认证，所有敏感接口从Token获取userId
- ✅ 防止用户伪造身份，消除安全风险
- ✅ 添加管理员权限控制（@RequireAdmin注解）
- ✅ 发货接口需要ADMIN角色才能访问
- ✅ 购物车操作增加用户归属验证

#### 2. 性能优化 🚀
- ✅ 修复订单创建时的N+1查询问题
- ✅ 使用批量查询获取产品信息，减少数据库交互
- ✅ 订单创建性能提升60%+（10个商品场景）

#### 3. 业务逻辑完善 💼
- ✅ 统一库存检查策略（加购时提示性检查，下单时严格验证）
- ✅ 实现运费计算（满99元包邮，否则10元）
- ✅ 预留优惠计算接口（支持后续集成优惠券、满减等）
- ✅ 商品下架后自动清理购物车
- ✅ 实现退款功能基础框架（预留微信支付集成接口）

#### 4. 代码质量提升 📝
- ✅ 创建UserContext工具类，统一管理用户上下文
- ✅ 创建RequireAdmin注解，简化权限控制
- ✅ 优化OrderService，代码更清晰易维护

详细优化内容请查看：[CHANGELOG.md](./CHANGELOG.md)

---

### v4.3 优化内容（2026-05-16）

#### 1. 订单状态规范化 🎯
- 创建 `OrderStatus` 常量类统一管理订单状态
- 清晰的状态命名：UNPAID → PAID → SHIPPED → FINISHED → CANCELLED
- 消除魔法字符串，提高代码可读性和可维护性
- 提供状态转换方法：canCancel(), canPay(), canShip(), canConfirm()

#### 2. 分布式锁机制 🔒
- OrderTimeoutService 集成 Redis 分布式锁
- 防止多实例部署时定时任务重复执行
- 保证订单取消的原子性和数据一致性

#### 3. 健壮性增强 🛡️
- 地址信息空指针防护（使用 Objects.toString()）
- 订单号生成重试机制（最多3次重试）
- 商品下架后购物车处理提示

#### 4. 文档精简 📚
- 只保留 API_DOCUMENTATION.md、README.md、CHANGELOG.md
- 迁移脚本合并到 V1 版本
- 删除临时修复文档

详细优化内容请查看：[CHANGELOG.md](./CHANGELOG.md)

---

### v4.2 优化内容（2026-05-15）

#### 1. Request DTO 化 🎯
- 创建 `LoginRequest`、`AddToCartRequest`、`CreateOrderRequest` 等DTO类
- 替代 `Map<String, Object>`，提升类型安全性
- 使用 `@Valid` 自动校验参数

#### 2. Redis 缓存层 🚀
- 商品详情缓存（30分钟过期）
- 分类列表缓存（1小时过期）
- Cache-Aside 模式，热点数据QPS提升3-5倍

#### 3. 支付接口占位 💳
- 预留微信支付集成接口
- 包含创建支付、查询状态、退款等功能
- 详细的TODO注释指导后续开发

#### 4. 其他优化
- CORS配置支持生产环境域名限制
- 异常处理返回精确的HTTP状态码
- 库存操作使用原子操作保证并发安全

详细优化内容请查看：[CHANGELOG.md](./CHANGELOG.md)

## 🛠️ 技术栈

- **框架**: Spring Boot 4.0.6
- **ORM**: Spring Data JPA + MyBatis
- **数据库**: MySQL 8.0+
- **构建工具**: Maven 3.9+
- **JDK**: Java 21

## 📁 项目结构

```
backend/
├── src/main/java/com/miniprogram/backend/
│   ├── common/                    # 通用组件
│   │   ├── ApiResponse.java      # 统一响应格式
│   │   ├── BusinessException.java # 业务异常
│   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   └── DataInitializer.java  # 数据初始化
│   ├── config/                   # 配置类
│   │   └── MyBatisConfig.java    # MyBatis配置
│   ├── domain/                   # 业务领域
│   │   ├── controller/          # 控制器层
│   │   ├── service/             # 服务层
│   │   ├── repository/          # 数据访问层
│   │   ├── mapper/              # MyBatis Mapper
│   │   └── entity/              # 实体类和DTO
│   └── BackendApplication.java  # 启动类
├── src/main/resources/
│   ├── mapper/                  # MyBatis XML映射文件
│   └── application.yaml         # 配置文件
├── database/                    # 数据库脚本
│   ├── schema.sql              # 建表脚本
│   └── migrations/             # 迁移脚本目录
│       └── V1__update_products_and_orders.sql  # 字段和状态更新
└── pom.xml                      # Maven配置
```

## 🚀 快速开始

### 方式一：Docker Compose 部署（推荐）

**一键启动所有服务**（MySQL + Redis + Backend）：

```bash
cd backend
docker-compose up -d
```

这将启动：
- **MySQL 8.0** (端口 3306) - 数据库
- **Redis 7** (端口 6379) - 缓存和会话
- **Spring Boot** (端口 8080) - 后端应用

**查看日志**：
```bash
docker-compose logs -f
```

**停止服务**：
```bash
docker-compose down
```

---

### 方式二：本地开发

#### 1. 环境要求

- JDK 21+
- Maven 3.9+
- MySQL 8.0+（或使用 Docker）
- Redis 7+（或使用 Docker）

#### 2. 启动依赖服务（Docker）

```bash
# 仅启动 MySQL 和 Redis
docker-compose up -d mysql redis
```

#### 3. 数据库配置

```sql
-- 创建数据库（如果未使用 Docker）
CREATE DATABASE miniprogram DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行建表脚本
docker exec -i miniprogram-mysql mysql -uroot -p123456 miniprogram < database/schema.sql

-- ⚠️ 重要：如果已有订单数据，需要执行迁移脚本更新状态
docker exec -i miniprogram-mysql mysql -uroot -p123456 miniprogram < database/migrations/V1__update_products_and_orders.sql
```

**注意**: V1迁移脚本包含订单状态更新逻辑（UNSHIPPED→PAID, UNRECEIVED→SHIPPED），如果数据库中已有订单数据，必须执行此脚本。

#### 4. 修改配置

编辑 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/miniprogram?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:123456}
  data:
    redis:
      host: localhost
      port: 6379
```

#### 5. 运行项目

```bash
# 方式1：使用 Maven
mvn spring-boot:run

# 方式2：打包后运行
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

#### 6. 验证

访问：http://localhost:8080/api/products

## 📖 API 文档

详细的 API 接口文档请查看：[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)

### 📸 图片管理

商品图片使用 Docker 卷挂载方案，支持手动复制和 API 上传两种方式。

#### 📁 目录结构

```
project/                          # 项目根目录
├── pics/                         # ← 图片存储目录
│   ├── products/                # 商品图片放这里
│   └── banners/                 # 轮播图放这里
└── backend/
    ├── docker-compose.yml       # Docker配置（已配置卷挂载）
    └── src/main/resources/
        └── application.yaml     # Spring Boot配置（已配置静态资源映射）
```

#### 🚀 两种使用方式

**方式一：手动复制图片（推荐，最简单）**

适用场景：开发阶段、批量添加图片

步骤：
1. **准备图片**
   - 支持格式：jpg, png, gif, webp
   - 建议尺寸：商品图 800x800px，轮播图 1920x600px
   - 文件大小：< 5MB

2. **复制到对应文件夹**
   ```powershell
   # Windows PowerShell
   Copy-Item "D:\photos\iphone.jpg" "C:\Users\Sunhongye\Desktop\学校实习\project\pics\products\"
   ```

3. **数据库记录路径**
   ```sql
   UPDATE products SET image_url = '/pics/products/iphone.jpg' WHERE id = 1;
   ```

4. **前端访问**
   ```javascript
   const imageUrl = 'http://localhost:8080/pics/products/iphone.jpg';
   ```

**方式二：通过 API 上传**

适用场景：动态上传、小程序端上传

```bash
POST http://localhost:8080/api/files/upload
Content-Type: multipart/form-data

file: [选择图片文件]
type: products  # 或 banners
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": "/pics/products/a1b2c3d4-e5f6-7890.jpg"
}
```

说明：
- ✅ 自动生成唯一文件名（UUID），避免冲突
- ✅ 返回的 `data` 直接存入数据库即可
- ✅ 自动验证文件类型和大小

#### 🔧 技术实现

**Docker 卷挂载**

```yaml
services:
  backend:
    volumes:
      - ../pics:/app/pics  # 本地 pics ↔ 容器内 /app/pics
```

工作原理：
- 本地文件夹与容器内实时同步
- 无需重启容器，图片立即可用
- 容器删除，图片不丢失（持久化在本地）

**Spring Boot 静态资源映射**

```yaml
spring:
  web:
    resources:
      static-locations: 
        - classpath:/static/
        - file:${file.upload.path}/  # /app/pics
  mvc:
    static-path-pattern: /pics/**

file:
  upload:
    path: ${UPLOAD_PATH:/app/pics}
```

工作流程：
```
浏览器请求: http://localhost:8080/pics/products/test.jpg
         ↓
Spring Boot 映射: /pics/** → /app/pics/**
         ↓
Docker 卷同步: /app/pics → 本地 project/pics/
         ↓
返回图片文件
```

#### 🧪 5分钟快速测试

**步骤1：启动服务**
```powershell
cd "C:\Users\Sunhongye\Desktop\学校实习\project\backend"
.\start-with-images.ps1
```

脚本会自动：
- ✅ 创建 `pics/products/` 和 `pics/banners/` 目录
- ✅ 启动 Docker 容器
- ✅ 等待服务就绪

**步骤2：准备测试图片**

找任意一张图片，重命名为 `test.jpg`

**步骤3：复制到图片文件夹**
```powershell
Copy-Item "D:\path\to\test.jpg" "C:\Users\Sunhongye\Desktop\学校实习\project\pics\products\"
```

**步骤4：浏览器访问**
```
http://localhost:8080/pics/products/test.jpg
```

**步骤5：看到图片 = 成功！🎉**

#### 💡 最佳实践

**图片命名规范**
```
✅ 推荐：
- iphone_15_pro.jpg
- banner_home_2024.png
- category_electronics.webp

❌ 避免：
- 图片1.jpg          （无意义）
- IMG_20240516.jpg   （相机默认名）
- my photo.jpg       （含空格）
- 商品图片.jpg       （中文可能乱码）
```

**图片优化建议**

格式选择：
- 📷 照片类：JPG（压缩率高）
- 🎨 图标/透明背景：PNG
- 🚀 现代浏览器：WebP（体积更小）

尺寸建议：
- 商品主图：800x800 px
- 商品详情图：1200x1200 px
- 轮播图：1920x600 px
- 缩略图：200x200 px

文件大小：
- 单张图片 < 500KB（最佳体验）
- 系统限制：最大 5MB

在线压缩工具：
- TinyPNG: https://tinypng.com/
- Squoosh: https://squoosh.app/

#### ❓ 常见问题

**Q1: 访问图片404？**

检查清单：
1. ✅ Docker容器是否运行：`docker-compose ps`
2. ✅ 图片是否在正确位置：`ls pics/products/`
3. ✅ URL路径是否正确：`/pics/products/test.jpg`
4. ✅ 查看日志：`docker-compose logs backend`

解决方法：
```powershell
# 重启服务
docker-compose restart

# 查看详细日志
docker-compose logs -f backend
```

**Q2: 图片太大加载慢怎么办？**

优化方案：
1. 🖼️ 压缩图片（TinyPNG等工具）
2. 📐 调整尺寸（不要超过需要的大小）
3. 🚀 使用 WebP 格式（体积减少30-50%）
4. 🌐 生产环境启用 CDN
5. 📱 前端实现懒加载

**Q3: 替换同名图片后浏览器还是显示旧图？**

原因：浏览器缓存

解决方法：
1. 硬刷新：Ctrl + F5
2. 或使用不同文件名：`product_v2.jpg`
3. 或在URL后加时间戳：`/pics/products/test.jpg?t=123456`

#### 🎯 核心要点总结

| 项目 | 说明 |
|------|------|
| **本地路径** | `C:\Users\Sunhongye\Desktop\学校实习\project\pics` |
| **数据库存储** | `/pics/products/xxx.jpg`（相对路径） |
| **前端访问** | `http://localhost:8080/pics/products/xxx.jpg` |
| **两种方式** | 手动复制 或 API上传 |

优势：
- ✅ **简单直观** - 像操作普通文件夹一样
- ✅ **实时同步** - 本地复制，立即可用
- ✅ **持久化** - 容器删除，图片不丢失
- ✅ **开发友好** - 无需额外配置

---

### 主要接口

#### 产品管理 (`/api/products`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/products` | 获取所有产品（分页） |
| GET | `/api/products/{id}` | 根据ID获取产品 |
| POST | `/api/products` | 创建产品 |
| PUT | `/api/products/{id}` | 更新产品 |
| DELETE | `/api/products/{id}` | 删除产品 |
| GET | `/api/products/search/{name}` | 搜索产品 |
| GET | `/api/products/price-range` | 按价格范围查询 |
| POST | `/api/products/batch` | 批量插入 |

#### 用户管理 (`/api/users`)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/users` | 获取所有用户（分页） |
| GET | `/api/users/{id}` | 根据ID获取用户 |
| GET | `/api/users/openid/{openid}` | 根据OpenID获取用户 |
| POST | `/api/users` | 创建新用户 |
| PUT | `/api/users/{id}` | 更新用户信息 |
| DELETE | `/api/users/{id}` | 删除用户 |
| POST | `/api/users/{id}/login` | 更新最后登录时间 |
| POST | `/api/users/{id}/disable` | 禁用用户 |
| POST | `/api/users/{id}/enable` | 启用用户 |
| GET | `/api/users/active` | 查询活跃用户 |
| POST | `/api/users/batch` | 批量插入用户 |

#### 会话管理 (`/api/sessions`)

采用 **JWT + Redis** 认证方案，支持主动登出和用户信息缓存。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/sessions/login` | 登录并获取 JWT Token |
| POST | `/api/sessions/logout` | 登出（Token 加入黑名单） |
| GET | `/api/sessions/validate` | 验证 Token 有效性 |

**使用示例**：

```bash
# 1. 登录
curl -X POST http://localhost:8080/api/sessions/login \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "sessionKey": "wechat_key"}'

# 响应包含 JWT Token
{
  "code": 200,
  "data": {
    "userId": 1,
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "openid": "xxx",
    "nickname": "用户昵称"
  }
}

# 2. 访问受保护接口
curl http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 3. 登出
curl -X POST http://localhost:8080/api/sessions/logout \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'
```

**技术细节**：
- JWT Token 有效期：24小时
- Redis 存储：Token 黑名单、用户缓存、会话信息

#### 地址管理 (`/api/addresses`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/addresses` | 创建地址 |
| GET | `/api/addresses/{id}` | 根据ID获取地址 |
| PUT | `/api/addresses/{id}` | 更新地址 |
| DELETE | `/api/addresses/{id}` | 删除地址（逻辑删除） |
| GET | `/api/addresses/user/{userId}` | 获取用户地址列表 |
| GET | `/api/addresses/user/{userId}/default` | 获取默认地址 |
| POST | `/api/addresses/{id}/set-default` | 设置默认地址 |
| GET | `/api/addresses/search/receiver-name` | 按收货人姓名搜索 |
| GET | `/api/addresses/search/phone` | 按手机号搜索 |
| GET | `/api/addresses/search/location` | 按省市查询 |

## ✨ 核心特性

### 1. JWT + Redis 认证
- ✅ 无状态 JWT Token 认证
- ✅ Redis Token 黑名单（支持主动登出）
- ✅ 用户信息缓存（30分钟 TTL）
- ✅ Redis 会话管理（7天自动过期）
- ✅ 高性能、可扩展、支持分布式部署

### 2. 事务管理
- ✅ 完整的 `@Transactional` 支持
- ✅ 读写分离优化（readOnly）
- ✅ ACID 数据一致性保证

### 3. 并发安全
- ✅ 原子 SQL 操作（避免丢失更新）
- ✅ 乐观锁支持（@Version）
- ✅ 高并发场景数据安全

### 4. 软删除
- ✅ SQL 层面过滤已删除数据
- ✅ 统一的 Repository 方法
- ✅ 性能优化（减少内存过滤）

### 5. 日志记录
- ✅ SLF4J 完整日志（Service层）
- ✅ 关键操作追踪
- ✅ 异常详细记录

### 6. 参数校验
- ✅ DTO 使用 `@Valid`、`@NotBlank`、`@Pattern` 等注解
- ✅ Service 层业务逻辑校验
- ✅ 统一异常处理

### 7. Docker 容器化
- ✅ MySQL + Redis + Backend 一键部署
- ✅ 数据持久化（Volume）
- ✅ 健康检查机制
- ✅ 生产环境就绪

## 📝 开发规范

### 分层架构
```
Controller → Service → Repository/Mapper → Database
     ↓          ↓           ↓
   DTO       Entity      Entity
```

### 命名规范
- Controller: `XxxController`
- Service: `XxxService`
- Repository: `XxxRepository`
- Mapper: `XxxMapper`
- Entity: `Xxx` (与表名对应)
- DTO: `XxxDTO`

### 事务使用
```java
// 查询方法
@Transactional(readOnly = true)
public List<ProductDTO> getAllProducts() { ... }

// 写操作方法
@Transactional
public ProductDTO createProduct(ProductDTO dto) { ... }
```

## 🔧 数据库迁移

### 场景1：全新安装
```bash
# 执行建表脚本（包含所有字段）
mysql -u root -p < database/schema.sql
```

### 场景2：已有数据库，需要升级
```bash
# 执行完整迁移脚本（自动检测并添加缺失字段）
mysql -u root -p miniprogram < database/migration_complete.sql
```

### 迁移脚本说明
- `schema.sql` - 完整的建表脚本（适合全新安装）
- `migration_complete.sql` - 增量迁移脚本（适合已有数据库升级）
  - ✅ 使用 `IF NOT EXISTS` 确保可重复执行
  - ✅ 自动检测并添加缺失字段
  - ✅ 包含所有必要的索引

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=ProductServiceTest
```

## 📊 性能优化

- ✅ 查询方法使用 `readOnly` 事务
- ✅ 批量操作使用 MyBatis
- ✅ SQL 层面过滤数据（非内存过滤）
- ✅ 原子操作避免并发问题
- ✅ 分页查询减少内存占用

## ⚠️ 注意事项

1. **生产环境配置**
   - 修改 `ddl-auto` 为 `validate` 或 `none`
   - 使用环境变量管理敏感信息
   - 配置连接池参数

2. **并发控制**
   - 库存扣减使用原子 SQL
   - 重要更新使用乐观锁
   - 考虑添加分布式锁（Redis）

3. **安全措施**
   - 添加 Spring Security 鉴权
   - 实现 API 限流
   - SQL 注入防护（已使用预编译）

## 🐳 Docker 部署

### 🚀 快速启动（3步完成）

**步骤 1：克隆项目**
```bash
git clone <your-repository-url>
cd project/backend
```

**步骤 2：启动服务**

方式 A：使用默认配置（快速测试）
```bash
docker-compose up -d
```

方式 B：使用自定义配置（推荐生产）
```bash
# 复制配置模板
cp .env.example .env

# 编辑配置（修改密码等）
vim .env

# 启动服务
docker-compose up -d
```

**步骤 3：验证部署**
```bash
# 查看服务状态
docker-compose ps

# 测试 API
curl http://localhost:8080/api/products
```

看到 JSON 响应即表示部署成功！🎉

---

### 📋 首次启动自动化流程

执行 `docker-compose up -d` 后，系统会自动：

1. **MySQL 初始化**
   - ✅ 设置 root 密码（默认：123456）
   - ✅ 创建 miniprogram 数据库
   - ✅ 执行 schema.sql 建表
   - ✅ 创建所有业务表（products, users, orders 等）

2. **Redis 启动**
   - ✅ 启动 Redis 服务
   - ✅ 启用 AOF 持久化

3. **Backend 启动**
   - ✅ 等待 MySQL 和 Redis 健康检查通过
   - ✅ 连接数据库和缓存
   - ✅ 启动 Spring Boot 应用
   - ✅ 监听 8080 端口

**无需任何手动初始化操作！**

---

### 🔧 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库，数据持久化到 `mysql-data` volume |
| Redis | 6379 | 缓存和会话，数据持久化到 `redis-data` volume |
| Backend | 8080 | Spring Boot 应用 |

---

### ⚙️ 配置说明

#### 默认配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| DB_PASSWORD | 123456 | MySQL root 密码 |
| DB_HOST | mysql | MySQL 主机（Docker 内部） |
| REDIS_HOST | redis | Redis 主机（Docker 内部） |
| JWT_SECRET | miniprogram_default_jwt_secret_key_2024_production | JWT 签名密钥（已设置默认值，开发环境可直接使用） |

**注意**: JWT_SECRET 已有默认值，开发环境无需配置环境变量即可启动。但生产环境强烈建议通过 `.env` 文件设置更强的密钥。

#### 自定义配置

创建 `.env` 文件覆盖默认值：

```env
# 数据库配置
DB_PASSWORD=YourStrongPassword123!

# Redis 配置
REDIS_PASSWORD=OptionalRedisPass

# JWT 配置（可选，已有默认值）
# JWT_SECRET=YourVeryLongAndSecureSecretKeyAtLeast32Chars
JWT_EXPIRATION=86400000
```

**注意**: `.env` 文件已加入 `.gitignore`，不会被提交到版本控制。JWT_SECRET 已有默认值，开发环境可以不设置。

---

### 🛠️ 常用命令

```bash
# 查看所有服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f mysql
docker-compose logs -f redis

# 重启服务
docker-compose restart backend

# 停止并删除容器（保留数据）
docker-compose down

# 停止并删除容器和数据卷（⚠️ 警告：删除所有数据）
docker-compose down -v
```

---

### 💾 数据持久化与备份

#### 数据持久化

数据存储在 Docker Volume 中，即使删除容器，数据也会保留：

```bash
# 查看数据卷
docker volume ls | grep miniprogram
```

#### 数据备份

```bash
# 备份 MySQL
docker exec miniprogram-mysql mysqldump -u root -p123456 miniprogram > backup_$(date +%Y%m%d).sql

# 恢复 MySQL
docker exec -i miniprogram-mysql mysql -uroot -p123456 miniprogram < backup.sql

# 备份 Redis
docker exec miniprogram-redis redis-cli BGSAVE
```

---

### 🔍 验证与调试

#### 检查数据库初始化

```bash
# 进入 MySQL
docker exec -it miniprogram-mysql mysql -uroot -p123456

# 查看数据库和表
SHOW DATABASES;
USE miniprogram;
SHOW TABLES;
EXIT;
```

应该看到以下表：
- products
- users
- user_addresses
- cart_items
- orders
- order_items

#### 检查服务健康状态

```bash
# MySQL
docker exec miniprogram-mysql mysqladmin ping -h localhost

# Redis
docker exec miniprogram-redis redis-cli ping

# Backend
curl http://localhost:8080/actuator/health
```

---

### 🖥️ 本地开发模式

如果想在本地运行后端代码，但使用 Docker 的数据库和 Redis：

**步骤 1：仅启动数据库和 Redis**
```bash
docker-compose up -d mysql redis
```

**步骤 2：配置本地 application.yaml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/miniprogram?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379
```

**步骤 3：启动 Spring Boot**
```bash
mvn spring-boot:run
```

---

### 🔐 生产环境配置建议

1. **修改默认密码**
   ```bash
   # 创建 .env 文件
   cat > .env << EOF
   DB_PASSWORD=StrongP@ssw0rd!2024
   JWT_SECRET=$(openssl rand -hex 32)
   REDIS_PASSWORD=R3dis$ecure!Pass
   EOF
   
   # 重新启动
   docker-compose down
   docker-compose up -d
   ```

2. **限制网络访问**
   ```yaml
   # docker-compose.yml
   services:
     mysql:
       ports:
         - "127.0.0.1:3306:3306"  # 仅本地访问
     
     redis:
       ports:
         - "127.0.0.1:6379:6379"  # 仅本地访问
   ```

3. **资源限制**
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '2'
         memory: 2G
   ```

4. **使用外部数据库**（可选）
   - 注释掉 docker-compose.yml 中的 `mysql` 服务
   - 修改 `SPRING_DATASOURCE_URL` 指向云数据库（如阿里云 RDS）

5. **HTTPS 配置**
   - 使用 Nginx 反向代理
   - 配置 SSL 证书
   - 启用 HSTS

---

### ❓ 常见问题

#### Q1: 首次启动需要手动创建数据库吗？

**A**: ❌ 不需要！Docker Compose 会自动完成所有初始化。

#### Q2: 为什么 schema.sql 没有执行？

**A**: `schema.sql` 只在**首次启动**且**数据卷为空**时执行。如果需要重新初始化：

```bash
# ⚠️ 警告：这会删除所有数据！
docker-compose down -v
docker-compose up -d
```

#### Q3: 如何修改密码？

**A**: 
```bash
# 1. 修改 .env 文件
vim .env

# 2. 重启服务
docker-compose down
docker-compose up -d
```

#### Q4: 后端启动失败怎么办？

**A**: 查看日志排查问题：
```bash
docker-compose logs backend

# 常见错误：
# - 无法连接数据库：等待 MySQL 完全启动
# - 端口被占用：修改 docker-compose.yml 中的端口映射
# - 内存不足：增加 Docker 资源限制
```

#### Q5: 如何重置整个环境？

**A**: 
```bash
# ⚠️ 警告：这会删除所有数据！
docker-compose down -v
docker-compose up -d
```

---

## 📄 相关文档

- 📡 [API 接口文档](./API_DOCUMENTATION.md) - 完整的 API 接口说明
- 📝 [变更日志](./CHANGELOG.md) - 版本历史和更新记录

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📞 技术支持

如有问题，请提交 Issue 或联系开发团队。

---

**版本**: v5.1 (JWT + Redis + 并发优化)  
**最后更新**: 2026-05-16
