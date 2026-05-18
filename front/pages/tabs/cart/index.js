// pages/tabs/cart/index.js
const api = require('../../../utils/api')

Page({
  data: {
    cartItems: [],
    isAllChecked: false,
    totalPrice: '0.00',
    totalCount: 0,
    touchStartX: 0,
    touchStartY: 0,
    touchStartOffset: 0,
    touchIndex: -1,
    rpxRatio: 2,
    deleteWidth: 132
  },

  onLoad() {
    const systemInfo = wx.getSystemInfoSync();
    this.setData({
      rpxRatio: 750 / systemInfo.windowWidth
    });
  },

  onShow() {
    this.loadCart();
  },

  async loadCart() {
    try {
      const cartItems = (await api.getCartItems()).map(item => ({
        ...item,
        swipeX: 0,
        isSwiping: false
      }));
      this.setData({ cartItems });
      this.calculateTotal();
    } catch (err) {
      this.setData({
        cartItems: [],
        isAllChecked: false,
        totalPrice: '0.00',
        totalCount: 0
      });
      if (!err.handled) wx.showToast({ title: err.message || '购物车加载失败', icon: 'none' });
    }
  },

  onCartItemTouchStart(e) {
    const index = e.currentTarget.dataset.index;
    const touch = e.touches[0];
    const currentItem = this.data.cartItems[index] || {};
    const cartItems = this.data.cartItems.map((item, itemIndex) => ({
      ...item,
      swipeX: itemIndex === index ? (item.swipeX || 0) : 0,
      isSwiping: itemIndex === index
    }));

    this.setData({
      cartItems,
      touchStartX: touch.clientX,
      touchStartY: touch.clientY,
      touchStartOffset: currentItem.swipeX || 0,
      touchIndex: index
    });
  },

  onCartItemTouchMove(e) {
    const index = e.currentTarget.dataset.index;
    if (index !== this.data.touchIndex) return;

    const touch = e.touches[0];
    const moveX = touch.clientX - this.data.touchStartX;
    const moveY = touch.clientY - this.data.touchStartY;
    if (Math.abs(moveY) > Math.abs(moveX) && this.data.touchStartOffset === 0) return;

    const moveRpx = moveX * this.data.rpxRatio;
    const nextSwipeX = Math.max(-this.data.deleteWidth, Math.min(0, this.data.touchStartOffset + moveRpx));

    this.setData({
      [`cartItems[${index}].swipeX`]: nextSwipeX
    });
  },

  onCartItemTouchEnd(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.cartItems[index];
    if (!item) return;

    const shouldOpen = Math.abs(item.swipeX || 0) > this.data.deleteWidth / 2;
    this.setData({
      [`cartItems[${index}].swipeX`]: shouldOpen ? -this.data.deleteWidth : 0,
      [`cartItems[${index}].isSwiping`]: false,
      touchIndex: -1
    });
  },

  calculateTotal() {
    let items = this.data.cartItems;
    let isAllChecked = true;
    let total = 0;
    let count = 0;

    if (items.length === 0) {
      isAllChecked = false;
    } else {
      for (let i = 0; i < items.length; i++) {
        if (items[i].checked) {
          total += parseFloat(items[i].price) * items[i].count;
          count += items[i].count;
        } else {
          isAllChecked = false;
        }
      }
    }

    this.setData({
      isAllChecked: isAllChecked,
      totalPrice: total.toFixed(2),
      totalCount: count
    });
  },

  async toggleCheck(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.cartItems[index];
    if (!item) return;

    try {
      await api.toggleCartItem(item.id, !item.checked);
      await this.loadCart();
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '更新失败', icon: 'none' });
    }
  },

  async toggleAllCheck() {
    const isAllChecked = !this.data.isAllChecked;
    try {
      await api.toggleAllCartItems(isAllChecked);
      await this.loadCart();
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '更新失败', icon: 'none' });
    }
  },

  async changeCount(e) {
    const index = e.currentTarget.dataset.index;
    const type = e.currentTarget.dataset.type;
    const item = this.data.cartItems[index];
    if (!item) return;

    try {
      const result = await api.updateCartItemCount(item.id, type, item.count);
      if (!result.success && result.message) {
        wx.showToast({ title: result.message, icon: 'none' });
        return;
      }
      await this.loadCart();
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '数量更新失败', icon: 'none' });
    }
  },

  async deleteCartItem(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.cartItems[index];
    if (!item) return;

    try {
      await api.removeCartItem(item.id);
      wx.showToast({ title: '已删除', icon: 'success' });
      await this.loadCart();
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '删除失败', icon: 'none' });
    }
  },

  goShopping() {
    wx.switchTab({ url: '/pages/tabs/index/index' });
  },

  checkout() {
    if (this.data.totalCount === 0) {
      wx.showToast({ title: '请先选择商品哦', icon: 'none' });
      return;
    }

    wx.navigateTo({
      url: '/pages/shop/order-confirm/index?type=cart'
    });
  },

  toDetail(e) {
    const productNo = e.currentTarget.dataset.no;
    wx.navigateTo({
      url: '/pages/shop/product-detail/index?no=' + productNo,
    });
  }
});
