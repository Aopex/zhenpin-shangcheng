Page({
  data: {
    profile: {
      name: '实习同学',
      level: '校园会员',
      desc: '欢迎来到个人中心，这里可以管理购物相关内容。'
    },
    serviceList: [
      { name: '我的订单', path: '/pages/order/index' },
      { name: '购物车', path: '/pages/cart/index' },
      { name: '收货地址', path: '' },
      { name: '优惠券', path: '' }
    ]
  },
  goToPage(e) {
    const { path, name } = e.currentTarget.dataset
    if (!path) {
      wx.showToast({
        title: `${name} 功能待接入`,
        icon: 'none'
      })
      return
    }

    wx.navigateTo({
      url: path
    })
  }
})