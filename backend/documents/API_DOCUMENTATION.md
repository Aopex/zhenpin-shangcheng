# API 接口文档

## 📋 基本信息

- **基础路径**: `/api`
- **请求格式**: `application/json`
- **响应格式**: `application/json`
- **字符编码**: `UTF-8`
- **项目名称**: 微信小程序电商商城后端系统
- **技术栈**: Spring Boot 3.2.5 + MySQL + Redis + JWT

---

---

## 🔐 认证机制（JWT + Redis）

### 概述

本项目采用 **JWT (JSON Web Token) + Redis** 的混合认证方案：

- **JWT**: 无状态认证，包含用户身份信息
- **Redis**: Token 黑名单、用户信息缓存、会话管理

### 认证流程

```
1. 用户登录 → 获取 JWT Token
2. 访问受保护接口 → 携带 Token (Authorization: Bearer {token})
3. 后端验证 → 检查 JWT 签名 + Redis 黑名单
4. 返回数据
```

### 需要认证的接口

以下接口需要在请求头中携带 JWT Token：

- `/api/orders/**` - 订单相关
- `/api/cart/**` - 购物车相关

以下接口无需认证：

- `/api/products/**` - 产品浏览
- `/api/users/**` - 用户注册/查询
- `/api/sessions/login` - 登录接口

### Token 使用方式

在请求头中添加 `Authorization` 字段：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 登录接口

**请求**:
```bash
POST /api/sessions/login
Content-Type: application/json

{
  "userId": 1,
  "sessionKey": "wechat_session_key"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "openid": "xxx",
    "nickname": "用户昵称",
    "avatarUrl": "头像URL",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "sessionKey": "wechat_session_key"
  }
}
```

### 登出接口

**请求**:
```bash
POST /api/sessions/logout
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "userId": 1
}
```

**说明**: 
- Token 会被加入 Redis 黑名单
- Token 在剩余有效期内无法再次使用
- 实现主动登出功能

### Token 验证

**请求**:
```bash
GET /api/sessions/validate?token={JWT_TOKEN}
```

**响应**:
```json
{
  "code": 200,
  "data": true  // 或 false
}
```

### Redis 存储结构

| 用途 | Key 格式 | TTL | 说明 |
|------|---------|-----|------|
| Token 黑名单 | `token:blacklist:{token}` | Token剩余有效期 | 实现主动登出 |
| 用户信息缓存 | `user:info:{userId}` | 30分钟 | 减少DB查询 |
| 用户会话 | `session:{userId}` | 7天 | 存储 session_key |

### 技术细节

- **Token 有效期**: 24小时（可配置）
- **签名算法**: HMAC-SHA256
- **黑名单检查**: 每次请求都会检查 Redis 黑名单
- **用户缓存**: Cache-Aside 模式，自动降级

---

## 📦 统一响应格式

所有接口均返回以下 JSON 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 响应状态码<br/>• 200: 成功<br/>• 400: 请求参数错误<br/>• 404: 资源不存在<br/>• 500: 服务器内部错误 |
| message | String | 响应消息描述 |
| data | Object/Array/null | 响应数据，可能为对象、数组或 null |

---

## 📝 ProductDTO 数据结构

### 字段说明

| 字段 | 类型 | 必填 | 约束 | 说明 | 示例 |
|------|------|------|------|------|------|
| id | Long | 否 | - | 产品ID（新增时不需要，系统自动生成；更新和查询时返回） | 1 |
| name | String | **是** | 最长100字符 | 产品名称 | "iPhone 15" |
| price | BigDecimal | **是** | ≥ 0 | 产品价格（单位：元） | 7999.00 |
| quantity | Integer | **是** | ≥ 0 | 产品库存数量 | 50 |
| imageUrl | String | 否 | 最长500字符 | 产品图片URL地址 | "http://example.com/image.jpg" |
| description | String | 否 | 长文本 | 产品详细描述信息 | "Apple iPhone 15，搭载 A16 芯片，4800万像素主摄像头" |
| createTime | LocalDateTime | 否 | 自动生成 | 创建时间（ISO 8601格式） | "2026-05-13T10:30:00" |
| updateTime | LocalDateTime | 否 | 自动更新 | 最后更新时间（ISO 8601格式） | "2026-05-13T10:30:00" |

### 注意事项

1. **必填字段**：`name`、`price`、`quantity` 为数据库 NOT NULL 字段，新增时必须提供
2. **可选字段**：`imageUrl` 和 `description` 可以为 null 或不传
3. **自动字段**：`createTime` 和 `updateTime` 由后端自动管理，前端无需传入
4. **部分更新**：更新接口支持只传入需要修改的字段

---

## 🔌 接口列表

### 1. 获取所有产品

**接口描述**: 查询数据库中所有产品列表

**请求方式**: `GET`

**接口地址**: `/api/products`

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "iPhone 15",
      "price": 7999.00,
      "quantity": 50,
      "imageUrl": "http://example.com/iphone15.jpg",
      "description": "Apple iPhone 15，搭载 A16 芯片",
      "createTime": "2026-05-13T10:30:00",
      "updateTime": "2026-05-13T10:30:00"
    },
    {
      "id": 2,
      "name": "MacBook Pro",
      "price": 14999.00,
      "quantity": 30,
      "imageUrl": null,
      "description": null,
      "createTime": "2026-05-13T11:00:00",
      "updateTime": "2026-05-13T11:00:00"
    }
  ]
}
```

---

### 2. 根据ID获取产品

**接口描述**: 根据产品ID查询单个产品详情

**请求方式**: `GET`

**接口地址**: `/api/products/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 产品ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "iPhone 15",
    "price": 7999.00,
    "quantity": 50,
    "imageUrl": "http://example.com/iphone15.jpg",
    "description": "Apple iPhone 15，搭载 A16 芯片",
    "createTime": "2026-05-13T10:30:00",
    "updateTime": "2026-05-13T10:30:00"
  }
}
```

**失败响应示例** (HTTP 404):
```json
{
  "code": 404,
  "message": "Product not found with id: 999",
  "data": null
}
```

---

### 3. 创建新产品

**接口描述**: 新增一个产品到数据库

**请求方式**: `POST`

**接口地址**: `/api/products`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "name": "iPhone 15",
  "price": 7999.00,
  "quantity": 50,
  "imageUrl": "http://example.com/iphone15.jpg",
  "description": "Apple iPhone 15，搭载 A16 芯片"
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | **是** | 产品名称，不能为空 |
| price | BigDecimal | **是** | 产品价格，必须 ≥ 0 |
| quantity | Integer | **是** | 产品数量，必须 ≥ 0 |
| imageUrl | String | 否 | 产品图片URL，可为 null |
| description | String | 否 | 产品描述，可为 null |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "name": "iPhone 15",
    "price": 7999.00,
    "quantity": 50,
    "imageUrl": "http://example.com/iphone15.jpg",
    "description": "Apple iPhone 15，搭载 A16 芯片",
    "createTime": "2026-05-13T12:00:00",
    "updateTime": "2026-05-13T12:00:00"
  }
}
```

**失败响应示例** (HTTP 400) - 缺少必填字段:
```json
{
  "code": 400,
  "message": "Product name cannot be empty",
  "data": null
}
```

**失败响应示例** (HTTP 400) - 价格为负数:
```json
{
  "code": 400,
  "message": "Product price cannot be negative",
  "data": null
}
```

---

### 4. 更新产品

**接口描述**: 根据ID更新产品信息（支持部分字段更新）

**请求方式**: `PUT`

**接口地址**: `/api/products/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 产品ID |

**请求头**:
```
Content-Type: application/json
```

**请求体**（可以只传入需要更新的字段）:
```json
{
  "name": "iPhone 15 Pro",
  "price": 8999.00,
  "description": "全新升级版本"
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 否 | 如果提供，不能为空字符串 |
| price | BigDecimal | 否 | 如果提供，必须 ≥ 0 |
| quantity | Integer | 否 | 如果提供，必须 ≥ 0 |
| imageUrl | String | 否 | 如果提供，更新图片URL |
| description | String | 否 | 如果提供，更新产品描述 |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "price": 8999.00,
    "quantity": 50,
    "imageUrl": "http://example.com/iphone15.jpg",
    "description": "全新升级版本",
    "createTime": "2026-05-13T10:30:00",
    "updateTime": "2026-05-13T12:30:00"
  }
}
```

**失败响应示例** (HTTP 404) - 产品不存在:
```json
{
  "code": 404,
  "message": "Product not found with id: 999",
  "data": null
}
```

**失败响应示例** (HTTP 400) - 参数校验失败:
```json
{
  "code": 400,
  "message": "Product price cannot be negative",
  "data": null
}
```

---

### 5. 删除产品

**接口描述**: 根据ID删除产品

**请求方式**: `DELETE`

**接口地址**: `/api/products/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 产品ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 6. 根据名称搜索产品

**接口描述**: 模糊搜索产品名称

**请求方式**: `GET`

**接口地址**: `/api/products/search/{name}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 搜索关键词（支持模糊匹配） |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "iPhone 15",
      "price": 7999.00,
      "quantity": 50,
      "imageUrl": "http://example.com/iphone15.jpg",
      "description": "Apple iPhone 15",
      "createTime": "2026-05-13T10:30:00",
      "updateTime": "2026-05-13T10:30:00"
    },
    {
      "id": 3,
      "name": "iPhone 15 Pro",
      "price": 8999.00,
      "quantity": 30,
      "imageUrl": null,
      "description": null,
      "createTime": "2026-05-13T12:00:00",
      "updateTime": "2026-05-13T12:30:00"
    }
  ]
}
```

---

### 7. 根据数量查找产品

**接口描述**: 查询库存数量大于等于指定值的产品

**请求方式**: `GET`

**接口地址**: `/api/products/quantity/{quantity}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| quantity | Integer | 是 | 最小库存数量 |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "iPhone 15",
      "price": 7999.00,
      "quantity": 50,
      "imageUrl": "http://example.com/iphone15.jpg",
      "description": "Apple iPhone 15",
      "createTime": "2026-05-13T10:30:00",
      "updateTime": "2026-05-13T10:30:00"
    }
  ]
}
```

---

### 8. 根据价格范围查询产品

**接口描述**: 查询价格在指定范围内的产品（MyBatis 实现）

**请求方式**: `GET`

**接口地址**: `/api/products/price-range`

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| minPrice | Double | 是 | 最低价格 |
| maxPrice | Double | 是 | 最高价格 |

**请求示例**:
```
GET /api/products/price-range?minPrice=5000&maxPrice=10000
```

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "iPhone 15",
      "price": 7999.00,
      "quantity": 50,
      "imageUrl": "http://example.com/iphone15.jpg",
      "description": "Apple iPhone 15",
      "createTime": "2026-05-13T10:30:00",
      "updateTime": "2026-05-13T10:30:00"
    }
  ]
}
```

---

### 9. 批量插入产品

**接口描述**: 批量创建多个产品（MyBatis 实现）

**请求方式**: `POST`

**接口地址**: `/api/products/batch`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
[
  {
    "name": "iPhone 15",
    "price": 7999.00,
    "quantity": 50,
    "imageUrl": "http://example.com/iphone15.jpg",
    "description": "Apple iPhone 15"
  },
  {
    "name": "MacBook Pro",
    "price": 14999.00,
    "quantity": 30,
    "imageUrl": null,
    "description": "MacBook Pro 14英寸"
  }
]
```

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": 2
}
```

**说明**: 返回的 data 为成功插入的记录数

**失败响应示例** (HTTP 400) - 某个产品校验失败:
```json
{
  "code": 400,
  "message": "Product name cannot be empty",
  "data": null
}
```

---

### 10. 按销量排序获取产品列表

**接口描述**: 获取按销量降序排列的产品列表（分页）

**请求方式**: `GET`

**接口地址**: `/api/products/sorted-by-sales`

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码（从1开始） |
| pageSize | Integer | 否 | 5 | 每页大小 |

**请求示例**:
```
GET /api/products/sorted-by-sales?page=1&pageSize=10
```

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "pageSize": 10,
    "total": 50,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false,
    "data": [
      {
        "id": 1,
        "name": "iPhone 15",
        "price": 7999.00,
        "quantity": 50,
        "sales": 1200,
        "imageUrl": "http://example.com/iphone15.jpg",
        "description": "Apple iPhone 15",
        "createTime": "2026-05-13T10:30:00",
        "updateTime": "2026-05-13T10:30:00"
      },
      {
        "id": 2,
        "name": "MacBook Pro",
        "price": 14999.00,
        "quantity": 30,
        "sales": 800,
        "imageUrl": null,
        "description": "MacBook Pro 14英寸",
        "createTime": "2026-05-13T11:00:00",
        "updateTime": "2026-05-13T11:00:00"
      }
    ]
  }
}
```

---

### 11. 增加产品销量

**接口描述**: 手动增加产品销量（通常用于线下销售记录等场景）

**请求方式**: `POST`

**接口地址**: `/api/products/{id}/increase-sales`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 产品ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| amount | Integer | 是 | 增加的销量数量（必须 > 0） |

**请求示例**:
```
POST /api/products/1/increase-sales?amount=10
```

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**失败响应示例** (HTTP 400):
```json
{
  "code": 400,
  "message": "Sales amount must be positive",
  "data": null
}
```

---

## ⚠️ 常见错误码

| 错误码 | 说明 | 常见原因 |
|--------|------|----------|
| 200 | 成功 | 请求成功处理 |
| 400 | 请求参数错误 | • 缺少必填字段<br/>• 字段格式不正确<br/>• 业务规则校验失败 |
| 404 | 资源不存在 | • 产品ID不存在 |
| 500 | 服务器内部错误 | • 数据库连接失败<br/>• 系统异常 |

---

## 📌 注意事项

### 1. 字段校验规则

**新增产品时**：
- ✅ `name`、`price`、`quantity` 为必填字段
- ✅ `price` 和 `quantity` 必须 ≥ 0
- ✅ `imageUrl` 和 `description` 可选，可以为 null

**更新产品时**：
- ✅ 支持部分更新，只传入需要修改的字段
- ✅ 如果传入 `name`，不能为空字符串
- ✅ 如果传入 `price` 或 `quantity`，必须 ≥ 0
- ✅ `imageUrl` 和 `description` 可以设置为 null

### 2. 时间字段说明

- `createTime` 和 `updateTime` 由后端自动管理
- 前端无需传入这两个字段
- 新增时，两个字段都设置为当前时间
- 更新时，只更新 `updateTime` 为当前时间

### 3. 部分更新示例

**只更新价格**:
```json
PUT /api/products/1
{
  "price": 8999.00
}
```

**只更新描述**:
```json
PUT /api/products/1
{
  "description": "全新升级版本"
}
```

**清空图片URL**:
```json
PUT /api/products/1
{
  "imageUrl": null
}
```

### 4. 搜索功能说明

- **名称搜索**: 使用 SQL LIKE 进行模糊匹配
- **数量查询**: 查询 quantity >= 指定值的产品
- **价格范围**: 查询 price 在 [minPrice, maxPrice] 范围内的产品

### 5. 库存检查策略

**重要说明**:

- **购物车阶段**: 添加到购物车或更新数量时，**不进行严格的库存检查**
  - 原因: 从加购到下单期间库存可能变化，提前检查意义有限
  - 建议: 前端可在购物车页面显示库存状态作为提示
  
- **下单阶段**: 创建订单时进行**严格的库存验证和锁定**
  - 时机: 先扣减库存，再创建订单项
  - 保障: 使用数据库原子操作，防止超卖
  - 失败处理: 库存不足时抛出异常，事务回滚

**最佳实践**:
```
用户加购 → 提示性检查（可选）→ 加入购物车
   ↓
用户下单 → 严格库存检查 → 扣减库存 → 创建订单
```

---

## 🔧 Postman 测试示例

### 创建产品
```
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "name": "测试产品",
  "price": 99.9,
  "quantity": 100,
  "description": "这是一个测试产品"
}
```

### 更新产品
```
PUT http://localhost:8080/api/products/1
Content-Type: application/json

{
  "price": 199.9,
  "description": "更新后的描述"
}
```

### 查询产品
```
GET http://localhost:8080/api/products/1
```

### 搜索产品
```
GET http://localhost:8080/api/products/search/iPhone
```

### 价格范围查询
```
GET http://localhost:8080/api/products/price-range?minPrice=5000&maxPrice=10000
```

---

## 👤 用户管理接口

### 基础路径: `/api/users`

#### 1. 获取所有用户（分页）

**请求方式**: `GET`

**接口地址**: `/api/users`

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码（从1开始） |
| pageSize | Integer | 否 | 10 | 每页大小 |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "pageSize": 10,
    "total": 50,
    "items": [
      {
        "id": 1,
        "openid": "oXXXX...",
        "nickname": "张三",
        "avatarUrl": "http://example.com/avatar.jpg",
        "gender": 1,
        "phone": "13800138000",
        "status": 1,
        "lastLoginTime": "2026-05-14T10:30:00",
        "createTime": "2026-05-13T10:30:00",
        "updateTime": "2026-05-14T10:30:00"
      }
    ]
  }
}
```

---

#### 2. 根据 ID 获取用户

**请求方式**: `GET`

**接口地址**: `/api/users/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "openid": "oXXXX...",
    "nickname": "张三",
    "avatarUrl": "http://example.com/avatar.jpg",
    "gender": 1,
    "phone": "13800138000",
    "status": 1,
    "lastLoginTime": "2026-05-14T10:30:00",
    "createTime": "2026-05-13T10:30:00",
    "updateTime": "2026-05-14T10:30:00"
  }
}
```

---

#### 3. 根据 OpenID 获取用户

**请求方式**: `GET`

**接口地址**: `/api/users/openid/{openid}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| openid | String | 是 | 微信OpenID |

---

#### 4. 创建新用户

**请求方式**: `POST`

**接口地址**: `/api/users`

**请求体**:
```json
{
  "openid": "oXXXX...",
  "nickname": "张三",
  "avatarUrl": "http://example.com/avatar.jpg",
  "gender": 1,
  "phone": "13800138000"
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| openid | String | **是** | 微信OpenID，用户唯一标识 |
| nickname | String | 否 | 用户昵称 |
| avatarUrl | String | 否 | 头像URL |
| gender | Integer | 否 | 性别：0-未知, 1-男, 2-女，默认0 |
| phone | String | 否 | 手机号（需符合格式：1开头11位数字） |

---

#### 5. 更新用户信息

**请求方式**: `PUT`

**接口地址**: `/api/users/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**请求体**（支持部分更新）:
```json
{
  "nickname": "李四",
  "phone": "13900139000"
}
```

---

#### 6. 删除用户

**请求方式**: `DELETE`

**接口地址**: `/api/users/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

---

#### 7. 禁用/启用用户

**禁用用户**:
```
POST /api/users/{id}/disable
```

**启用用户**:
```
POST /api/users/{id}/enable
```

---

#### 8. 更新最后登录时间

**请求方式**: `POST`

**接口地址**: `/api/users/{id}/login`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

---

#### 9. 批量操作

**批量插入用户**:
```
POST /api/users/batch
Content-Type: application/json

[
  {"openid": "o1", "nickname": "用户1"},
  {"openid": "o2", "nickname": "用户2"}
]
```

**批量更新用户状态**:
```
POST /api/users/batch/status?status=0
Content-Type: application/json

[1, 2, 3]
```

---

#### 10. 查询活跃用户

**请求方式**: `GET`

**接口地址**: `/api/users/active?days=7`

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| days | Integer | 否 | 7 | 最近多少天内有登录 |

---

## 🏷️ 商品分类管理接口

### 基础路径: `/api/categories`

#### 1. 获取所有分类列表

**请求方式**: `GET`

**接口地址**: `/api/categories`

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "数码产品",
      "iconUrl": "http://example.com/icon/digital.png",
      "sortOrder": 100,
      "parentId": 0,
      "status": 1,
      "createTime": "2026-05-13T10:30:00",
      "updateTime": "2026-05-13T10:30:00"
    },
    {
      "id": 2,
      "name": "服装鞋帽",
      "iconUrl": "http://example.com/icon/clothing.png",
      "sortOrder": 90,
      "parentId": 0,
      "status": 1,
      "createTime": "2026-05-13T10:30:00",
      "updateTime": "2026-05-13T10:30:00"
    }
  ]
}
```

---

#### 2. 根据父分类ID获取子分类

**请求方式**: `GET`

**接口地址**: `/api/categories/sub/{parentId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentId | Long | 是 | 父分类ID，0表示顶级分类 |

**成功响应示例**: 同上（返回子分类列表）

---

#### 3. 根据ID获取分类详情

**请求方式**: `GET`

**接口地址**: `/api/categories/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |

---

#### 4. 创建分类

**请求方式**: `POST`

**接口地址**: `/api/categories`

**请求体**:
```json
{
  "name": "手机配件",
  "iconUrl": "http://example.com/icon/accessories.png",
  "sortOrder": 80,
  "parentId": 1,
  "status": 1
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| name | String | **是** | - | 分类名称 |
| iconUrl | String | 否 | null | 分类图标URL |
| sortOrder | Integer | 否 | 0 | 排序权重，数值越大越靠前 |
| parentId | Long | 否 | 0 | 父分类ID，0表示顶级分类 |
| status | Integer | 否 | 1 | 状态：0-隐藏 1-显示 |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 3,
    "name": "手机配件",
    "iconUrl": "http://example.com/icon/accessories.png",
    "sortOrder": 80,
    "parentId": 1,
    "status": 1,
    "createTime": "2026-05-15T10:30:00",
    "updateTime": "2026-05-15T10:30:00"
  }
}
```

---

#### 5. 更新分类

**请求方式**: `PUT`

**接口地址**: `/api/categories/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |

**请求体**（支持部分更新）:
```json
{
  "name": "手机壳/膜",
  "sortOrder": 85
}
```

---

#### 6. 删除分类

**请求方式**: `DELETE`

**接口地址**: `/api/categories/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 分类ID |

**注意**: 删除分类前请确保该分类下没有商品，否则可能导致数据不一致

---

## 🎠 轮播图管理接口

### 基础路径: `/api/banners`

#### 1. 获取所有轮播图列表

**请求方式**: `GET`

**接口地址**: `/api/banners`

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "新品上市",
      "imageUrl": "http://example.com/banner/new-product.jpg",
      "linkType": 1,
      "linkValue": "P101",
      "sortOrder": 100,
      "status": 1,
      "startTime": "2026-05-01T00:00:00",
      "endTime": "2026-05-31T23:59:59",
      "createTime": "2026-05-01T10:00:00"
    }
  ]
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| linkType | Integer | 跳转类型：1-商品详情 2-分类页 3-小程序页面 4-外部链接 |
| linkValue | String | 跳转目标（商品编号/分类ID/页面路径/URL） |

---

#### 2. 获取当前有效的轮播图列表

**请求方式**: `GET`

**接口地址**: `/api/banners/active`

**说明**: 只返回status=1且在有效期内的轮播图

**成功响应示例**: 同上（只返回有效的轮播图）

---

#### 3. 根据ID获取轮播图详情

**请求方式**: `GET`

**接口地址**: `/api/banners/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 轮播图ID |

---

#### 4. 创建轮播图

**请求方式**: `POST`

**接口地址**: `/api/banners`

**请求体**:
```json
{
  "title": "促销活动",
  "imageUrl": "http://example.com/banner/promotion.jpg",
  "linkType": 2,
  "linkValue": "1",
  "sortOrder": 90,
  "status": 1,
  "startTime": "2026-05-15T00:00:00",
  "endTime": "2026-05-20T23:59:59"
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| title | String | 否 | null | 轮播图标题（管理用） |
| imageUrl | String | **是** | - | 图片地址 |
| linkType | Integer | 否 | 1 | 跳转类型：1-商品详情 2-分类页 3-小程序页面 4-外部链接 |
| linkValue | String | 否 | null | 跳转目标 |
| sortOrder | Integer | 否 | 0 | 排序权重 |
| status | Integer | 否 | 1 | 状态：0-隐藏 1-显示 |
| startTime | String | 否 | null | 生效开始时间（ISO 8601格式） |
| endTime | String | 否 | null | 生效结束时间（ISO 8601格式） |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "title": "促销活动",
    "imageUrl": "http://example.com/banner/promotion.jpg",
    "linkType": 2,
    "linkValue": "1",
    "sortOrder": 90,
    "status": 1,
    "startTime": "2026-05-15T00:00:00",
    "endTime": "2026-05-20T23:59:59",
    "createTime": "2026-05-15T10:30:00"
  }
}
```

---

#### 5. 更新轮播图

**请求方式**: `PUT`

**接口地址**: `/api/banners/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 轮播图ID |

**请求体**（支持部分更新）:
```json
{
  "sortOrder": 95,
  "status": 0
}
```

---

#### 6. 删除轮播图

**请求方式**: `DELETE`

**接口地址**: `/api/banners/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 轮播图ID |

---

## 🔑 用户会话管理接口

### 基础路径: `/api/sessions`

**说明**: 本模块采用 JWT + Redis 混合认证方案
- **JWT**: 无状态认证，包含用户身份信息，有效期24小时
- **Redis**: Token黑名单、用户信息缓存、会话管理

---

#### 1. 用户登录

**请求方式**: `POST`

**接口地址**: `/api/sessions/login`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "userId": 1,
  "sessionKey": "wechat_session_key"
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | **是** | 用户ID |
| sessionKey | String | **是** | 微信会话密钥（用于验证用户身份） |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "openid": "oXXXX...",
    "nickname": "张三",
    "avatarUrl": "http://example.com/avatar.jpg",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
    "sessionKey": "wechat_session_key"
  }
}
```

**响应数据字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户ID |
| openid | String | 微信OpenID |
| nickname | String | 用户昵称 |
| avatarUrl | String | 头像URL |
| token | String | JWT Token，后续请求需在Authorization头中携带 |
| sessionKey | String | 微信会话密钥 |

**使用说明**:
1. 登录成功后，前端应保存返回的 `token`
2. 后续访问需要认证的接口时，在请求头中添加：`Authorization: Bearer {token}`
3. Token有效期为24小时，过期后需重新登录

---

#### 2. 用户登出

**请求方式**: `POST`

**接口地址**: `/api/sessions/logout`

**请求头**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**请求体**:
```json
{
  "userId": 1
}
```

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**说明**: 
- 登出后，Token会被加入Redis黑名单
- Token在剩余有效期内无法再次使用
- 实现主动登出功能，增强安全性

---

#### 3. 验证Token有效性

**请求方式**: `GET`

**接口地址**: `/api/sessions/validate?token={JWT_TOKEN}`

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| token | String | **是** | JWT Token（可带Bearer前缀或不带） |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**响应说明**:
- `true`: Token有效
- `false`: Token无效或已过期或在黑名单中

**使用场景**:
- 前端在访问受保护页面前可以先验证Token是否有效
- Token即将过期时提示用户重新登录

---

#### 4. 获取用户会话列表

**请求方式**: `GET`

**接口地址**: `/api/sessions/user/{userId}?page=1&pageSize=10`

---

#### 5. 清理过期会话

**请求方式**: `POST`

**接口地址**: `/api/sessions/cleanup`

---

## 📍 用户收货地址管理接口

### 基础路径: `/api/addresses`

#### 1. 创建地址

**请求方式**: `POST`

**接口地址**: `/api/addresses`

**请求体**:
```json
{
  "userId": 1,
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "province": "广东省",
  "city": "深圳市",
  "district": "南山区",
  "address": "科技南路100号",
  "isDefault": true
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | **是** | 用户ID |
| receiverName | String | **是** | 收货人姓名 |
| receiverPhone | String | **是** | 收货人手机号（1开头11位） |
| province | String | **是** | 省份 |
| city | String | **是** | 城市 |
| district | String | **是** | 区县 |
| address | String | **是** | 详细地址 |
| isDefault | Boolean | 否 | 是否默认地址，默认false |

---

#### 2. 获取用户地址列表

**请求方式**: `GET`

**接口地址**: `/api/addresses/user/{userId}?page=1&pageSize=10`

**成功响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "pageSize": 10,
    "total": 3,
    "items": [
      {
        "id": 1,
        "userId": 1,
        "receiverName": "张三",
        "receiverPhone": "13800138000",
        "province": "广东省",
        "city": "深圳市",
        "district": "南山区",
        "address": "科技南路100号",
        "isDefault": true,
        "createTime": "2026-05-14T10:30:00",
        "updateTime": "2026-05-14T10:30:00"
      }
    ]
  }
}
```

---

#### 3. 获取默认地址

**请求方式**: `GET`

**接口地址**: `/api/addresses/user/{userId}/default`

---

#### 4. 设置默认地址

**请求方式**: `POST`

**接口地址**: `/api/addresses/{id}/set-default`

---

#### 5. 更新地址

**请求方式**: `PUT`

**接口地址**: `/api/addresses/{id}`

**请求体**（支持部分更新）:
```json
{
  "receiverPhone": "13900139000",
  "address": "科技南路200号"
}
```

---

#### 6. 删除地址（逻辑删除）

**请求方式**: `DELETE`

**接口地址**: `/api/addresses/{id}`

---

#### 7. 搜索地址

**按收货人姓名搜索（分页）**:
```
GET /api/addresses/search/receiver-name?receiverName=张&page=1&pageSize=10
```

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| receiverName | String | **是** | - | 收货人姓名关键词 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**成功响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "pageSize": 10,
    "total": 5,
    "items": [
      {
        "id": 1,
        "userId": 1,
        "receiverName": "张三",
        "receiverPhone": "13800138000",
        "province": "广东省",
        "city": "深圳市",
        "district": "南山区",
        "address": "科技南路100号",
        "isDefault": true,
        "createTime": "2026-05-14T10:30:00",
        "updateTime": "2026-05-14T10:30:00"
      }
    ]
  }
}
```

---

**按手机号搜索（分页）**:
```
GET /api/addresses/search/phone?receiverPhone=13800138000&page=1&pageSize=10
```

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| receiverPhone | String | **是** | - | 收货人手机号 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

---

**按省市搜索（分页）**:
```
GET /api/addresses/search/location?province=广东省&city=深圳市&page=1&pageSize=10
```

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| province | String | **是** | - | 省份 |
| city | String | **是** | - | 城市 |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

---

## 🛒 购物车管理接口

### 基础路径: `/api/cart`

**注意**: 所有购物车接口都需要 JWT 认证，请在请求头中携带 `Authorization: Bearer {token}`

#### 1. 添加商品到购物车

**请求方式**: `POST`

**接口地址**: `/api/cart`

**请求体**:
```json
{
  "userId": 1,
  "productId": 10,
  "quantity": 2
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| userId | Long | **是** | - | 用户ID |
| productId | Long | **是** | - | 产品ID |
| quantity | Integer | 否 | 1 | 购买数量（必须 > 0） |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "productId": 10,
    "quantity": 2,
    "selected": true,
    "createTime": "2026-05-15T10:30:00",
    "updateTime": "2026-05-15T10:30:00",
    "productName": "iPhone 15",
    "productPrice": 7999.00,
    "productImage": "http://example.com/iphone15.jpg",
    "productStock": 50,
    "subtotal": 15998.00
  }
}
```

**说明**: 
- 如果购物车中已存在该产品，则累加数量
- 购物车阶段不进行严格的库存检查，真正库存锁定在下单时进行

---

#### 2. 获取用户购物车列表

**请求方式**: `GET`

**接口地址**: `/api/cart/{userId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "productId": 10,
      "quantity": 2,
      "selected": true,
      "createTime": "2026-05-15T10:30:00",
      "updateTime": "2026-05-15T10:30:00",
      "productName": "iPhone 15",
      "productPrice": 7999.00,
      "productImage": "http://example.com/iphone15.jpg",
      "productStock": 50,
      "subtotal": 15998.00
    },
    {
      "id": 2,
      "userId": 1,
      "productId": 20,
      "quantity": 1,
      "selected": false,
      "createTime": "2026-05-15T11:00:00",
      "updateTime": "2026-05-15T11:00:00",
      "productName": "MacBook Pro",
      "productPrice": 14999.00,
      "productImage": null,
      "productStock": 30,
      "subtotal": 14999.00
    }
  ]
}
```

---

#### 3. 更新购物车项数量

**请求方式**: `PUT`

**接口地址**: `/api/cart/{cartItemId}/quantity`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cartItemId | Long | 是 | 购物车项ID |

**请求体**:
```json
{
  "quantity": 5
}
```

---

#### 4. 删除购物车项

**请求方式**: `DELETE`

**接口地址**: `/api/cart/{cartItemId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cartItemId | Long | 是 | 购物车项ID |

---

#### 5. 更新购物车项选中状态

**请求方式**: `PUT`

**接口地址**: `/api/cart/{cartItemId}/selected`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cartItemId | Long | 是 | 购物车项ID |

**请求体**:
```json
{
  "selected": true
}
```

---

#### 6. 批量更新用户购物车项选中状态

**请求方式**: `PUT`

**接口地址**: `/api/cart/{userId}/select-all`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**请求体**:
```json
{
  "selected": true
}
```

**说明**: 全选或取消全选用户的所有购物车项

---

#### 7. 获取用户选中的购物车项

**请求方式**: `GET`

**接口地址**: `/api/cart/{userId}/selected`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "productId": 10,
      "quantity": 2,
      "selected": true,
      "createTime": "2026-05-15T10:30:00",
      "updateTime": "2026-05-15T10:30:00",
      "productName": "iPhone 15",
      "productPrice": 7999.00,
      "productImage": "http://example.com/iphone15.jpg",
      "productStock": 50,
      "subtotal": 15998.00
    }
  ]
}
```

---

#### 8. 计算选中商品的总金额

**请求方式**: `GET`

**接口地址**: `/api/cart/{userId}/total`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": 15998.00
}
```

---

#### 9. 清空用户购物车

**请求方式**: `DELETE`

**接口地址**: `/api/cart/{userId}/clear`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

---

#### 10. 批量删除购物车项

**请求方式**: `POST`

**接口地址**: `/api/cart/batch-delete`

**请求体**:
```json
[1, 2, 3]
```

**说明**: 传入购物车项ID列表

---

## 📦 订单管理接口

### 基础路径: `/api/orders`

**注意**: 所有订单接口都需要 JWT 认证，请在请求头中携带 `Authorization: Bearer {token}`

#### 1. 创建订单（从购物车结算）

**请求方式**: `POST`

**接口地址**: `/api/orders`

**请求体**:
```json
{
  "userId": 1,
  "addressId": 5,
  "cartItemIds": [1, 2, 3],
  "remark": "请尽快发货"
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | **是** | 用户ID |
| addressId | Long | **是** | 收货地址ID |
| cartItemIds | List<Long> | **是** | 购物车项ID列表，传 `[0]` 表示选择所有选中项 |
| remark | String | 否 | 订单备注 |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "orderNo": "ORD1715745000000A1B2C3D4",
    "userId": 1,
    "totalAmount": 23997.00,
    "actualAmount": 23997.00,
    "status": 0,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverAddress": "广东省深圳市南山区科技南路100号",
    "remark": "请尽快发货",
    "paymentTime": null,
    "deliveryTime": null,
    "completionTime": null,
    "cancellationTime": null,
    "isDeleted": false,
    "createTime": "2026-05-15T10:30:00",
    "updateTime": "2026-05-15T10:30:00",
    "items": [
      {
        "id": 201,
        "orderId": 100,
        "productId": 10,
        "productName": "iPhone 15",
        "productImage": "http://example.com/iphone15.jpg",
        "price": 7999.00,
        "quantity": 2,
        "totalPrice": 15998.00
      },
      {
        "id": 202,
        "orderId": 100,
        "productId": 20,
        "productName": "MacBook Pro",
        "productImage": null,
        "price": 14999.00,
        "quantity": 1,
        "totalPrice": 14999.00
      }
    ],
    "itemCount": 2
  }
}
```

**说明**: 
- 创建订单时会先扣减库存，再创建订单项，保证数据一致性
- 订单创建成功后，对应的购物车项会被自动删除
- 订单状态：0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消

**失败响应示例** (HTTP 400) - 库存不足:
```json
{
  "code": 400,
  "message": "Insufficient stock for product: iPhone 15. Available: 1, Requested: 2",
  "data": null
}
```

---

#### 2. 获取订单详情

**请求方式**: `GET`

**接口地址**: `/api/orders/{orderId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**成功响应示例**: 同上（创建订单响应）

---

#### 3. 根据订单号获取订单

**请求方式**: `GET`

**接口地址**: `/api/orders/no/{orderNo}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | String | 是 | 订单编号 |

---

#### 4. 获取用户订单列表（分页）

**请求方式**: `GET`

**接口地址**: `/api/orders/user/{userId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | Integer | 否 | - | 订单状态筛选（0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消） |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页大小 |

**请求示例**:
```
GET /api/orders/user/1?status=0&page=1&size=10
```

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "pageSize": 10,
    "total": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false,
    "data": [
      {
        "id": 100,
        "orderNo": "ORD1715745000000A1B2C3D4",
        "userId": 1,
        "totalAmount": 23997.00,
        "actualAmount": 23997.00,
        "status": 0,
        "receiverName": "张三",
        "receiverPhone": "13800138000",
        "receiverAddress": "广东省深圳市南山区科技南路100号",
        "remark": "请尽快发货",
        "createTime": "2026-05-15T10:30:00",
        "updateTime": "2026-05-15T10:30:00",
        "itemCount": 2
      }
    ]
  }
}
```

---

#### 5. 取消订单

**请求方式**: `PUT`

**接口地址**: `/api/orders/{orderId}/cancel`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**请求体**:
```json
{
  "userId": 1
}
```

**说明**: 
- 只能取消待支付状态的订单（status = 0）
- 取消订单后会自动恢复库存

---

#### 6. 支付订单

**请求方式**: `PUT`

**接口地址**: `/api/orders/{orderId}/pay`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**请求体**:
```json
{
  "userId": 1
}
```

**说明**: 只能支付待支付状态的订单

---

#### 7. 发货（管理员功能）

**请求方式**: `PUT`

**接口地址**: `/api/orders/{orderId}/deliver`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**说明**: 
- 只能发货已支付的订单（status = 1）
- 此接口通常需要管理员权限

---

#### 8. 确认收货

**请求方式**: `PUT`

**接口地址**: `/api/orders/{orderId}/confirm`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**请求体**:
```json
{
  "userId": 1
}
```

**说明**: 只能确认已发货的订单（status = 2）

---

#### 9. 删除订单（软删除）

**请求方式**: `DELETE`

**接口地址**: `/api/orders/{orderId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**请求体**:
```json
{
  "userId": 1
}
```

**说明**: 
- 软删除，不会真正从数据库删除
- 删除后订单在列表中不再显示

---

## 🔐 认证机制详细说明

### JWT + Redis 混合认证方案

本系统采用 **JWT (JSON Web Token) + Redis** 的混合认证方案，结合了无状态认证和有状态管理的优势。

#### 认证流程

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   前端       │         │   后端        │         │   Redis     │
└──────┬──────┘         └──────┬───────┘         └──────┬──────┘
       │                       │                        │
       │  1. POST /login       │                        │
       │  {userId, sessionKey} │                        │
       │ ─────────────────────>│                        │
       │                       │                        │
       │                       │  2. 验证用户身份        │
       │                       │ ──────────────────────>│
       │                       │                        │
       │                       │  3. 生成 JWT Token      │
       │                       │     (有效期24小时)      │
       │                       │                        │
       │  4. 返回 Token        │                        │
       │ <─────────────────────│                        │
       │                       │                        │
       │  5. 存储 Token        │                        │
       │     (LocalStorage)    │                        │
       │                       │                        │
       │  6. 访问受保护接口     │                        │
       │  Authorization:       │                        │
       │  Bearer {token}       │                        │
       │ ─────────────────────>│                        │
       │                       │  7. 检查Token黑名单     │
       │                       │ ──────────────────────>│
       │                       │                        │
       │  8. 返回数据          │                        │
       │ <─────────────────────│                        │
```

#### JWT Token 结构

JWT Token 包含三部分：Header.Payload.Signature

**Payload 示例**:
```json
{
  "userId": 1,
  "openid": "oXXXX...",
  "iat": 1715745000,  // 签发时间
  "exp": 1715831400   // 过期时间（24小时后）
}
```

#### Redis 存储结构

| 用途 | Key 格式 | TTL | 说明 |
|------|---------|-----|------|
| Token 黑名单 | `token:blacklist:{token}` | Token剩余有效期 | 实现主动登出 |
| 用户信息缓存 | `user:info:{userId}` | 30分钟 | 减少数据库查询 |
| 用户会话 | `session:{userId}` | 7天 | 存储 session_key |

#### 需要认证的接口

以下接口需要在请求头中携带 JWT Token：

- `/api/orders/**` - 订单相关接口
- `/api/cart/**` - 购物车相关接口

**请求头示例**:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 不需要认证的接口

以下接口无需认证即可访问：

- `/api/users/**` - 用户注册/查询
- `/api/products/**` - 商品浏览
- `/api/categories/**` - 分类浏览
- `/api/banners/**` - 轮播图浏览
- `/api/sessions/login` - 登录接口
- `/api/sessions/validate` - Token验证接口

#### Token 管理最佳实践

**前端实现建议**:

1. **Token 存储**:
   ```javascript
   // 登录成功后保存 Token
   localStorage.setItem('token', response.data.token);
   localStorage.setItem('userId', response.data.userId);
   ```

2. **请求拦截器**:
   ```javascript
   // Axios 请求拦截器示例
   axios.interceptors.request.use(config => {
     const token = localStorage.getItem('token');
     if (token) {
       config.headers.Authorization = `Bearer ${token}`;
     }
     return config;
   });
   ```

3. **响应拦截器**:
   ```javascript
   // 处理 Token 过期
   axios.interceptors.response.use(
     response => response,
     error => {
       if (error.response?.status === 401) {
         // Token 过期或无效，跳转到登录页
         localStorage.removeItem('token');
         window.location.href = '/login';
       }
       return Promise.reject(error);
     }
   );
   ```

4. **Token 刷新策略**:
   - Token 有效期为24小时
   - 建议在 Token 即将过期前（如剩余1小时）提示用户重新登录
   - 或者实现 Refresh Token 机制（当前版本未实现）

#### 安全注意事项

1. **HTTPS**: 生产环境必须使用 HTTPS 传输，防止 Token 被窃取
2. **Token 泄露**: 如果 Token 泄露，用户应立即登出使 Token 失效
3. **XSS 防护**: 注意防范 XSS 攻击，避免 Token 被恶意脚本获取
4. **CSRF 防护**: 虽然 JWT 本身不受 CSRF 攻击，但仍建议实施 CSRF 防护措施
5. **敏感操作**: 对于支付等敏感操作，建议二次验证

---

## 📝 开发指南

### 环境要求

- **JDK**: 21+
- **Maven**: 3.6+
- **MySQL**: 8.0+
- **Redis**: 6.0+

### 快速启动

1. **配置环境变量**（JWT_SECRET已有默认值，可选配置）:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_USERNAME=root
   export DB_PASSWORD=your_password
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   # JWT_SECRET 已有默认值，开发环境可以不设置
   # export JWT_SECRET=your-secret-key-at-least-256-bits-long
   ```

2. **初始化数据库**:
   ```bash
   mysql -u root -p < database/schema.sql
   ```

3. **启动应用**:
   ```bash
   mvn spring-boot:run
   ```

4. **访问接口**:
   ```
   http://localhost:8080/api/products
   ```

### 健康检查

```bash
GET http://localhost:8080/actuator/health
```

响应:
```json
{
  "status": "UP"
}
```

---

## ❓ 常见问题

### 1. Token 过期怎么办？

Token 有效期为24小时，过期后需要重新登录获取新 Token。

### 2. 如何处理并发下单？

系统使用数据库原子操作扣减库存，保证并发安全。如果库存不足会返回错误。

### 3. 支持批量操作吗？

部分接口支持批量操作：
- 批量创建商品: `POST /api/products/batch`
- 批量删除购物车项: `POST /api/cart/batch-delete`
- 批量更新用户状态: `POST /api/users/batch/status`

### 4. 如何测试 API？

可以使用以下工具：
- **Postman**: 导入接口集合进行测试
- **curl**: 命令行测试
- **浏览器**: GET 请求可直接在浏览器中访问

### 5. 分页参数如何使用？

大部分列表接口支持分页：
- `page`: 页码，从1开始，默认1
- `pageSize`: 每页大小，默认值因接口而异

示例:
```
GET /api/products?page=2&pageSize=10
```

### 6. 并发安全说明

#### 用户登录并发处理

系统已修复UserService的竞态条件问题：
- **场景**: 同一用户同时在多个设备登录时，可能触发并发创建用户
- **解决方案**: 重试机制（最多3次）+ 数据库唯一约束
- **性能影响**: 正常情况无影响，仅在极端并发下触发重试

#### 库存扣减并发控制

订单创建时使用原子操作扣减库存：
```sql
UPDATE products SET stock = stock - :quantity 
WHERE id = :id AND stock >= :quantity
```
- 保证高并发下的库存一致性
- 防止超卖问题
- 库存不足时立即返回错误

---

如有问题，请联系后端开发团队。

**文档版本**: v5.1  
**最后更新**: 2026-05-16  
**更新内容**: 
- 新增订单管理接口完整文档
- 新增购物车管理接口完整文档
- 新增按销量排序和增加销量接口
- 优化库存检查策略说明
- 补充用户会话管理接口（JWT认证）
- 补充地址管理接口完整文档
- 补充分类管理接口文档
- 补充轮播图管理接口文档
- 完善认证机制说明
- **v5.0**: 补充支付接口、商品SKU接口、商品分类筛选接口、用户活跃查询接口等遗漏接口
- **v5.1**: JWT密钥配置优化（添加默认值），修复UserService竞态条件问题

---

## 💳 支付管理接口

### 基础路径: `/api/payment`

**注意**: 支付功能目前为占位实现，后续将集成微信支付或其他第三方支付平台

#### 1. 创建支付订单

**请求方式**: `POST`

**接口地址**: `/api/payment/create`

**请求体**:
```json
{
  "orderNo": "ORD1715745000000A1B2C3D4",
  "totalAmount": 23997.00,
  "openid": "oXXXX..."
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | String | **是** | 订单编号 |
| totalAmount | Double | **是** | 支付金额（单位：元） |
| openid | String | **是** | 用户微信OpenID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "appId": "wx1234567890",
    "timeStamp": "1715745000",
    "nonceStr": "abc123",
    "package": "prepay_id=wx123",
    "signType": "MD5",
    "paySign": "signature"
  }
}
```

**未实现响应** (HTTP 200):
```json
{
  "code": 501,
  "message": "支付功能暂未实现，敬请期待",
  "data": null
}
```

---

#### 2. 查询支付状态

**请求方式**: `GET`

**接口地址**: `/api/payment/status/{orderNo}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNo | String | 是 | 订单编号 |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": "SUCCESS"  // SUCCESS-支付成功, NOTPAY-未支付, CLOSED-已关闭
}
```

---

#### 3. 申请退款

**请求方式**: `POST`

**接口地址**: `/api/payment/refund`

**请求体**:
```json
{
  "orderNo": "ORD1715745000000A1B2C3D4",
  "refundAmount": 7999.00
}
```

**请求体字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderNo | String | **是** | 订单编号 |
| refundAmount | Double | **是** | 退款金额（单位：元） |

---

#### 4. 微信支付回调接口

**请求方式**: `POST`

**接口地址**: `/api/payment/wechat/callback`

**说明**: 
- 此接口由微信支付平台调用，前端无需调用
- 用于接收微信支付结果通知
- 返回 "SUCCESS" 表示处理成功，微信不再重复通知

---

## 🏷️ 商品SKU管理接口

### 基础路径: `/api/products`

#### 1. 获取商品的所有SKU

**请求方式**: `GET`

**接口地址**: `/api/products/{productId}/skus`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "productId": 10,
      "skuName": "iPhone 15 - 黑色 - 128GB",
      "price": 7999.00,
      "stock": 50,
      "sales": 100,
      "specValues": "{\"颜色\": \"黑色\", \"容量\": \"128GB\"}",
      "createTime": "2026-05-15T10:30:00",
      "updateTime": "2026-05-15T10:30:00"
    },
    {
      "id": 2,
      "productId": 10,
      "skuName": "iPhone 15 - 白色 - 256GB",
      "price": 8999.00,
      "stock": 30,
      "sales": 80,
      "specValues": "{\"颜色\": \"白色\", \"容量\": \"256GB\"}",
      "createTime": "2026-05-15T10:30:00",
      "updateTime": "2026-05-15T10:30:00"
    }
  ]
}
```

---

#### 2. 获取指定SKU详情

**请求方式**: `GET`

**接口地址**: `/api/products/skus/{skuId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuId | Long | 是 | SKU ID |

---

#### 3. 按分类查询商品（分页）

**请求方式**: `GET`

**接口地址**: `/api/products/category/{categoryId}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | Long | 是 | 分类ID |

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码（从1开始） |
| pageSize | Integer | 否 | 10 | 每页大小 |

**请求示例**:
```
GET /api/products/category/1?page=1&pageSize=10
```

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "page": 1,
    "pageSize": 10,
    "total": 25,
    "totalPages": 3,
    "hasNext": true,
    "hasPrevious": false,
    "data": [
      {
        "id": 1,
        "name": "iPhone 15",
        "price": 7999.00,
        "quantity": 50,
        "sales": 1200,
        "imageUrl": "http://example.com/iphone15.jpg",
        "description": "Apple iPhone 15",
        "categoryId": 1,
        "createTime": "2026-05-13T10:30:00",
        "updateTime": "2026-05-13T10:30:00"
      }
    ]
  }
}
```

---

## 👤 用户管理接口（补充）

### 基础路径: `/api/users`

#### 补充接口1: 根据状态获取用户（分页）

**请求方式**: `GET`

**接口地址**: `/api/users/status/{status}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 是 | 用户状态：0-禁用, 1-正常 |

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

---

#### 补充接口2: 根据昵称搜索用户（分页）

**请求方式**: `GET`

**接口地址**: `/api/users/search?nickname=张&page=1&pageSize=10`

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| nickname | String | **是** | - | 用户昵称关键词（模糊匹配） |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

---

#### 补充接口3: 根据注册时间范围查询用户（分页）

**请求方式**: `GET`

**接口地址**: `/api/users/range?startTime=2026-05-01T00:00:00&endTime=2026-05-31T23:59:59&page=1&pageSize=10`

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| startTime | LocalDateTime | **是** | - | 开始时间（ISO 8601格式） |
| endTime | LocalDateTime | **是** | - | 结束时间（ISO 8601格式） |
| page | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

---

#### 补充接口4: 查询活跃用户

**请求方式**: `GET`

**接口地址**: `/api/users/active?days=7`

**查询参数**:

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| days | Integer | 否 | 7 | 最近多少天内有登录记录 |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "openid": "oXXXX...",
      "nickname": "张三",
      "avatarUrl": "http://example.com/avatar.jpg",
      "lastLoginTime": "2026-05-14T10:30:00",
      "createTime": "2026-05-13T10:30:00"
    }
  ]
}
```

---

## 📍 用户收货地址管理接口（补充）

### 基础路径: `/api/addresses`

#### 补充接口1: 获取用户的所有地址列表（默认地址优先）

**请求方式**: `GET`

**接口地址**: `/api/addresses/user/{userId}/list`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**说明**: 与分页接口不同，此接口返回所有地址，且默认地址排在前面

---

#### 补充接口2: 统计用户的地址数量

**请求方式**: `GET`

**接口地址**: `/api/addresses/user/{userId}/count`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

---

#### 补充接口3: 批量设置默认地址

**请求方式**: `POST`

**接口地址**: `/api/addresses/batch/set-default?userId=1&addressId=5`

**查询参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | **是** | 用户ID |
| addressId | Long | **是** | 要设置为默认的地址ID |

**说明**: 会将该用户的所有其他地址取消默认，只保留指定地址为默认

---

#### 补充接口4: 批量逻辑删除用户地址

**请求方式**: `POST`

**接口地址**: `/api/addresses/batch/delete`

**请求体**:
```json
[1, 2, 3]
```

**说明**: 传入用户ID列表，批量逻辑删除这些用户的地址

---

#### 补充接口5: 统计每个用户的地址数量

**请求方式**: `GET`

**接口地址**: `/api/addresses/stats/by-user`

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": 1,
      "addressCount": 5
    },
    {
      "userId": 2,
      "addressCount": 3
    }
  ]
}
```

---

## 🛒 购物车管理接口（补充）

### 基础路径: `/api/cart`

#### 补充说明: AddToCartRequest 数据结构

**添加商品到购物车的请求体结构**:

```json
{
  "userId": 1,
  "productId": 10,
  "skuId": 1,
  "quantity": 2
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| userId | Long | **是** | - | 用户ID |
| productId | Long | **是** | - | 产品ID |
| skuId | Long | 否 | null | SKU ID（如果商品有规格） |
| quantity | Integer | 否 | 1 | 购买数量（必须 > 0） |

---

## 📦 订单管理接口（补充）

### 基础路径: `/api/orders`

#### 补充接口: 获取用户订单统计信息

**请求方式**: `GET`

**接口地址**: `/api/orders/user/{userId}/stats`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**成功响应示例** (HTTP 200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalOrders": 50,
    "unpaidOrders": 2,
    "unshippedOrders": 3,
    "unreceivedOrders": 5,
    "completedOrders": 40,
    "cancelledOrders": 5,
    "totalAmount": 125000.00
  }
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| totalOrders | Integer | 总订单数 |
| unpaidOrders | Integer | 待支付订单数 |
| unshippedOrders | Integer | 待发货订单数 |
| unreceivedOrders | Integer | 待收货订单数 |
| completedOrders | Integer | 已完成订单数 |
| cancelledOrders | Integer | 已取消订单数 |
| totalAmount | BigDecimal | 订单总金额 |

---

## 🔧 定时任务说明

### 订单超时自动取消

系统包含一个定时任务，用于自动取消超时未支付的订单：

- **执行频率**: 每10分钟执行一次
- **超时时间**: 订单创建后30分钟未支付自动取消
- **功能**: 
  - 扫描所有待支付状态的订单
  - 取消超过30分钟未支付的订单
  - 自动恢复库存
  - 记录取消时间

**配置位置**: `OrderTimeoutService.java`

---

## 📊 数据库表结构概览

### 核心数据表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| user | 用户表 | id, openid, nickname, avatar_url, gender, phone, status |
| product | 商品表 | id, name, price, quantity, sales, image_url, description, category_id |
| product_sku | 商品SKU表 | id, product_id, sku_name, price, stock, spec_values |
| category | 商品分类表 | id, name, icon_url, parent_id, sort_order, status |
| banner | 轮播图表 | id, title, image_url, link_type, link_value, sort_order, status |
| cart_item | 购物车项表 | id, user_id, product_id, sku_id, quantity, selected |
| orders | 订单表 | id, order_no, user_id, total_amount, status, receiver_info |
| order_item | 订单项表 | id, order_id, product_id, product_name, price, quantity |
| user_address | 用户地址表 | id, user_id, receiver_name, receiver_phone, province, city, district, address |

---

## ⚠️ 代码层面问题分析

### 已识别的问题

#### 1. 安全性问题

**问题**: JWT密钥硬编码在配置文件中
- **位置**: `application.yaml` 第46行
- **风险**: 生产环境可能泄露密钥
- **建议**: 通过环境变量 `JWT_SECRET` 设置，并使用强随机密钥

**问题**: CORS配置过于宽松
- **位置**: `WebConfig.java`
- **风险**: `allowedOrigins("*")` 允许所有域名访问
- **建议**: 生产环境应限制为特定域名

#### 2. 性能问题

**问题**: N+1查询问题
- **位置**: 订单查询、商品查询等关联查询场景
- **影响**: 大量数据库查询，性能低下
- **建议**: 使用JOIN查询或批量加载关联数据

**问题**: 缺少数据库连接池监控
- **建议**: 启用HikariCP监控指标，通过Actuator暴露

#### 3. 事务管理

**现状**: 大部分Service方法已正确使用 `@Transactional`
- ✅ 订单创建、库存扣减使用事务
- ✅ 购物车操作使用事务
- ⚠️ 部分批量操作可能需要优化事务边界

#### 4. 异常处理

**现状**: 全局异常处理完善
- ✅ BusinessException 统一处理
- ✅ 参数校验异常处理
- ✅ 乐观锁异常处理
- ✅ 通用异常兜底处理

#### 5. 数据验证

**现状**: 使用Jakarta Validation进行参数校验
- ✅ ProductDTO、UserDTO、UserAddressDTO 等都有校验注解
- ✅ Service层也有手动校验逻辑
- ⚠️ 部分DTO缺少校验注解，依赖Service层校验

---

## 💼 业务层面分析

### 核心业务流程

#### 1. 用户流程
```
注册/登录 → 浏览商品 → 加入购物车 → 选择地址 → 创建订单 → 支付 → 等待收货 → 确认收货
```

#### 2. 订单状态流转
```
待支付(0) → 已支付(1) → 已发货(2) → 已完成(3)
         ↓
      已取消(4)
```

#### 3. 库存管理策略
- **加购阶段**: 不锁定库存，仅提示性检查
- **下单阶段**: 严格锁定库存，使用数据库原子操作防止超卖
- **取消订单**: 自动恢复库存

### 业务功能模块

| 模块 | 功能 | 状态 |
|------|------|------|
| 用户管理 | 注册、登录、信息管理、地址管理 | ✅ 完成 |
| 商品管理 | CRUD、分类、SKU、搜索、排序 | ✅ 完成 |
| 购物车 | 增删改查、选中、结算 | ✅ 完成 |
| 订单管理 | 创建、支付、发货、收货、取消 | ✅ 完成 |
| 支付管理 | 创建支付、查询状态、退款 | ⚠️ 占位实现 |
| 轮播图 | CRUD、有效期管理 | ✅ 完成 |
| 分类管理 | CRUD、层级结构 | ✅ 完成 |

### 待完善功能

1. **支付集成**: 需要集成微信支付SDK
2. **消息通知**: 订单状态变更通知（短信/推送）
3. **评价系统**: 商品评价、晒图
4. **优惠券**: 优惠券发放、使用
5. **物流追踪**: 对接物流API
6. **数据统计**: 销售报表、用户行为分析

---

## 🎯 API接口汇总

### 接口统计

| 模块 | 接口数量 | 认证要求 |
|------|---------|----------|
| 用户管理 | 14个 | 部分需要 |
| 商品管理 | 13个 | 不需要 |
| 商品分类 | 6个 | 不需要 |
| 轮播图 | 6个 | 不需要 |
| 购物车 | 10个 | **需要** |
| 订单管理 | 11个 | **需要** |
| 支付管理 | 4个 | 部分需要 |
| 地址管理 | 11个 | 部分需要 |
| 会话管理 | 3个 | 部分需要 |
| **总计** | **78个** | - |

### 需要认证的接口

- `/api/cart/**` - 所有购物车接口
- `/api/orders/**` - 所有订单接口
- `/api/sessions/logout` - 登出接口

### 公开接口

- `/api/products/**` - 商品浏览
- `/api/categories/**` - 分类浏览
- `/api/banners/**` - 轮播图
- `/api/users/**` - 用户注册/查询（部分）
- `/api/sessions/login` - 登录
- `/api/sessions/validate` - Token验证

---

如有问题，请联系后端开发团队。

**文档版本**: v5.0  
**最后更新**: 2026-05-15  
**维护者**: 后端开发团队
