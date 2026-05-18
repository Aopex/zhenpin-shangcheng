// pages/tabs/order/index.js
const api = require('../../../utils/api')

Page({
  data: {
    tabs: [
      { id: 0, name: '全部', status: 'ALL' },
      { id: 1, name: '待付款', status: 'UNPAID' },
      { id: 2, name: '待发货', status: 'UNSHIPPED' },
      { id: 3, name: '待收货', status: 'UNRECEIVED' },
      { id: 4, name: '已完成', status: 'FINISHED' }
    ],
    currentStatus: 'ALL',
    displayOrders: []
  },

  onLoad(options) {
    if (options.status) {
      this.setData({ currentStatus: options.status });
    }
  },

  onShow() {
    const app = getApp();
    const defaultStatus = app.globalData && app.globalData.defaultOrderStatus;
    if (defaultStatus) {
      this.setData({ currentStatus: defaultStatus });
      app.globalData.defaultOrderStatus = '';
    }
    this.fetchOrders();
  },

  switchTab(e) {
    const status = e.currentTarget.dataset.status;
    this.setData({ currentStatus: status });
    this.fetchOrders();
  },

  fetchOrders() {
    wx.showLoading({ title: '加载中...', mask: true });
    setTimeout(() => {
      const orders = api.getOrders(this.data.currentStatus);
      this.setData({ displayOrders: orders });
      wx.hideLoading();
    }, 300);
  },

  toDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/shop/order-detail/index?id=' + id
    });
  },

  cancelOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '提示',
      content: '确定要取消该订单吗？',
      success: (res) => {
        if (res.confirm) {
          api.cancelOrder(id);
          this.fetchOrders();
          wx.showToast({ title: '已取消', icon: 'success' });
        }
      }
    });
  },

  remindShip(e) {
    const id = e.currentTarget.dataset.id;
    const result = api.remindShipOrder(id);
    wx.showToast({
      title: result.message || '提醒失败',
      icon: result.shipped ? 'success' : 'none',
      duration: 1600
    });
    if (result.success) this.fetchOrders();
  },

  payOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showToast({ title: '调起支付...', icon: 'loading' });
    setTimeout(() => {
      api.payOrder(id);
      this.fetchOrders();
      wx.showToast({ title: '支付成功', icon: 'success' });
    }, 1000);
  },

  confirmReceive(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认收货',
      content: '确认已经收到商品吗？',
      success: (res) => {
        if (!res.confirm) return;
        const result = api.confirmReceiveOrder(id);
        wx.showToast({
          title: result.success ? '已确认收货' : (result.message || '操作失败'),
          icon: result.success ? 'success' : 'none'
        });
        if (result.success) this.fetchOrders();
      }
    });
  },

  deleteOrder(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '删除订单',
      content: '删除后该订单将不再显示，但不会影响订单商品快照。',
      confirmText: '删除',
      confirmColor: '#CB4042',
      success: (res) => {
        if (!res.confirm) return;
        const result = api.deleteOrder(id);
        wx.showToast({
          title: result.success ? '已删除' : (result.message || '删除失败'),
          icon: result.success ? 'success' : 'none'
        });
        if (result.success) this.fetchOrders();
      }
    });
  }
});
