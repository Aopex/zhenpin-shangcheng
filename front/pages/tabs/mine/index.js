// pages/tabs/mine/index.js
const api = require('../../../utils/api')
const { clearAuthStorage } = require('../../../utils/request')

Page({
  data: {
    userInfo: {},
    orderStats: { unpaid: 0, unshipped: 0, unreceived: 0, finished: 0 },
    loginLoading: false
  },

  onShow() {
    this.loadData();
  },

  async loadData() {
    const localUserInfo = api.getUserInfo();
    this.setData({ userInfo: localUserInfo });

    if (!localUserInfo.isLoggedIn) {
      this.setData({ orderStats: { unpaid: 0, unshipped: 0, unreceived: 0, finished: 0 } });
      return;
    }

    try {
      const [userInfo, orderStats] = await Promise.all([
        api.fetchUserInfo(),
        api.getOrderStats()
      ]);
      this.setData({ userInfo, orderStats });
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '个人中心加载失败', icon: 'none' });
      this.setData({ userInfo: api.getUserInfo() });
    }
  },

  handleLogin() {
    if (this.data.loginLoading) return;

    this.setData({ loginLoading: true });
    wx.login({
      success: async (res) => {
        if (!res.code) {
          this.setData({ loginLoading: false });
          wx.showToast({ title: res.errMsg || '获取登录凭证失败', icon: 'none' });
          return;
        }

        try {
          const userInfo = await api.wxLogin({ code: res.code });
          const orderStats = await api.getOrderStats();
          getApp().globalData.userInfo = userInfo;
          this.setData({ userInfo, orderStats });
          wx.showToast({ title: '登录成功', icon: 'success' });
        } catch (err) {
          wx.showToast({ title: err.message || '登录失败', icon: 'none' });
        } finally {
          this.setData({ loginLoading: false });
        }
      },
      fail: (err) => {
        this.setData({ loginLoading: false });
        wx.showToast({ title: err.errMsg || '获取登录凭证失败', icon: 'none' });
      }
    });
  },

  toOrderList(e) {
    const status = e.currentTarget.dataset.status;
    getApp().globalData = getApp().globalData || {};
    getApp().globalData.defaultOrderStatus = status;
    wx.switchTab({
      url: '/pages/tabs/order/index'
    });
  },

  toAddressManage() {
    wx.navigateTo({
      url: '/pages/user/address/index'
    });
  },

  toProfileEdit() {
    wx.navigateTo({
      url: '/pages/user/profile/index'
    });
  },

  toAbout() {
    wx.navigateTo({
      url: '/pages/user/about/index'
    });
  },

  handleLogout() {
    clearAuthStorage();
    getApp().globalData = getApp().globalData || {};
    getApp().globalData.userInfo = null;
    this.setData({
      userInfo: api.getUserInfo(),
      orderStats: { unpaid: 0, unshipped: 0, unreceived: 0, finished: 0 }
    });
    wx.showToast({ title: '已退出登录', icon: 'none' });
  }
});
