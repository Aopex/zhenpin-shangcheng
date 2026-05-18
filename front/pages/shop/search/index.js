const api = require('../../../utils/api')

const DEFAULT_FILTER = {
  categoryId: 0,
  minPrice: '',
  maxPrice: '',
  inStockOnly: false
}

Page({
  data: {
    keyword: '',
    inputKeyword: '',
    sortType: 'comprehensive',
    sortLabel: '综合',
    products: [],
    categories: [],
    filterSummary: [],
    total: 0,
    resultCountText: '检索中',
    loading: true,
    filterShow: false,
    filterActive: false,
    categoryId: 0,
    minPrice: '',
    maxPrice: '',
    inStockOnly: false,
    draftCategoryId: 0,
    draftMinPrice: '',
    draftMaxPrice: '',
    draftInStockOnly: false,
    skeletons: [1, 2, 3, 4]
  },

  onLoad(options = {}) {
    const keyword = decodeURIComponent(options.keyword || '')
    this.setData({
      keyword,
      inputKeyword: keyword
    })
    this.search()
  },

  onKeywordInput(e) {
    this.setData({ inputKeyword: e.detail.value })
  },

  clearKeyword() {
    this.setData({
      inputKeyword: '',
      keyword: ''
    })
    this.search()
  },

  submitSearch() {
    this.setData({ keyword: this.data.inputKeyword.trim() })
    this.search()
  },

  switchSort(e) {
    const sortType = e.currentTarget.dataset.type
    const labelMap = {
      comprehensive: '综合',
      latest: '最新',
      priceAsc: '价格升序',
      priceDesc: '价格降序',
      sales: '销量'
    }
    this.setData({
      sortType,
      sortLabel: labelMap[sortType] || '综合'
    })
    this.search()
  },

  togglePriceSort() {
    const sortType = this.data.sortType === 'priceAsc' ? 'priceDesc' : 'priceAsc'
    this.setData({
      sortType,
      sortLabel: sortType === 'priceAsc' ? '价格升序' : '价格降序'
    })
    this.search()
  },

  openFilter() {
    this.setData({
      filterShow: true,
      filterActive: false,
      draftCategoryId: this.data.categoryId,
      draftMinPrice: this.data.minPrice,
      draftMaxPrice: this.data.maxPrice,
      draftInStockOnly: this.data.inStockOnly
    })
    setTimeout(() => {
      this.setData({ filterActive: true })
    }, 30)
  },

  closeFilter() {
    this.setData({ filterActive: false })
    setTimeout(() => {
      this.setData({ filterShow: false })
    }, 260)
  },

  stopTap() {},

  selectDraftCategory(e) {
    this.setData({ draftCategoryId: Number(e.currentTarget.dataset.id || 0) })
  },

  onMinPriceInput(e) {
    this.setData({ draftMinPrice: e.detail.value })
  },

  onMaxPriceInput(e) {
    this.setData({ draftMaxPrice: e.detail.value })
  },

  toggleDraftStock() {
    this.setData({ draftInStockOnly: !this.data.draftInStockOnly })
  },

  resetDraftFilter() {
    this.setData({
      draftCategoryId: DEFAULT_FILTER.categoryId,
      draftMinPrice: DEFAULT_FILTER.minPrice,
      draftMaxPrice: DEFAULT_FILTER.maxPrice,
      draftInStockOnly: DEFAULT_FILTER.inStockOnly
    })
  },

  confirmFilter() {
    this.setData({
      categoryId: this.data.draftCategoryId,
      minPrice: this.data.draftMinPrice,
      maxPrice: this.data.draftMaxPrice,
      inStockOnly: this.data.draftInStockOnly
    })
    this.closeFilter()
    this.search()
  },

  clearFilter(e) {
    const type = e.currentTarget.dataset.type
    const data = {}
    if (type === 'category') data.categoryId = DEFAULT_FILTER.categoryId
    if (type === 'minPrice') data.minPrice = DEFAULT_FILTER.minPrice
    if (type === 'maxPrice') data.maxPrice = DEFAULT_FILTER.maxPrice
    if (type === 'stock') data.inStockOnly = DEFAULT_FILTER.inStockOnly
    this.setData(data)
    this.search()
  },

  clearAllFilters() {
    this.setData({
      categoryId: DEFAULT_FILTER.categoryId,
      minPrice: DEFAULT_FILTER.minPrice,
      maxPrice: DEFAULT_FILTER.maxPrice,
      inStockOnly: DEFAULT_FILTER.inStockOnly,
      draftCategoryId: DEFAULT_FILTER.categoryId,
      draftMinPrice: DEFAULT_FILTER.minPrice,
      draftMaxPrice: DEFAULT_FILTER.maxPrice,
      draftInStockOnly: DEFAULT_FILTER.inStockOnly
    })
    this.search()
  },

  search() {
    this.setData({ loading: true })
    setTimeout(() => {
      const result = api.searchProducts({
        keyword: this.data.keyword,
        sortType: this.data.sortType,
        categoryId: this.data.categoryId,
        minPrice: this.data.minPrice,
        maxPrice: this.data.maxPrice,
        inStockOnly: this.data.inStockOnly
      })
      this.setData({
        products: result.products,
        categories: result.categories,
        filterSummary: result.filterSummary,
        total: result.total,
        resultCountText: `${result.total} 件`,
        loading: false
      })
    }, 220)
  },

  toDetail(e) {
    const productNo = e.currentTarget.dataset.no
    wx.navigateTo({
      url: '/pages/shop/product-detail/index?no=' + productNo
    })
  }
})
