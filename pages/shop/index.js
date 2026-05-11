Page({
  data: {
    categories: ['全部', '数码', '家居', '学习'],
    currentCategory: '全部',
    goodsList: [
      { id: 1, name: '无线蓝牙耳机', category: '数码', price: 199, desc: '通勤学习都适用' },
      { id: 2, name: '护眼台灯', category: '家居', price: 89, desc: '三档亮度可调节' },
      { id: 3, name: '活页笔记本', category: '学习', price: 26, desc: '支持替芯与分隔页' },
      { id: 4, name: '双肩电脑包', category: '数码', price: 139, desc: '轻便防泼水设计' }
    ]
  },
  chooseCategory(e) {
    this.setData({
      currentCategory: e.currentTarget.dataset.name
    })
  },
  goToCart() {
    wx.navigateTo({
      url: '/pages/cart/index'
    })
  },
  goToOrder() {
    wx.navigateTo({
      url: '/pages/order/index'
    })
  },
  addToCart(e) {
    const { name } = e.currentTarget.dataset
    wx.showToast({
      title: `${name} 已加入购物车`,
      icon: 'success'
    })
  }
})