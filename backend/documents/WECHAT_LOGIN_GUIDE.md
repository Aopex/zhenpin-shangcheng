# 微信登录功能使用说明

## 📱 功能概述

已实现完整的微信小程序登录流程，包括：
1. ✅ 使用 code 换取 openid 和 session_key（通过微信服务器验证）
2. ✅ 自动注册/更新用户信息
3. ✅ 生成 JWT Token
4. ✅ 一键登录（注册+登录合并）

---

## 🔧 配置步骤

### 1. 获取微信 AppID 和 AppSecret

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入"开发" → "开发管理" → "开发设置"
3. 复制 `AppID` 和 `AppSecret`

### 2. 配置环境变量

在项目根目录创建 `.env` 文件（基于 `.env.example`）：

```bash
# 微信配置
WECHAT_APPID=YOUR_APPID_HERE       # 替换为你的 AppID
WECHAT_SECRET=YOUR_SECRET_HERE     # 替换为你的 AppSecret
```

或者直接在 `application.yaml` 中配置（仅用于开发环境）：

```yaml
wechat:
  appid: YOUR_APPID_HERE
  secret: YOUR_SECRET_HERE
```

---

## 🚀 API 接口

### 微信一键登录

**接口地址**: `POST /api/auth/wx-login`

**请求体**:
```json
{
  "code": "071ABC123...",           // 必填：wx.login() 获取的 code
  "nickname": "张三",                // 可选：用户昵称
  "avatarUrl": "https://...",       // 可选：头像URL
  "gender": 1                       // 可选：性别 0-未知, 1-男, 2-女
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "openid": "oXXXX...",
    "nickname": "张三",
    "avatarUrl": "https://...",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "sessionKey": "xxxxx"
  }
}
```

**错误响应**:
```json
{
  "code": 400,
  "message": "Code cannot be empty",
  "data": null
}
```

---

## 💻 前端调用示例

### 小程序端代码

```javascript
// pages/login/login.js
Page({
  data: {
    loading: false
  },

  // 微信一键登录
  async handleWxLogin() {
    this.setData({ loading: true });
    
    try {
      // 1. 调用 wx.login 获取 code
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({
          success: resolve,
          fail: reject
        });
      });
      
      if (!loginRes.code) {
        throw new Error('获取登录凭证失败');
      }
      
      // 2. 获取用户信息（可选）
      const userInfo = await new Promise((resolve, reject) => {
        wx.getUserProfile({
          desc: '用于完善用户资料',
          success: resolve,
          fail: reject
        });
      });
      
      // 3. 调用后端登录接口
      const res = await new Promise((resolve, reject) => {
        wx.request({
          url: 'http://your-backend/api/auth/wx-login',
          method: 'POST',
          header: {
            'Content-Type': 'application/json'
          },
          data: {
            code: loginRes.code,
            nickname: userInfo.userInfo.nickName,
            avatarUrl: userInfo.userInfo.avatarUrl,
            gender: userInfo.userInfo.gender
          },
          success: resolve,
          fail: reject
        });
      });
      
      if (res.data.code === 200) {
        // 4. 保存 token 和用户信息
        const { token, userId, nickname, avatarUrl } = res.data.data;
        
        wx.setStorageSync('token', token);
        wx.setStorageSync('userId', userId);
        wx.setStorageSync('userInfo', {
          nickname,
          avatarUrl
        });
        
        // 5. 跳转到首页
        wx.showToast({ title: '登录成功', icon: 'success' });
        wx.switchTab({ url: '/pages/index/index' });
      } else {
        throw new Error(res.data.message || '登录失败');
      }
      
    } catch (error) {
      console.error('登录失败:', error);
      wx.showToast({ 
        title: error.message || '登录失败', 
        icon: 'none' 
      });
    } finally {
      this.setData({ loading: false });
    }
  }
});
```

### WXML 模板

```html
<!-- pages/login/login.wxml -->
<view class="login-container">
  <button 
    class="login-btn" 
    bindtap="handleWxLogin"
    disabled="{{loading}}"
  >
    {{loading ? '登录中...' : '微信一键登录'}}
  </button>
</view>
```

---

## 🔄 登录流程说明

```
┌──────────┐         ┌──────────┐         ┌──────────┐         ┌──────────┐
│ 小程序   │         │ 后端     │         │ 微信API  │         │ 数据库   │
└────┬─────┘         └────┬─────┘         └────┬─────┘         └────┬─────┘
     │                    │                    │                    │
     │ 1. wx.login()      │                    │                    │
     │───────────────────>│                    │                    │
     │ 返回 code          │                    │                    │
     │                    │                    │                    │
     │ 2. POST /wx-login  │                    │                    │
     │ {code, userInfo}   │                    │                    │
     │───────────────────>│                    │                    │
     │                    │                    │                    │
     │                    │ 3. code2Session    │                    │
     │                    │───────────────────>│                    │
     │                    │                    │                    │
     │                    │ 4. openid +        │                    │
     │                    │    session_key     │                    │
     │                    │<───────────────────│                    │
     │                    │                    │                    │
     │                    │ 5. 查询/创建用户    │                    │
     │                    │───────────────────────────────────────>│
     │                    │                    │                    │
     │                    │ 6. 用户信息         │                    │
     │                    │<───────────────────────────────────────│
     │                    │                    │                    │
     │                    │ 7. 生成 JWT Token  │                    │
     │                    │                    │                    │
     │ 8. 返回 token +    │                    │                    │
     │    用户信息        │                    │                    │
     │<───────────────────│                    │                    │
     │                    │                    │                    │
```

---

## ⚠️ 注意事项

### 安全性
1. **不要在前端存储 AppSecret** - 所有微信 API 调用都应在后端进行
2. **使用 HTTPS** - 生产环境必须使用 HTTPS 传输
3. **Token 安全存储** - 小程序端使用 `wx.setStorageSync` 安全存储

### 开发环境
1. **配置正确的 AppID** - 确保使用的小程序 AppID 与微信公众平台一致
2. **配置服务器域名** - 在微信公众平台配置后端服务器域名
3. **本地调试** - 可以使用微信开发者工具的"不校验合法域名"选项

### 常见问题

**Q: 提示 "WeChat configuration is missing"**
A: 检查是否正确配置了 `WECHAT_APPID` 和 `WECHAT_SECRET` 环境变量

**Q: 提示 "invalid code"**
A: code 只能使用一次且有效期为5分钟，确保及时调用

**Q: 提示 "appid and openid not match"**
A: 确保使用的 AppID 与获取 code 的小程序一致

---

## 📊 两种登录方式对比

| 特性 | 旧方式 (/api/users/register) | 新方式 (/api/auth/wx-login) |
|------|------------------------------|------------------------------|
| **安全性** | ⚠️ 低（openid可伪造） | ✅ 高（微信验证） |
| **用户体验** | 需要两步操作 | 一键完成 |
| **适用场景** | 学习/测试 | 生产环境 |
| **是否需要配置** | 不需要 | 需要配置 AppID/Secret |

**建议**：
- 开发阶段可以继续使用旧的注册接口进行测试
- 生产环境务必使用新的微信登录接口

---

## 🎯 后续优化建议

1. **添加 Refresh Token 机制** - 实现无感刷新 Token
2. **增加登录频率限制** - 防止恶意攻击
3. **记录登录日志** - 便于安全审计
4. **支持多端登录** - 区分不同设备类型
5. **添加短信验证码** - 绑定手机号时二次验证
