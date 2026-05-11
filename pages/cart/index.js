Page({
  data: {
    cartList: [
      { id: 1, name: '无线蓝牙耳机', price: 199, count: 1 },
      { id: 2, name: '活页笔记本', price: 26, count: 2 }
    ]
  },
  onLoad() {
    this.updateSummary()
  },
  updateSummary() {
    const totalCount = this.data.cartList.reduce((sum, item) => sum + item.count, 0)
    const totalPrice = this.data.cartList.reduce((sum, item) => sum + item.count * item.price, 0)

    this.setData({
      totalCount,
      totalPrice
    })
  },
  goToOrder() {
    wx.navigateTo({
      url: '/pages/order/index'
    })
  }
})