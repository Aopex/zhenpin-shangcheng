Page({
  data: {
    orderList: [
      { id: 'NO20260511001', status: '待发货', amount: 251, date: '2026-05-11' },
      { id: 'NO20260508002', status: '已完成', amount: 129, date: '2026-05-08' },
      { id: 'NO20260501003', status: '已取消', amount: 59, date: '2026-05-01' }
    ]
  },
  goToShop() {
    wx.navigateTo({
      url: '/pages/shop/index'
    })
  }
})