// app.js
App({
  onLaunch() {
    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    const userInfo = wx.getStorageSync('userInfo')
    const token = wx.getStorageSync('token')
    if (userInfo && token) {
      this.globalData.userInfo = {
        ...userInfo,
        token,
        isLoggedIn: true
      }
    }
  },
  globalData: {
    userInfo: null
  }
})
