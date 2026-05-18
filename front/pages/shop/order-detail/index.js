const api = require('../../../utils/api')

Page({
  data: {
    orderId: 0,
    order: null,
    timeRows: [],
    hasBottomActions: false
  },

  onLoad(options) {
    this.setData({ orderId: Number(options.id || 0) })
    this.loadOrderDetail()
  },

  onShow() {
    if (this.data.orderId) this.loadOrderDetail()
  },

  async loadOrderDetail() {
    let result = null
    try {
      result = await api.getOrderDetail(this.data.orderId)
      if (!result.success) {
        wx.showToast({ title: result.message || '订单不存在', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 600)
        return
      }
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '订单不存在', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 600)
      return
    }

    const order = result.data
    this.setData({
      order,
      timeRows: this.buildTimeRows(order),
      hasBottomActions: order.status !== 'CANCELLED'
    })
  },

  buildTimeRows(order) {
    const rows = [{ label: '下单时间', value: order.createdAt || '暂无' }]
    if (['UNSHIPPED', 'UNRECEIVED', 'FINISHED'].includes(order.status) && order.payTime) {
      rows.push({ label: '支付时间', value: order.payTime })
    }
    if (['UNRECEIVED', 'FINISHED'].includes(order.status) && order.shipTime) {
      rows.push({ label: '发货时间', value: order.shipTime })
    }
    if (order.status === 'FINISHED') {
      rows.push({ label: '完成时间', value: order.finishTime || order.receiveTime || '暂无' })
    }
    if (order.status === 'CANCELLED') {
      rows.push({ label: '取消时间', value: order.cancelTime || '暂无' })
    }
    return rows
  },

  cancelOrder() {
    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await api.cancelOrder(this.data.orderId)
          wx.showToast({ title: '已取消', icon: 'success' })
          await this.loadOrderDetail()
        } catch (err) {
          if (!err.handled) wx.showToast({ title: err.message || '取消失败', icon: 'none' })
        }
      }
    })
  },

  payOrder() {
    wx.showToast({ title: '调起支付...', icon: 'loading' })
    setTimeout(async () => {
      try {
        await api.payOrder(this.data.orderId)
        wx.showToast({ title: '支付成功', icon: 'success' })
        await this.loadOrderDetail()
      } catch (err) {
        if (!err.handled) wx.showToast({ title: err.message || '支付失败', icon: 'none' })
      }
    }, 800)
  },

  async remindShip() {
    const result = await api.remindShipOrder(this.data.orderId)
    wx.showToast({
      title: result.message || '提醒失败',
      icon: result.shipped ? 'success' : 'none',
      duration: 1600
    })
    if (result.success) this.loadOrderDetail()
  },

  confirmReceive() {
    wx.showModal({
      title: '确认收货',
      content: '确认已经收到商品吗？',
      success: async (res) => {
        if (!res.confirm) return
        try {
          const result = await api.confirmReceiveOrder(this.data.orderId)
          wx.showToast({ title: result.success ? '已确认收货' : (result.message || '操作失败'), icon: result.success ? 'success' : 'none' })
          await this.loadOrderDetail()
        } catch (err) {
          if (!err.handled) wx.showToast({ title: err.message || '操作失败', icon: 'none' })
        }
      }
    })
  },

  deleteOrder() {
    wx.showModal({
      title: '删除订单',
      content: '删除后该订单将不再显示，但不会影响订单商品快照。',
      confirmText: '删除',
      confirmColor: '#CB4042',
      success: async (res) => {
        if (!res.confirm) return
        try {
          const result = await api.deleteOrder(this.data.orderId)
          wx.showToast({
            title: result.success ? '已删除' : (result.message || '删除失败'),
            icon: result.success ? 'success' : 'none'
          })
          if (result.success) {
            setTimeout(() => wx.navigateBack(), 500)
          }
        } catch (err) {
          if (!err.handled) wx.showToast({ title: err.message || '删除失败', icon: 'none' })
        }
      }
    })
  }
})
