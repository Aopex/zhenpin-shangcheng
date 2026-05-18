const api = require('../../../utils/api')

Page({
  data: {
    sourceType: 'cart',
    productNo: '',
    skuId: 0,
    quantity: 1,
    address: null,
    selectedAddressId: 0,
    goods: [],
    totalCount: 0,
    goodsAmountText: '0.00',
    freightAmountText: '0.00',
    discountAmountText: '0.00',
    totalAmountText: '0.00',
    remark: '',
    orderStatus: 'UNPAID',
    orderStatusText: '待付款'
  },

  onLoad(options) {
    this.setData({
      sourceType: options.type || 'cart',
      productNo: options.productNo || '',
      skuId: Number(options.skuId || 0),
      quantity: Number(options.quantity || 1)
    })
    this.loadConfirmData()
  },

  async onShow() {
    const app = getApp()
    const selectedAddressId = app.globalData && app.globalData.selectedAddressId
    if (!selectedAddressId || Number(selectedAddressId) === Number(this.data.selectedAddressId)) return

    try {
      const address = await api.getAddressById(selectedAddressId)
      if (address) {
        this.setData({
          address,
          selectedAddressId: address.id
        })
      }
    } catch (err) {
      wx.showToast({ title: err.message || '地址加载失败', icon: 'none' })
    }
  },

  async loadConfirmData() {
    wx.showLoading({ title: '加载中...', mask: true })
    let result = null
    try {
      result = await api.getOrderConfirmData({
        type: this.data.sourceType,
        productNo: this.data.productNo,
        skuId: this.data.skuId,
        quantity: this.data.quantity
      })
    } catch (err) {
      wx.hideLoading()
      if (!err.handled) wx.showToast({ title: err.message || '订单信息异常', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 600)
      return
    }

    if (!result.success) {
      wx.hideLoading()
      wx.showToast({ title: result.message || '订单信息异常', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 600)
      return
    }

    const data = result.data
    const app = getApp()
    const selectedAddressId = app.globalData && app.globalData.selectedAddressId
    let selectedAddress = null
    if (selectedAddressId) {
      try {
        selectedAddress = await api.getAddressById(selectedAddressId)
      } catch (err) {
        wx.showToast({ title: err.message || '地址加载失败', icon: 'none' })
      }
    }
    wx.hideLoading()

    const statusTextMap = {
      UNPAID: '待付款',
      UNSHIPPED: '待发货',
      UNRECEIVED: '待收货',
      PAID: '待发货',
      SHIPPED: '待收货',
      FINISHED: '已完成',
      CANCELLED: '已取消'
    }
    this.setData({
      address: selectedAddress || data.address,
      selectedAddressId: selectedAddress ? selectedAddress.id : (data.address ? data.address.id : 0),
      goods: data.goods,
      totalCount: data.total_count,
      goodsAmountText: data.goodsAmountText,
      freightAmountText: data.freightAmountText,
      discountAmountText: data.discountAmountText,
      totalAmountText: data.totalAmountText,
      orderStatus: data.status,
      orderStatusText: statusTextMap[data.status] || data.status
    })
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },

  chooseAddress() {
    wx.navigateTo({
      url: '/pages/user/address/index?mode=select'
    })
  },

  async submitOrder() {
    if (!this.data.address) {
      wx.showToast({ title: '请先选择收货地址', icon: 'none' })
      return
    }

    const submit = async () => {
      if (this.data.sourceType === 'direct') {
        return api.createDirectOrder({
          productNo: this.data.productNo,
          skuId: this.data.skuId,
          quantity: this.data.quantity,
          remark: this.data.remark,
          addressId: this.data.address.id
        })
      }
      return api.createOrder({
        remark: this.data.remark,
        addressId: this.data.address.id,
        cartItemIds: this.data.goods.map(item => item.cart_item_id).filter(Boolean)
      })
    }

    wx.showLoading({ title: '提交中...', mask: true })
    try {
      const result = await submit()
      wx.hideLoading()
      if (!result.success) {
        wx.showToast({ title: result.message || '提交失败', icon: 'none' })
        return
      }

      wx.showToast({ title: '订单已生成', icon: 'success' })
      setTimeout(() => {
        getApp().globalData = getApp().globalData || {}
        getApp().globalData.defaultOrderStatus = 'UNPAID'
        wx.switchTab({ url: '/pages/tabs/order/index' })
      }, 700)
    } catch (err) {
      wx.hideLoading()
      if (!err.handled) wx.showToast({ title: err.message || '提交失败', icon: 'none' })
    }
  }
})
