Page({
  data: {
    bannerTitle: '商城',
    bannerDesc: '首页、购物、购物车、订单、个人中心已串联完成。',
    quickActions: [
      { name: '购物中心', desc: '浏览精选商品', path: '/pages/shop/index' },
      { name: '购物车', desc: '查看待结算商品', path: '/pages/cart/index' },
      { name: '我的订单', desc: '查看订单状态', path: '/pages/order/index' },
      { name: '个人中心', desc: '管理账号与服务', path: '/pages/mine/index' }
    ],
    featuredGoods: [
      { id: 1, name: '无线蓝牙耳机', price: 199, tag: '热卖' },
      { id: 2, name: '便携保温杯', price: 59, tag: '新品' },
      { id: 3, name: '简约双肩包', price: 129, tag: '推荐' }
    ]
  },
  goToPage(e) {
    const { path } = e.currentTarget.dataset
    wx.navigateTo({
      url: path
    })
  }
})
