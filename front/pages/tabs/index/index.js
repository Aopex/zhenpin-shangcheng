// pages/tabs/index/index.js
const api = require('../../../utils/api')

Page({
  data: {
    banners: [],
    categories: [],
    products: [],
    searchKeyword: '',
    productPage: 1,
    productPageSize: 20,
    productTotal: 0,
    hasMoreProducts: true,
    loadingMoreProducts: false
  },

  onLoad() {
    this.fetchHomeData();
  },

  async fetchHomeData() {
    wx.showLoading({ title: '加载中...', mask: true });
    try {
      const data = await api.getHomeData();
      // 金刚区只展示 1-7 和 10（其他综合），过滤掉 8、9
      data.categories = data.categories.filter(c => c.id !== 8 && c.id !== 9);
      this.setData({
        ...data,
        loadingMoreProducts: false
      });
    } catch (err) {
      wx.showToast({
        title: err.message || '首页加载失败',
        icon: 'none'
      });
    } finally {
      wx.hideLoading();
    }
  },

  async onReachBottom() {
    if (this.data.loadingMoreProducts || !this.data.hasMoreProducts) return;

    this.setData({ loadingMoreProducts: true });
    try {
      const nextPage = Number(this.data.productPage || 1) + 1;
      const result = await api.getHomeProducts({
        page: nextPage,
        pageSize: this.data.productPageSize || 20
      });

      this.setData({
        products: this.data.products.concat(result.products),
        productPage: result.page,
        productPageSize: result.pageSize,
        productTotal: result.total,
        hasMoreProducts: result.hasNext,
        loadingMoreProducts: false
      });
    } catch (err) {
      wx.showToast({
        title: err.message || '加载更多失败',
        icon: 'none'
      });
      this.setData({ loadingMoreProducts: false });
    }
  },

  toDetail(e) {
    const productNo = e.currentTarget.dataset.no;
    wx.navigateTo({
      url: '/pages/shop/product-detail/index?no=' + productNo,
    });
  },

  toBannerProduct(e) {
    const productNo = e.currentTarget.dataset.no;
    if (!productNo) return;
    wx.navigateTo({
      url: '/pages/shop/product-detail/index?no=' + productNo,
    });
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  clearSearch() {
    this.setData({ searchKeyword: '' });
  },

  submitSearch() {
    const keyword = this.data.searchKeyword.trim();
    wx.navigateTo({
      url: '/pages/shop/search/index?keyword=' + encodeURIComponent(keyword)
    });
  },

  // 金刚区跳转到商城对应分类
  switchToShop(e) {
    const categoryId = e.currentTarget.dataset.id;
    wx.setStorageSync('shopTargetCategoryId', categoryId);
    wx.switchTab({
      url: '/pages/tabs/shop/index',
    });
  }
});
