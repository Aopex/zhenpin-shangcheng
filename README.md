# 臻品商城 zhenpin-shangcheng

一个基于**微信小程序** + **Spring Boot**后端的全栈电商小程序，涵盖商品浏览、购物车、下单支付、订单管理、用户中心等核心电商功能，支持管理员后台管理。

## 目录

- [项目概览](#-项目概览)
- [技术栈](#-技术栈)
- [项目结构](#-项目结构)
- [功能特性](#-功能特性)
- [快速开始](#-快速开始)
- [接口说明](#-接口说明)

## 项目概览

| 端 | 技术 | 说明 |
|---|---|---|
| **小程序前端** | 微信小程序原生框架 | 面向 C 端用户的购物入口 |
| **后端 API** | Spring Boot 3.2.5 / Java 21 | RESTful API 服务，处理业务逻辑和数据持久化 |

## 技术栈

### 后端（backend）

| 技术 | 用途 |
|---|---|
| Spring Boot 3.2.5 | 应用框架 |
| Java 21 | 运行环境 |
| MyBatis 3.0.5 | ORM / 数据持久化 |
| Spring Data JPA | 辅助数据访问 |
| MySQL | 关系型数据库 |
| Redis | 缓存（会话/Token） |
| JWT (jjwt 0.12.6) | 用户认证与鉴权 |
| Lombok | 简化 POJO 开发 |
| Spring Boot Actuator | 应用监控与健康检查 |
| Spring Validation | 请求参数校验 |

### 前端（微信小程序）

| 技术 | 用途 |
|---|---|
| 微信小程序原生框架 | 应用框架 |
| Glass-Easel 组件框架 | 小程序组件渲染引擎 |

## 项目结构

```
.
├── front/                     # 微信小程序前端
│   ├── app.json               # 小程序全局配置
│   ├── app.js                 # 小程序入口
│   ├── pages/
│   │   ├── tabs/              # 底部 TabBar 页面
│   │   │   ├── index/         # 首页（轮播图 + 商品推荐）
│   │   │   ├── shop/          # 分类页（分类 + 商品列表）
│   │   │   ├── cart/          # 购物车
│   │   │   ├── order/         # 订单列表
│   │   │   └── mine/          # 个人中心
│   │   ├── shop/              # 商城子页面
│   │   │   ├── product-detail/ # 商品详情（SKU 选择）
│   │   │   ├── order-confirm/  # 订单确认页
│   │   │   ├── order-detail/   # 订单详情页
│   │   │   └── search/         # 商品搜索
│   │   └── user/              # 用户子页面
│   │       ├── profile/       # 个人资料编辑
│   │       ├── address/       # 收货地址管理
│   │       └── about/         # 关于我们
│   └── utils/
│       ├── api.js             # API 服务层（接口封装、字段适配）
│       ├── request.js         # HTTP 请求封装
│       ├── db.js              # 本地存储工具
│       └── util.js            # 通用工具函数
│
├── backend/                   # Spring Boot 后端 API
│   ├── pom.xml
│   └── src/main/java/com/miniprogram/backend/
│       ├── common/            # 公共模块
│       │   ├── ApiResponse.java        # 统一响应封装
│       │   ├── PageResponse.java       # 分页响应封装
│       │   ├── BusinessException.java  # 业务异常
│       │   ├── GlobalExceptionHandler  # 全局异常处理
│       │   ├── UserContext.java        # 用户上下文（ThreadLocal）
│       │   ├── OrderStatus.java        # 订单状态枚举
│       │   ├── RequireAdmin.java       # 管理员权限注解
│       │   └── Permission.java         # 权限注解
│       ├── config/            # 配置层
│       │   ├── AuthInterceptor.java    # JWT 认证拦截器
│       │   ├── JwtUtil.java            # JWT 工具类
│       │   ├── WebConfig.java          # Web 配置（CORS 等）
│       │   ├── RedisConfig.java        # Redis 配置
│       │   └── MyBatisConfig.java      # MyBatis 配置
│       └── domain/
│           ├── Controller/    # 控制器层（REST API）
│           ├── Service/       # 业务逻辑层
│           ├── DAO/           # 数据访问层
│           ├── Mapper/        # MyBatis Mapper 接口
│           └── Entity/        # 实体 & DTO
│
```

## 功能特性

### C 端用户（小程序）

| 模块 | 功能 |
|---|---|
| **首页** | 轮播图展示、商品推荐、下拉刷新、触底加载更多 |
| **分类** | 左侧分类导航、右侧商品列表、按分类筛选商品 |
| **搜索** | 关键词搜索商品、搜索结果分页 |
| **商品详情** | 商品图文详情、SKU 规格选择（颜色/尺码等）、加购/立即购买 |
| **购物车** | 添加/删除商品、调整数量、全选/单选、合计金额计算、批量删除、清空购物车 |
| **订单** | 创建订单（购物车结算 & 直接购买）、订单列表（按状态筛选）、订单详情、取消订单、支付、提醒发货、确认收货、删除订单 |
| **用户中心** | 微信登录 / Mock 登录、个人信息查看与编辑、收货地址增删改查、默认地址设置、订单统计展示 |
| **退款** | 申请退款（部分实现） |

### 管理端

| 模块 | 功能 |
|---|---|
| **权限控制** | 基于 JWT + `@RequireAdmin` 注解的管理员鉴权 |
| **用户管理** | 用户列表分页查询、批量插入、批量状态更新、活跃用户查询 |
| **商品管理** | 商品 CRUD、SKU 管理、分类筛选、商品图片管理 |
| **分类管理** | 分类 CRUD、父子分类层级 |
| **轮播图管理** | 轮播图 CRUD、启用/禁用状态 |
| **订单管理** | 订单列表、发货、退款审核等管理操作 |
| **文件上传** | 图片上传（商品图/轮播图）、图片删除 |

## 快速开始

### 环境要求

| 工具 | 版本 |
|---|---|
| JDK | 21+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| 微信开发者工具 | 最新稳定版 |

### 1. 后端启动

```bash
# 进入后端目录
cd backend

# 创建数据库并导入表结构（SQL 文件请根据实际放置位置查找）

# 修改配置文件
# src/main/resources/application.yml
# 配置 MySQL 连接、Redis 连接、JWT 密钥等

# 编译并启动
mvn spring-boot:run
```

后端默认启动在 `http://localhost:8080`。

### 2. 小程序前端

1. 打开 **微信开发者工具**
2. 导入项目，选择 `front/` 目录
3. 填入你的小程序 AppID（当前配置：`wx2b17628bf3c86c3f`）
4. 在 `front/utils/request.js` 中确认 `BASE_URL` 指向后端地址
5. 编译运行即可预览


## 接口说明

所有 API 以 `/api` 为前缀，统一返回格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

> 🔒 = 需要管理员权限（`@RequireAdmin`）　　🔓 = 需要用户登录（JWT Token）　　🌐 = 公开访问

### 认证方式

需要认证的接口在请求头携带 JWT Token：

```
Authorization: Bearer <token>
```

---

### 微信认证 `/api/auth`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `POST` | `/wx-login` | 🌐 | 微信一键登录，传入 `code` + 可选用户信息，返回 JWT Token |

### 会话管理 `/api/sessions`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `POST` | `/login` | 🌐 | 登录（传入 `userId`、`sessionKey`），返回 JWT Token 和用户信息 |
| `POST` | `/logout` | 🔓 | 登出，Token 加入黑名单并删除 Redis 会话 |
| `GET` | `/validate` | 🌐 | 验证 Token 是否有效，参数 `?token=xxx` |

### 用户 `/api/users`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `GET` | `/me` | 🔓 | 获取当前登录用户信息 |
| `PUT` | `/me` | 🔓 | 更新当前用户资料（昵称/头像/性别/手机号） |
| `POST` | `/register` | 🌐 | 用户注册（需提供 `openid`） |
| `GET` | `/` | 🔒 | 获取所有用户（分页），参数 `?page=&pageSize=` |
| `GET` | `/{id}` | 🔒 | 根据 ID 获取用户 |
| `GET` | `/openid/{openid}` | 🔒 | 根据 OpenID 获取用户 |
| `PUT` | `/{id}` | 🔒 | 更新用户信息 |
| `DELETE` | `/{id}` | 🔒 | 删除用户 |
| `GET` | `/status/{status}` | 🔒 | 根据状态获取用户（分页） |
| `POST` | `/batch` | 🔒 | 批量插入用户 |
| `POST` | `/batch/status` | 🔒 | 批量更新用户状态，参数 `?status=` |
| `GET` | `/active` | 🔒 | 查询活跃用户，参数 `?days=7` |

### 商品 `/api/products`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `GET` | `/` | 🌐 | 获取商品列表（分页），参数 `?page=&pageSize=` |
| `GET` | `/{id}` | 🌐 | 根据 ID 获取商品详情 |
| `GET` | `/no/{productNo}/detail` | 🌐 | 根据商品编号获取商品详情 |
| `GET` | `/search` | 🌐 | 综合搜索，参数 `?keyword=&categoryId=&minPrice=&maxPrice=&inStockOnly=&sortType=&page=&pageSize=` |
| `GET` | `/search/{name}` | 🌐 | 按名称搜索商品（分页） |
| `GET` | `/quantity/{quantity}` | 🌐 | 按库存数量查询商品（分页） |
| `GET` | `/category/{categoryId}` | 🌐 | 按分类查询商品（分页），参数 `?page=&pageSize=` |
| `GET` | `/{productId}/skus` | 🌐 | 获取商品的所有 SKU |
| `GET` | `/skus/{skuId}` | 🌐 | 获取指定 SKU 详情 |
| `POST` | `/` | 🔒 | 创建商品 |
| `PUT` | `/{id}` | 🔒 | 更新商品 |
| `DELETE` | `/{id}` | 🔒 | 删除商品 |

### 商品分类 `/api/categories`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `GET` | `/` | 🌐 | 获取所有分类列表 |
| `GET` | `/sub/{parentId}` | 🌐 | 根据父分类 ID 获取子分类列表 |
| `GET` | `/{id}` | 🌐 | 根据 ID 获取分类详情 |
| `POST` | `/` | 🔒 | 创建分类 |
| `PUT` | `/{id}` | 🔒 | 更新分类 |
| `DELETE` | `/{id}` | 🔒 | 删除分类 |

### 购物车 `/api/cart`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `POST` | `/` | 🔓 | 添加商品到购物车（需 `skuId`、`quantity`） |
| `GET` | `/` | 🔓 | 获取当前用户购物车列表 |
| `PUT` | `/quantity` | 🔓 | 更新购物车商品数量 |
| `PUT` | `/selected` | 🔓 | 切换单个商品选中状态 |
| `PUT` | `/selected/all` | 🔓 | 全选/取消全选 |
| `GET` | `/selected` | 🔓 | 获取已选中的购物车商品 |
| `GET` | `/total` | 🔓 | 计算选中商品总金额 |
| `DELETE` | `/{cartItemId}` | 🔓 | 删除单个购物车项 |
| `DELETE` | `/clear` | 🔓 | 清空购物车 |
| `POST` | `/batch-delete` | 🔓 | 批量删除购物车项 |

### 订单 `/api/orders`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `POST` | `/` | 🔓 | 创建订单（购物车结算），需 `cartItemIds`、`addressId`、`remark` |
| `GET` | `/{orderId}` | 🔓 | 获取订单详情 |
| `GET` | `/no/{orderNo}` | 🔓 | 根据订单号获取订单 |
| `GET` | `/my-orders` | 🔓 | 当前用户订单列表（分页），参数 `?status=&page=&size=` |
| `GET` | `/my-orders/unpaid` | 🔓 | 待付款订单列表 |
| `GET` | `/my-orders/unshipped` | 🔓 | 待发货订单列表（已支付） |
| `GET` | `/my-orders/unreceived` | 🔓 | 待收货订单列表（已发货） |
| `GET` | `/my-orders/stats` | 🔓 | 当前用户订单统计（各状态数量） |
| `PUT` | `/{orderId}/pay` | 🔓 | 支付订单 |
| `PUT` | `/{orderId}/cancel` | 🔓 | 取消订单 |
| `PUT` | `/{orderId}/confirm` | 🔓 | 确认收货 |
| `PUT` | `/{orderId}/remind-ship` | 🔓 | 提醒发货（演示模式自动触发发货） |
| `DELETE` | `/{orderId}` | 🔓 | 删除订单（软删除） |
| `PUT` | `/{orderId}/deliver` | 🔒 | 发货（管理员操作） |

### 收货地址 `/api/addresses`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `GET` | `/{id}` | 🔓 | 根据 ID 获取地址详情 |
| `POST` | `/` | 🔓 | 创建新地址 |
| `PUT` | `/{id}` | 🔓 | 更新地址 |
| `DELETE` | `/{id}` | 🔓 | 删除地址（逻辑删除） |
| `GET` | `/my-addresses` | 🔓 | 获取当前用户地址列表（分页），参数 `?page=&pageSize=` |
| `GET` | `/my-addresses/list` | 🔓 | 获取当前用户全部地址（默认地址优先） |
| `GET` | `/my-addresses/default` | 🔓 | 获取当前用户默认地址 |
| `POST` | `/{id}/set-default` | 🔓 | 设置默认地址 |
| `GET` | `/my-addresses/count` | 🔓 | 统计当前用户地址数量 |
| `POST` | `/batch/set-default` | 🔒 | 批量设置默认地址 |
| `POST` | `/batch/delete` | 🔒 | 批量逻辑删除地址 |
| `GET` | `/stats/by-user` | 🔒 | 统计每个用户的地址数量 |

### 退款 `/api/refunds`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `POST` | `/` | 🔓 | 申请退款，参数 `?orderId=&reason=`，返回退款单号 |
| `GET` | `/my-refunds` | 🔓 | 获取当前用户退款列表（分页），参数 `?page=&pageSize=` |
| `GET` | `/order/{orderId}` | 🔓 | 根据订单 ID 获取退款记录 |
| `GET` | `/{refundId}` | 🔓 | 获取退款详情 |
| `POST` | `/{refundId}/approve` | 🔒 | 批准退款 |
| `POST` | `/{refundId}/reject` | 🔒 | 拒绝退款，参数 `?reason=` |

### 支付 `/api/payment`

> 当前为占位实现（Stub），后续集成微信支付。所有接口在未实现时返回 `501`。

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `POST` | `/create` | 🔓 | 创建支付订单，需 `orderNo`、`totalAmount`、`openid` |
| `GET` | `/status/{orderNo}` | 🔓 | 查询支付状态 |
| `POST` | `/refund` | 🔓 | 申请退款，需 `orderNo`、`refundAmount` |
| `POST` | `/wechat/callback` | 🌐 | 微信支付异步回调 |

### 轮播图 `/api/banners`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `GET` | `/` | 🌐 | 获取所有轮播图 |
| `GET` | `/active` | 🌐 | 获取当前有效的轮播图 |
| `GET` | `/{id}` | 🌐 | 根据 ID 获取轮播图详情 |
| `POST` | `/` | 🔒 | 创建轮播图 |
| `PUT` | `/{id}` | 🔒 | 更新轮播图 |
| `DELETE` | `/{id}` | 🔒 | 删除轮播图 |

### 文件上传 `/api/files`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| `POST` | `/upload` | 🔒 | 上传图片，参数 `file`（MultipartFile）+ `type`（`products` / `banners`） |
| `DELETE` | `/delete` | 🔒 | 删除图片，参数 `?imageUrl=/pics/products/xxx.jpg` |

---

**项目名称**：臻品商城（zhenpin-shangcheng）
**开发背景**：2025 年春季学期 专业实习项目
