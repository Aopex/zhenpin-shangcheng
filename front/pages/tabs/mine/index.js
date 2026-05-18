// pages/tabs/mine/index.js
const api = require('../../../utils/api')

Page({
  data: {
    userInfo: {},
    orderStats: {}
  },

  onShow() {
    this.loadData();
  },

  loadData() {
    const userInfo = api.getUserInfo();
    const orderStats = api.getOrderStats();
    this.setData({ userInfo, orderStats });
  },

  handleLogin() {
    const result = api.mockLogin();
    if (!result.success) {
      wx.showToast({ title: result.message || '登录失败', icon: 'none' });
      return;
    }
    getApp().globalData.userInfo = result.userInfo;
    this.setData({ userInfo: result.userInfo });
    wx.showToast({ title: '登录成功', icon: 'success' });
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
  }
});
