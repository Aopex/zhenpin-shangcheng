// pages/shop/product-detail/index.js
const api = require('../../../utils/api')
const db = require('../../../utils/db')

Page({
  data: {
    productDetail: {},
    currentBannerIndex: 0,
    isSkuShow: false,
    selectedSkuText: '',
    displaySkuText: '',
    selectedSkuId: null,
    buyCount: 1,
    cartCount: 0,
    specGroups: [],
    specNames: '',
    // 抛物线动画
    flyImg: '',
    flyX: 0,
    flyY: 0,
    flyDx: 0,
    flyDy: 0,
    flyAnim: ''
  },

  // SKU 弹窗的触发意图：'cart' | 'buy'
  skuAction: '',

  onLoad(options) {
    const no = options.no;
    this.productNo = no;
    this.fetchProductDetail(no);
    this._loadCartCount();
  },

  onShow() {
    this._loadCartCount();
  },

  _loadCartCount() {
    const items = api.getCartItems();
    const total = items.reduce((sum, item) => sum + item.count, 0);
    this.setData({ cartCount: total });
  },

  async fetchProductDetail(no) {
    wx.showLoading({ title: '加载中...', mask: true });

    try {
      const detail = await api.getProductDetail(no);
      if (!detail) {
        wx.showToast({ title: '商品不存在', icon: 'none' });
        return;
      }

      // 构建规格分组（含每个值的库存状态）
      const specGroups = detail.specs.map(spec => ({
        name: spec.name,
        values: spec.values.map(sv => {
          const relatedSkus = detail.skus.filter(sku => {
            const parts = sku.spec_values.split(',');
            return parts.includes(sv.value);
          });
          const totalStock = relatedSkus.reduce((sum, sku) => sum + sku.stock, 0);
          return {
            id: sv.id,
            value: sv.value,
            disabled: totalStock === 0,
            selected: false
          };
        })
      }));

      this.setData({
        productDetail: detail,
        specGroups: specGroups,
        specNames: detail.specs.map(s => s.name).join('/')
      });
    } catch (err) {
      wx.showToast({
        title: err.message || '商品加载失败',
        icon: 'none'
      });
    } finally {
      wx.hideLoading();
    }
  },

  onSwiperChange(e) {
    this.setData({ currentBannerIndex: e.detail.current });
  },

  previewImage(e) {
    const current = e.currentTarget.dataset.src;
    wx.previewImage({
      current,
      urls: this.data.productDetail.banners
    });
  },

  // ----------- SKU 弹窗 -----------
  showSkuPopup(action) {
    this.skuAction = action || 'cart';
    this.setData({ isSkuShow: true });
  },

  hideSkuPopup() {
    this.setData({ isSkuShow: false });
  },

  selectSpecValue(e) {
    const groupIndex = e.currentTarget.dataset.groupIndex;
    const valueIndex = e.currentTarget.dataset.valueIndex;
    const specGroups = this.data.specGroups;

    if (specGroups[groupIndex].values[valueIndex].disabled) return;

    specGroups[groupIndex].values.forEach((v, i) => {
      v.selected = (i === valueIndex) ? !v.selected : false;
    });

    this.setData({ specGroups });
    this._matchSku();
  },

  _matchSku() {
    const selectedValues = [];
    this.data.specGroups.forEach(group => {
      const sel = group.values.find(v => v.selected);
      if (sel) selectedValues.push(sel.value);
    });

    if (selectedValues.length === this.data.specGroups.length) {
      const detail = this.data.productDetail;
      const sku = detail.skus.find(s => {
        const parts = s.spec_values.split(',');
        return selectedValues.every(v => parts.includes(v));
      });
      if (sku) {
        this.setData({
          selectedSkuId: sku.id,
          selectedSkuText: sku.spec_values.replace(/,/g, '，')
        });
      }
    } else {
      this.setData({ selectedSkuId: null, selectedSkuText: '' });
    }
  },

  addBuyCount() {
    this.setData({ buyCount: this.data.buyCount + 1 });
  },

  minusBuyCount() {
    if (this.data.buyCount > 1) {
      this.setData({ buyCount: this.data.buyCount - 1 });
    } else {
      wx.showToast({ title: '至少购买1件', icon: 'none' });
    }
  },

  confirmSku() {
    // 检查是否所有规格组都已选择
    const allSelected = this.data.specGroups.every(group =>
      group.values.some(v => v.selected)
    );
    if (!allSelected) {
      wx.showToast({ title: '请选择完整规格', icon: 'none' });
      return;
    }

    // 匹配 SKU
    if (!this.data.selectedSkuId) {
      wx.showToast({ title: '暂无可选规格', icon: 'none' });
      return;
    }

    // 执行对应操作
    const productRecord = db.product.find(p => p.product_no === this.productNo);
    if (!productRecord) return;

    if (this.skuAction === 'buy') {
      wx.navigateTo({
        url: `/pages/shop/order-confirm/index?type=direct&productNo=${this.productNo}&skuId=${this.data.selectedSkuId}&quantity=${this.data.buyCount}`
      });
    } else {
      // 加入购物车
      api.addToCart(productRecord.id, this.data.selectedSkuId, this.data.buyCount);
      this._loadCartCount();
      wx.showToast({ title: '添加成功', icon: 'success' });
      this._flyToCart();
    }

    // 更新底部卡片显示，保留选择状态供下次打开复用
    this.setData({
      displaySkuText: this.data.selectedSkuText + '，' + this.data.buyCount + '件',
      isSkuShow: false
    });
  },

  // ----------- 抛物线飞入购物车 -----------
  _flyToCart() {
    const query = wx.createSelectorQuery();
    query.select('.sku-img').boundingClientRect();
    query.select('.cart-icon-area').boundingClientRect();
    query.exec(res => {
      if (!res[0] || !res[1]) return;
      const from = res[0];
      const to = res[1];
      this.setData({
        flyImg: this.data.productDetail.banners[0],
        flyX: from.left + from.width / 2 - 20,
        flyY: from.top + from.height / 2 - 20,
        flyDx: to.left + to.width / 2 - (from.left + from.width / 2),
        flyDy: to.top + to.height / 2 - (from.top + from.height / 2),
        flyAnim: ''
      });
      setTimeout(() => { this.setData({ flyAnim: 'fly-parabola' }); }, 30);
    });
  },

  onFlyEnd() {
    this.setData({ flyImg: '', flyAnim: '' });
  },

  // ----------- 底部操作栏 -----------
  goHome() {
    wx.switchTab({ url: '/pages/tabs/index/index' });
  },

  goCart() {
    wx.switchTab({ url: '/pages/tabs/cart/index' });
  },

  addToCart() {
    this.showSkuPopup('cart');
  },

  buyNow() {
    this.showSkuPopup('buy');
  }
});
