// pages/tabs/shop/index.js
const api = require('../../../utils/api')
const db = require('../../../utils/db')

Page({
  data: {
    activeCategoryId: 1,
    activeCategoryName: '',
    categories: [],
    currentProducts: [],
    // SKU 弹窗
    skuPopupShow: false,
    skuProductDetail: {},
    skuSpecGroups: [],
    skuSelectedId: null,
    skuSelectedText: '',
    skuBuyCount: 1,
    // 抛物线动画
    flyImg: '',
    flyX: 0,
    flyY: 0,
    flyDx: 0,
    flyDy: 0,
    flyAnim: ''
  },

  // 当前弹窗对应的 product_no
  _skuProductNo: '',
  _skuProductImg: '',

  onLoad() {
    this.fetchCategoryData();
  },

  onShow() {
    const targetId = wx.getStorageSync('shopTargetCategoryId');
    if (targetId && this.data.categories.length > 0) {
      wx.removeStorageSync('shopTargetCategoryId');
      this.switchToCategory(targetId);
    }
  },

  switchToCategory(categoryId) {
    const id = Number(categoryId);
    const category = this.data.categories.find(c => c.id === id);
    if (category) {
      this.setData({
        activeCategoryId: category.id,
        activeCategoryName: category.name,
        currentProducts: category.products
      });
    }
  },

  async fetchCategoryData() {
    wx.showLoading({ title: '加载中...', mask: true });
    try {
      const categories = await api.getCategoriesWithProducts();

      const targetId = wx.getStorageSync('shopTargetCategoryId');
      const initCategory = targetId
        ? (categories.find(c => c.id === Number(targetId)) || categories[0])
        : categories[0];

      if (targetId) {
        wx.removeStorageSync('shopTargetCategoryId');
      }

      if (!initCategory) {
        this.setData({
          categories: [],
          activeCategoryId: 0,
          activeCategoryName: '',
          currentProducts: []
        });
        return;
      }

      this.setData({
        categories: categories,
        activeCategoryId: initCategory.id,
        activeCategoryName: initCategory.name,
        currentProducts: initCategory.products
      });
    } catch (err) {
      wx.showToast({
        title: err.message || '分类加载失败',
        icon: 'none'
      });
    } finally {
      wx.hideLoading();
    }
  },

  switchCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    this.switchToCategory(categoryId);
  },

  toDetail(e) {
    const productNo = e.currentTarget.dataset.no;
    wx.navigateTo({
      url: '/pages/shop/product-detail/index?no=' + productNo,
    });
  },

  addToCart(e) {
    const productNo = e.currentTarget.dataset.no;
    this._skuProductImg = e.currentTarget.dataset.img || '';
    const detail = api.getProductDetail(productNo);
    if (!detail) return;
    if (!detail.skus || detail.skus.length === 0) {
      wx.showToast({ title: '暂无可选规格', icon: 'none' });
      return;
    }

    // 同一商品保留上次选择，不同商品重置
    if (this._skuProductNo !== productNo) {
      this._skuProductNo = productNo;
      const specGroups = this._buildSpecGroups(detail);
      this.setData({
        skuProductDetail: detail,
        skuSpecGroups: specGroups,
        skuSelectedId: null,
        skuSelectedText: '',
        skuBuyCount: 1
      });
    } else {
      this.setData({ skuProductDetail: detail });
    }
    this.setData({ skuPopupShow: true });
  },

  closeSkuPopup() {
    this.setData({ skuPopupShow: false });
  },

  // 构建规格分组
  _buildSpecGroups(detail) {
    return detail.specs.map(spec => ({
      name: spec.name,
      values: spec.values.map(sv => {
        const relatedSkus = detail.skus.filter(sku => {
          const parts = sku.spec_values.split(',');
          return parts.includes(sv.value);
        });
        const totalStock = relatedSkus.reduce((sum, sku) => sum + sku.stock, 0);
        return { id: sv.id, value: sv.value, disabled: totalStock === 0, selected: false };
      })
    }));
  },

  onSkuTagTap(e) {
    const gi = e.currentTarget.dataset.groupIndex;
    const vi = e.currentTarget.dataset.valueIndex;
    const groups = this.data.skuSpecGroups;
    if (groups[gi].values[vi].disabled) return;
    groups[gi].values.forEach((v, i) => { v.selected = (i === vi) ? !v.selected : false; });
    this.setData({ skuSpecGroups: groups });
    this._matchSku();
  },

  _matchSku() {
    const selected = [];
    this.data.skuSpecGroups.forEach(g => {
      const sel = g.values.find(v => v.selected);
      if (sel) selected.push(sel.value);
    });
    if (selected.length === this.data.skuSpecGroups.length) {
      const sku = this.data.skuProductDetail.skus.find(s => {
        const parts = s.spec_values.split(',');
        return selected.every(v => parts.includes(v));
      });
      if (sku) {
        this.setData({ skuSelectedId: sku.id, skuSelectedText: sku.spec_values });
        return;
      }
    }
    this.setData({ skuSelectedId: null, skuSelectedText: '' });
  },

  onSkuCountMinus() {
    if (this.data.skuBuyCount > 1) {
      this.setData({ skuBuyCount: this.data.skuBuyCount - 1 });
    } else {
      wx.showToast({ title: '至少购买1件', icon: 'none' });
    }
  },

  onSkuCountPlus() {
    this.setData({ skuBuyCount: this.data.skuBuyCount + 1 });
  },

  // 抛物线飞入购物车
  _flyToCart() {
    const img = this._skuProductImg;
    if (!img) return;
    const query = wx.createSelectorQuery();
    query.select('.sku-img').boundingClientRect();
    query.exec(res => {
      if (!res[0]) return;
      const from = res[0];
      const sys = wx.getSystemInfoSync();
      const endX = sys.windowWidth / 2;
      const endY = sys.windowHeight - 30;
      this.setData({
        flyImg: img,
        flyX: from.left + from.width / 2 - 20,
        flyY: from.top + from.height / 2 - 20,
        flyDx: endX - (from.left + from.width / 2),
        flyDy: endY - (from.top + from.height / 2),
        flyAnim: ''
      });
      setTimeout(() => { this.setData({ flyAnim: 'fly-parabola' }); }, 30);
    });
  },

  onFlyEnd() {
    this.setData({ flyImg: '', flyAnim: '' });
  },

  confirmAddToCart() {
    const allSelected = this.data.skuSpecGroups.every(g => g.values.some(v => v.selected));
    if (!allSelected) {
      wx.showToast({ title: '请选择完整规格', icon: 'none' });
      return;
    }
    if (!this.data.skuSelectedId) {
      wx.showToast({ title: '暂无可选规格', icon: 'none' });
      return;
    }
    const productRecord = db.product.find(p => p.product_no === this._skuProductNo);
    if (!productRecord) return;
    api.addToCart(productRecord.id, this.data.skuSelectedId, this.data.skuBuyCount);
    wx.showToast({ title: '添加成功', icon: 'success' });
    this._flyToCart();
    this.setData({ skuPopupShow: false });
  }
});
