/**
 * utils/api.js
 * API 服务层 — 页面调用这里，不直接读接口细节
 * 负责真实接口调用、字段适配和少量前端组合逻辑
 */

const { BASE_URL, request } = require('./request')

function _formatPrice(price) {
  const value = Number(price || 0)
  return value.toFixed(2)
}

function _toBoolean(value) {
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value !== 0
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    return normalized === 'true' || normalized === '1'
  }
  return !!value
}

function _normalizeImageUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return `${BASE_URL}${url.startsWith('/') ? url : `/${url}`}`
}

function _normalizeMiniProgramAssetUrl(url) {
  if (!url) return ''
  if (url.startsWith('/assets/')) return url
  return _normalizeImageUrl(url)
}

function _toHomeProduct(product) {
  return {
    id: product.id,
    no: product.productNo || product.product_no,
    title: product.title,
    price: _formatPrice(product.price),
    imageUrl: _normalizeImageUrl(product.imageUrl || product.image_url || product.main_image),
    stock: product.stock || 0,
    sales: product.sales || 0,
    categoryId: product.categoryId || product.category_id,
    originalPrice: product.originalPrice ? _formatPrice(product.originalPrice) : '',
    status: product.status,
    sortOrder: product.sortOrder || product.sort_order || 0,
    createdAt: product.createdAt || product.created_at || '',
    updatedAt: product.updatedAt || product.updated_at || ''
  }
}

function _toCategory(category = {}) {
  return {
    id: category.id,
    name: category.name,
    icon: _normalizeMiniProgramAssetUrl(category.iconUrl || category.icon_url || ''),
    sortOrder: category.sortOrder || category.sort_order || 0,
    parentId: category.parentId || category.parent_id || 0,
    status: category.status
  }
}

function _getPageItems(pageData) {
  if (Array.isArray(pageData)) return pageData
  return (pageData && pageData.data) || []
}

// ==================== 首页 ====================

/**
 * 获取首页数据
 * @returns {Promise<{ banners: Array, categories: Array, products: Array }>}
 */
async function getHomeData() {
  const [bannersData, categoriesData, productsPage] = await Promise.all([
    request({ url: '/api/banners/active' }),
    request({ url: '/api/categories' }),
    request({ url: '/api/products', data: { page: 1, pageSize: 20 } })
  ])

  const banners = (bannersData || []).map(b => ({
    id: b.id,
    title: b.title,
    imgUrl: _normalizeImageUrl(b.imageUrl || b.image_url),
    linkType: b.linkType || b.link_type,
    linkValue: b.linkValue || b.link_value
  }))

  const categories = (categoriesData || []).map(_toCategory)

  const products = ((productsPage && productsPage.data) || []).map(_toHomeProduct)

  return {
    banners,
    categories,
    products,
    productPage: productsPage && productsPage.page ? productsPage.page : 1,
    productPageSize: productsPage && productsPage.pageSize ? productsPage.pageSize : 20,
    productTotal: productsPage && productsPage.total !== undefined ? productsPage.total : products.length,
    hasMoreProducts: !!(productsPage && productsPage.hasNext)
  }
}

/**
 * 分页获取首页商品
 * @param {{ page?: number, pageSize?: number }} params
 * @returns {Promise<{ products: Array, page: number, pageSize: number, total: number, hasNext: boolean }>}
 */
async function getHomeProducts(params = {}) {
  const page = Number(params.page || 1)
  const pageSize = Number(params.pageSize || 20)
  const productsPage = await request({
    url: '/api/products',
    data: { page, pageSize }
  })

  return {
    products: _getPageItems(productsPage).map(_toHomeProduct),
    page: productsPage && productsPage.page ? productsPage.page : page,
    pageSize: productsPage && productsPage.pageSize ? productsPage.pageSize : pageSize,
    total: productsPage && productsPage.total !== undefined ? productsPage.total : 0,
    hasNext: !!(productsPage && productsPage.hasNext)
  }
}

// ==================== 分类页 ====================

/**
 * 获取分类列表（含每个分类下的商品）
 * @returns {Promise<Array<{ id, name, products: Array }>>}
 */
async function getCategoriesWithProducts() {
  const categoriesData = await request({ url: '/api/categories' })
  const categories = (categoriesData || []).map(c => ({
    id: c.id,
    name: c.name,
    products: []
  }))

  const productsList = await Promise.all(
    categories.map(category => getProductsByCategory(category.id))
  )

  return categories.map((category, index) => ({
    ...category,
    products: productsList[index] || []
  }))
}

/**
 * 根据分类ID获取商品列表
 * @param {number} categoryId
 * @returns {Promise<Array>}
 */
async function getProductsByCategory(categoryId) {
  const result = await request({
    url: `/api/products/category/${categoryId}`,
    data: { page: 1, pageSize: 50 }
  })
  return ((result && result.data) || []).map(_toHomeProduct)
}

// ==================== 搜索页 ====================

function _toSearchProduct(p, categoryMap) {
  const product = _toHomeProduct(p)
  return {
    id: product.id,
    no: product.no,
    title: product.title,
    price: product.price,
    imageUrl: product.imageUrl,
    stock: product.stock,
    sales: product.sales,
    categoryId: product.categoryId,
    categoryName: categoryMap[product.categoryId] || '',
    createdAt: product.createdAt
  }
}

/**
 * 商品搜索（由后端统一处理关键词、分类、价格、库存、排序和分页）
 * @param {{ keyword?: string, sortType?: string, categoryId?: number|string, minPrice?: number|string, maxPrice?: number|string, inStockOnly?: boolean }} params
 * @returns {Promise<{ products: Array, categories: Array, filterSummary: Array, total: number }>}
 */
async function searchProducts(params = {}) {
  const keyword = (params.keyword || '').trim()
  const sortType = params.sortType || 'comprehensive'
  const categoryId = Number(params.categoryId || 0)
  const minPrice = params.minPrice === '' || params.minPrice === undefined ? null : Number(params.minPrice)
  const maxPrice = params.maxPrice === '' || params.maxPrice === undefined ? null : Number(params.maxPrice)
  const inStockOnly = !!params.inStockOnly

  const searchData = {
    inStockOnly,
    sortType,
    page: params.page || 1,
    pageSize: params.pageSize || 1000
  }
  if (keyword) searchData.keyword = keyword
  if (categoryId) searchData.categoryId = categoryId
  if (minPrice !== null && !Number.isNaN(minPrice)) searchData.minPrice = minPrice
  if (maxPrice !== null && !Number.isNaN(maxPrice)) searchData.maxPrice = maxPrice

  const [categoriesData, productsPage] = await Promise.all([
    request({ url: '/api/categories' }),
    request({ url: '/api/products/search', data: searchData })
  ])

  const categories = (categoriesData || [])
    .map(_toCategory)
    .filter(c => c.status === undefined || c.status === 1)
    .sort((a, b) => b.sortOrder - a.sortOrder)
    .map(c => ({ id: c.id, name: c.name }))

  const categoryMap = categories.reduce((map, item) => {
    map[item.id] = item.name
    return map
  }, {})

  const products = _getPageItems(productsPage).map(_toHomeProduct)

  const filterSummary = []
  const category = categoryId ? categories.find(c => c.id === categoryId) : null
  if (category) filterSummary.push({ type: 'category', label: category.name })
  if (minPrice !== null && !Number.isNaN(minPrice)) filterSummary.push({ type: 'minPrice', label: `¥${minPrice}以上` })
  if (maxPrice !== null && !Number.isNaN(maxPrice)) filterSummary.push({ type: 'maxPrice', label: `¥${maxPrice}以内` })
  if (inStockOnly) filterSummary.push({ type: 'stock', label: '仅看有货' })

  return {
    products: products.map(p => _toSearchProduct(p, categoryMap)),
    categories,
    filterSummary,
    total: productsPage && productsPage.total !== undefined ? productsPage.total : products.length
  }
}

// ==================== 商品详情 ====================

/**
 * 根据商品编号获取完整详情（含图片、规格、SKU）
 * @param {string} productNo
 * @returns {Promise<object|null>}
 */
async function getProductDetail(productNo) {
  const detail = await request({ url: `/api/products/no/${encodeURIComponent(productNo)}/detail` })
  if (!detail) return null

  return {
    id: detail.id,
    no: detail.no,
    title: detail.title,
    price: _formatPrice(detail.price),
    originalPrice: detail.originalPrice ? _formatPrice(detail.originalPrice) : '',
    sales: detail.sales || 0,
    stock: detail.stock || 0,
    banners: (detail.banners || []).map(_normalizeImageUrl),
    detailImages: (detail.detailImages || []).map(_normalizeImageUrl),
    skus: (detail.skus || []).map(sku => ({
      id: sku.id,
      product_id: sku.productId || sku.product_id,
      sku_no: sku.skuNo || sku.sku_no,
      spec_values: sku.specValues || sku.spec_values,
      price: sku.price || detail.price,
      stock: sku.stock || 0,
      image_url: _normalizeImageUrl(sku.imageUrl || sku.image_url)
    })),
    specs: detail.specs || []
  }
}

/**
 * 根据SKU ID获取SKU信息
 * @param {number} skuId
 * @returns {Promise<object|null>}
 */
async function getSkuById(skuId) {
  const sku = await request({ url: `/api/products/skus/${skuId}` })
  if (!sku) return null
  return {
    id: sku.id,
    product_id: sku.productId || sku.product_id,
    sku_no: sku.skuNo || sku.sku_no,
    spec_values: sku.specValues || sku.spec_values,
    price: sku.price || 0,
    stock: sku.stock || 0,
    image_url: _normalizeImageUrl(sku.imageUrl || sku.image_url)
  }
}

// ==================== 购物车 ====================

function _toCartItem(item = {}) {
  const productPrice = item.productPrice !== undefined ? item.productPrice : item.product_price
  const productImage = item.productImage || item.product_image
  const productName = item.productName || item.product_name
  const productNo = item.productNo || item.product_no
  const specValues = item.specValues || item.spec_values || ''

  return {
    id: item.id,
    product_id: item.productId || item.product_id,
    sku_id: item.skuId || item.sku_id,
    no: productNo,
    title: productName || item.title || '',
    price: _formatPrice(productPrice || item.price),
    imageUrl: _normalizeImageUrl(productImage || item.imageUrl || item.image_url),
    spec_values: specValues,
    count: item.quantity || item.count || 0,
    checked: item.selected !== undefined ? _toBoolean(item.selected) : _toBoolean(item.checked)
  }
}

/**
 * 获取当前用户的购物车列表（已关联商品和SKU信息）
 * @returns {Promise<Array>}
 */
async function getCartItems() {
  const items = await request({ url: '/api/cart' })
  return (items || []).map(_toCartItem)
}

/**
 * 添加商品到购物车
 * @param {number} productId
 * @param {number} skuId
 * @param {number} quantity
 * @returns {Promise<{ success: boolean, message: string, data?: object }>}
 */
async function addToCart(productId, skuId, quantity) {
  const item = await request({
    url: '/api/cart',
    method: 'POST',
    data: {
      productId,
      skuId,
      quantity: Number(quantity) || 1
    }
  })
  return { success: true, message: '添加成功', data: _toCartItem(item) }
}

/**
 * 切换购物车某项的选中状态
 * @param {number} cartItemId
 * @param {boolean} checked
 */
async function toggleCartItem(cartItemId, checked) {
  await request({
    url: `/api/cart/${cartItemId}/selected`,
    method: 'PUT',
    data: { selected: !!checked }
  })
}

/**
 * 全选 / 取消全选
 * @param {boolean} checked
 */
async function toggleAllCartItems(checked) {
  await request({
    url: '/api/cart/select-all',
    method: 'PUT',
    data: { selected: !!checked }
  })
}

/**
 * 修改购物车商品数量
 * @param {number} cartItemId
 * @param {'add'|'minus'} type
 * @param {number} currentCount
 * @returns {Promise<{ success: boolean, message?: string, count?: number, data?: object }>}
 */
async function updateCartItemCount(cartItemId, type, currentCount) {
  const nextCount = type === 'add' ? Number(currentCount) + 1 : Number(currentCount) - 1
  if (nextCount < 1) {
    return { success: false, message: '受不了了，宝贝不能再少了' }
  }
  const item = await request({
    url: `/api/cart/${cartItemId}/quantity`,
    method: 'PUT',
    data: { quantity: nextCount }
  })
  return { success: true, count: nextCount, data: _toCartItem(item) }
}

/**
 * 从购物车删除某项
 * @param {number} cartItemId
 */
async function removeCartItem(cartItemId) {
  await request({
    url: `/api/cart/${cartItemId}`,
    method: 'DELETE'
  })
  return { success: true }
}

// ==================== 地址 ====================

function _toAddress(addr) {
  if (!addr) return null
  const detail = addr.address || addr.detail || ''
  return {
    id: addr.id,
    user_id: addr.userId || addr.user_id,
    name: addr.receiverName || addr.name || '',
    phone: addr.receiverPhone || addr.phone || '',
    province: addr.province || '',
    city: addr.city || '',
    district: addr.district || '',
    detail,
    is_default: addr.isDefault || addr.is_default ? 1 : 0,
    fullAddress: addr.fullAddress || `${addr.province || ''}${addr.city || ''}${addr.district || ''}${detail}`,
    created_at: addr.createTime || addr.created_at || '',
    updated_at: addr.updateTime || addr.updated_at || ''
  }
}

function _toAddressPayload(payload = {}) {
  return {
    receiverName: (payload.name || '').trim(),
    receiverPhone: (payload.phone || '').trim(),
    province: (payload.province || '').trim(),
    city: (payload.city || '').trim(),
    district: (payload.district || '').trim(),
    address: (payload.detail || '').trim(),
    isDefault: Number(payload.is_default) === 1
  }
}

async function getAddresses() {
  const addresses = await request({ url: '/api/addresses/my-addresses/list' })
  return (addresses || []).map(_toAddress)
}

async function getAddressById(addressId) {
  const address = await request({ url: `/api/addresses/${addressId}` })
  return _toAddress(address)
}

async function getDefaultAddress() {
  const address = await request({ url: '/api/addresses/my-addresses/default' })
  return _toAddress(address)
}

async function createAddress(payload = {}) {
  const address = await request({
    url: '/api/addresses',
    method: 'POST',
    data: _toAddressPayload(payload)
  })
  return { success: true, data: _toAddress(address) }
}

async function updateAddress(addressId, payload = {}) {
  const address = await request({
    url: `/api/addresses/${addressId}`,
    method: 'PUT',
    data: _toAddressPayload(payload)
  })
  return { success: true, data: _toAddress(address) }
}

async function deleteAddress(addressId) {
  await request({
    url: `/api/addresses/${addressId}`,
    method: 'DELETE'
  })
  return { success: true }
}

async function setDefaultAddress(addressId) {
  await request({
    url: `/api/addresses/${addressId}/set-default`,
    method: 'POST'
  })
  return { success: true }
}

function _buildOrderConfirmData(items) {
  const totalCount = items.reduce((sum, item) => sum + item.quantity, 0)
  const goodsAmount = items.reduce((sum, item) => sum + item.subtotal, 0)
  const freightAmount = goodsAmount > 0 && goodsAmount < 99 ? 10 : 0
  const discountAmount = 0
  const totalAmount = goodsAmount + freightAmount - discountAmount

  return {
    goods: items,
    total_count: totalCount,
    goods_amount: goodsAmount,
    goodsAmountText: goodsAmount.toFixed(2),
    freight_amount: freightAmount,
    freightAmountText: freightAmount.toFixed(2),
    discount_amount: discountAmount,
    discountAmountText: discountAmount.toFixed(2),
    total_amount: totalAmount,
    totalAmountText: totalAmount.toFixed(2),
    status: 'UNPAID',
    pay_time: null,
    ship_time: null,
    receive_time: null,
    finish_time: null,
    cancel_time: null
  }
}

function _toOrderPreviewItem(item = {}) {
  const price = Number(item.price || item.productPrice || 0)
  const quantity = Number(item.count || item.quantity || 1)
  const subtotal = price * quantity
  return {
    cart_item_id: item.id || item.cartItemId || item.cart_item_id,
    product_id: item.product_id || item.productId,
    sku_id: item.sku_id || item.skuId,
    product_no: item.no || item.productNo || item.product_no,
    title: item.title || item.productName || '',
    image_url: item.imageUrl || item.image_url || item.productImage || '',
    spec_text: item.spec_values || item.specText || item.specValues || '',
    price,
    priceText: _formatPrice(price),
    quantity,
    subtotal,
    subtotalText: _formatPrice(subtotal)
  }
}

function _toFrontendOrderStatus(status) {
  if (status === 'PAID') return 'UNSHIPPED'
  if (status === 'SHIPPED') return 'UNRECEIVED'
  return status || 'UNPAID'
}

function _toBackendOrderStatus(status) {
  if (!status || status === 'ALL') return ''
  if (status === 'UNSHIPPED') return 'PAID'
  if (status === 'UNRECEIVED') return 'SHIPPED'
  return status
}

function _getOrderStatusText(status) {
  const STATUS_TEXT = {
    UNPAID: '等待买家付款',
    UNSHIPPED: '买家已付款',
    UNRECEIVED: '卖家已发货',
    FINISHED: '交易成功',
    CANCELLED: '已取消'
  }
  return STATUS_TEXT[status] || status || ''
}

function _getOrderStatusShortText(status) {
  const STATUS_SHORT_TEXT = {
    UNPAID: '待付款',
    UNSHIPPED: '待发货',
    UNRECEIVED: '待收货',
    FINISHED: '已完成',
    CANCELLED: '已取消'
  }
  return STATUS_SHORT_TEXT[status] || status || ''
}

function _toOrderGoods(item = {}) {
  return {
    id: item.id,
    productId: item.productId || item.product_id,
    skuId: item.skuId || item.sku_id,
    productNo: item.productNo || item.product_no,
    title: item.title || '',
    imageUrl: _normalizeImageUrl(item.imageUrl || item.image_url),
    specText: item.specText || item.spec_text || '',
    price: _formatPrice(item.price),
    count: item.quantity || item.count || 0,
    subtotal: _formatPrice(item.subtotal),
    createdAt: item.createdAt || item.createTime || item.created_at || ''
  }
}

function _toOrder(order = {}) {
  const status = _toFrontendOrderStatus(order.status)
  const goods = (order.orderItems || order.items || order.goods || []).map(_toOrderGoods)
  const goodsAmount = goods.reduce((sum, item) => sum + Number(item.subtotal || 0), 0)
  const totalAmount = order.actualAmount !== undefined && order.actualAmount !== null
    ? order.actualAmount
    : order.totalAmount
  const freightAmount = order.freightAmount || order.freight_amount || 0
  const discountAmount = order.discountAmount || order.discount_amount || 0
  return {
    id: order.id,
    orderNo: order.orderNo || order.order_no,
    status,
    statusText: _getOrderStatusText(status),
    statusShortText: _getOrderStatusShortText(status),
    totalCount: order.totalCount || order.total_count || goods.reduce((sum, item) => sum + Number(item.count || 0), 0),
    totalAmount: _formatPrice(totalAmount),
    freightAmount: _formatPrice(freightAmount),
    discountAmount: _formatPrice(discountAmount),
    goodsAmount: _formatPrice(goodsAmount || order.totalAmount),
    address: order.receiverName || order.receiverPhone || order.receiverAddress
      ? {
          name: order.receiverName || '',
          phone: order.receiverPhone || '',
          address: order.receiverAddress || ''
        }
      : null,
    remark: order.remark || '',
    createdAt: order.createTime || order.createdAt || order.created_at || '',
    updatedAt: order.updateTime || order.updatedAt || order.updated_at || '',
    payTime: order.paymentTime || order.payTime || order.pay_time || '',
    shipTime: order.deliveryTime || order.shipTime || order.ship_time || '',
    receiveTime: order.completionTime || order.receiveTime || order.receive_time || '',
    finishTime: order.completionTime || order.finishTime || order.finish_time || '',
    cancelTime: order.cancellationTime || order.cancelTime || order.cancel_time || '',
    goods
  }
}

/**
 * 获取订单确认页数据
 * @param {{ type: 'cart'|'direct', productNo?: string, skuId?: number, quantity?: number }} params
 */
async function getOrderConfirmData(params) {
  const type = params && params.type ? params.type : 'cart'
  let items = []

  if (type === 'direct') {
    const detail = await getProductDetail(params.productNo)
    const skuId = Number(params.skuId)
    const sku = detail && (detail.skus || []).find(item => Number(item.id) === skuId)
    if (!detail || !sku) return { success: false, message: '商品信息不完整' }
    const price = Number(sku.price || detail.price || 0)
    const quantity = Number(params.quantity) || 1
    items = [{
      product_id: detail.id,
      sku_id: skuId,
      product_no: detail.no,
      title: detail.title,
      image_url: sku.image_url || (detail.banners && detail.banners[0]) || '',
      spec_text: sku.spec_values || '',
      price,
      priceText: _formatPrice(price),
      quantity,
      subtotal: price * quantity,
      subtotalText: _formatPrice(price * quantity)
    }]
  } else {
    const selectedItems = await request({ url: '/api/cart/selected' })
    items = (selectedItems || []).map(item => _toOrderPreviewItem(_toCartItem(item)))
    if (items.length === 0) return { success: false, message: '请先选择商品' }
  }

  if (items.length === 0) return { success: false, message: '暂无可下单商品' }
  const address = await getDefaultAddress()

  return {
    success: true,
    data: {
      ..._buildOrderConfirmData(items),
      address
    }
  }
}

/**
 * 获取订单列表（含商品明细）
 * @param {string} status  'ALL' | 'UNPAID' | 'UNSHIPPED' | 'UNRECEIVED' | 'FINISHED'
 * @returns {Array}
 */
async function getOrders(status) {
  const data = {
    page: 1,
    size: 50
  }
  const backendStatus = _toBackendOrderStatus(status)
  if (backendStatus) data.status = backendStatus

  const result = await request({
    url: '/api/orders/my-orders',
    data
  })
  return ((result && result.data) || []).map(_toOrder)
}

/**
 * 将购物车中已勾选的 items 创建为订单
 * @returns {{ success: boolean, orderId?: number, message?: string }}
 */
async function createOrder(options = {}) {
  const cartItemIds = options.cartItemIds && options.cartItemIds.length
    ? options.cartItemIds
    : [0]
  const order = await request({
    url: '/api/orders',
    method: 'POST',
    data: {
      addressId: options.addressId,
      cartItemIds,
      remark: options.remark || ''
    }
  })
  return { success: true, orderId: order.id, orderNo: order.orderNo || order.order_no, data: _toOrder(order) }
}

/**
 * 立即购买创建订单
 * @param {{ productNo: string, skuId: number, quantity: number, remark?: string }} params
 */
async function createDirectOrder(params) {
  const detail = await getProductDetail(params.productNo)
  if (!detail) return { success: false, message: '商品信息不完整' }

  const existing = (await getCartItems()).find(item =>
    Number(item.product_id) === Number(detail.id) && Number(item.sku_id) === Number(params.skuId)
  )
  if (existing) {
    return { success: false, message: '该规格已在购物车中，请从购物车结算' }
  }

  const cartResult = await addToCart(detail.id, params.skuId, params.quantity)
  const cartItemId = cartResult.data && cartResult.data.id
  if (!cartItemId) return { success: false, message: '创建订单商品失败' }
  return createOrder({
    remark: params.remark,
    addressId: params.addressId,
    cartItemIds: [cartItemId]
  })
}

/**
 * 获取订单详情
 * @param {number} orderId
 * @returns {{ success: boolean, data?: object, message?: string }}
 */
async function getOrderDetail(orderId) {
  const order = await request({ url: `/api/orders/${orderId}` })
  return { success: true, data: _toOrder(order) }
}

/**
 * 支付订单（模拟）
 * @param {number} orderId
 */
async function payOrder(orderId) {
  await request({
    url: `/api/orders/${orderId}/pay`,
    method: 'PUT'
  })
  return { success: true }
}

const REMIND_SHIP_STORAGE_KEY = 'order_remind_ship_counts'
const REMIND_SHIP_TARGET_COUNT = 3
let remindShipMemoryCounts = {}

function _getRemindShipCounts() {
  if (typeof wx === 'undefined' || !wx.getStorageSync) return remindShipMemoryCounts
  try {
    return wx.getStorageSync(REMIND_SHIP_STORAGE_KEY) || {}
  } catch (err) {
    return {}
  }
}

function _setRemindShipCounts(counts) {
  if (typeof wx === 'undefined' || !wx.setStorageSync) {
    remindShipMemoryCounts = counts
    return
  }
  try {
    wx.setStorageSync(REMIND_SHIP_STORAGE_KEY, counts)
  } catch (err) {
    // 本地存储失败时降级到内存，避免影响课设演示流程。
    remindShipMemoryCounts = counts
  }
}

function _increaseRemindShipCount(orderId) {
  const key = String(orderId)
  const counts = _getRemindShipCounts()
  const count = Math.min((counts[key] || 0) + 1, REMIND_SHIP_TARGET_COUNT)
  counts[key] = count
  _setRemindShipCounts(counts)

  return {
    count,
    remaining: Math.max(REMIND_SHIP_TARGET_COUNT - count, 0),
    shouldShip: count >= REMIND_SHIP_TARGET_COUNT
  }
}

function _clearRemindShipCount(orderId) {
  const key = String(orderId)
  const counts = _getRemindShipCounts()
  if (!counts[key]) return
  delete counts[key]
  _setRemindShipCounts(counts)
}

/**
 * 提醒商家发货（课设模拟：前端第 3 次提醒后自动发货）
 * @param {number} orderId
 * @returns {{ success: boolean, message?: string, shipped?: boolean, remindCount?: number, remaining?: number }}
 */
async function remindShipOrder(orderId) {
  const reminder = _increaseRemindShipCount(orderId)
  if (!reminder.shouldShip) {
    return {
      success: true,
      shipped: false,
      remindCount: reminder.count,
      remaining: reminder.remaining,
      message: `再提醒${reminder.remaining}次将自动发货`
    }
  }

  try {
    await request({
      url: `/api/orders/${orderId}/remind-ship`,
      method: 'PUT'
    })
    _clearRemindShipCount(orderId)
    return { success: true, shipped: true, remindCount: reminder.count, remaining: 0, message: '已自动发货' }
  } catch (err) {
    _clearRemindShipCount(orderId)
    throw err
  }
}

/**
 * 取消订单
 * @param {number} orderId
 */
async function cancelOrder(orderId) {
  await request({
    url: `/api/orders/${orderId}/cancel`,
    method: 'PUT'
  })
  return { success: true }
}

/**
 * 确认收货（模拟）
 * @param {number} orderId
 * @returns {{ success: boolean, message?: string }}
 */
async function confirmReceiveOrder(orderId) {
  await request({
    url: `/api/orders/${orderId}/confirm`,
    method: 'PUT'
  })
  return { success: true }
}

/**
 * 用户侧逻辑删除订单
 * @param {number} orderId
 * @returns {{ success: boolean, message?: string }}
 */
async function deleteOrder(orderId) {
  await request({
    url: `/api/orders/${orderId}`,
    method: 'DELETE'
  })
  return { success: true }
}

// ==================== 个人中心 ====================

function _normalizeLoginUser(loginData = {}) {
  const userId = loginData.userId || loginData.id || 0
  return {
    id: userId,
    userId,
    openid: loginData.openid || '',
    nickname: loginData.nickname || '微信用户',
    avatar_url: loginData.avatarUrl || loginData.avatar_url || '',
    avatarUrl: loginData.avatarUrl || loginData.avatar_url || '',
    token: loginData.token || '',
    phone: loginData.phone || '',
    gender: loginData.gender || 0,
    isLoggedIn: !!loginData.token
  }
}

function _emptyOrderStats() {
  return { unpaid: 0, unshipped: 0, unreceived: 0, finished: 0 }
}

function _normalizeUserInfo(userData = {}, token = _getStoredToken()) {
  const userId = userData.userId || userData.id || 0
  return {
    id: userId,
    userId,
    openid: userData.openid || '',
    nickname: userData.nickname || '微信用户',
    avatar_url: userData.avatarUrl || userData.avatar_url || '',
    avatarUrl: userData.avatarUrl || userData.avatar_url || '',
    token,
    phone: userData.phone || '',
    gender: userData.gender || 0,
    isLoggedIn: !!token
  }
}

function _getStoredUserInfo() {
  if (typeof wx === 'undefined' || !wx.getStorageSync) return null
  try {
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo')
    if (!token || !userInfo) return null
    return {
      ...userInfo,
      token,
      isLoggedIn: true
    }
  } catch (err) {
    return null
  }
}

function _setLoginStorage(userInfo) {
  if (typeof wx === 'undefined' || !wx.setStorageSync) return
  wx.setStorageSync('token', userInfo.token)
  wx.setStorageSync('userId', userInfo.userId || userInfo.id)
  wx.setStorageSync('userInfo', userInfo)
}

function _getStoredToken() {
  if (typeof wx === 'undefined' || !wx.getStorageSync) return ''
  try {
    return wx.getStorageSync('token') || ''
  } catch (err) {
    return ''
  }
}

/**
 * 获取当前用户信息
 * @returns {object}
 */
function getUserInfo() {
  const storedUserInfo = _getStoredUserInfo()
  if (storedUserInfo) return storedUserInfo
  return { id: 0, userId: 0, nickname: '', avatar_url: '', avatarUrl: '', phone: '', openid: '', isLoggedIn: false }
}

async function fetchUserInfo() {
  const token = _getStoredToken()
  if (!token) return getUserInfo()

  const userData = await request({ url: '/api/users/me' })
  const userInfo = _normalizeUserInfo(userData, token)
  _setLoginStorage(userInfo)
  return userInfo
}

/**
 * 微信真实登录
 * @param {{ code: string, nickname?: string, avatarUrl?: string, gender?: number }} payload
 * @returns {Promise<object>}
 */
async function wxLogin(payload = {}) {
  const loginData = await request({
    url: '/api/auth/wx-login',
    method: 'POST',
    data: payload
  })
  const userInfo = _normalizeLoginUser(loginData)
  _setLoginStorage(userInfo)
  return userInfo
}

/**
 * 模拟登录并返回完整用户信息
 * @returns {{ success: boolean, userInfo: object }}
 */
function mockLogin() {
  const userInfo = getUserInfo()
  if (!userInfo.isLoggedIn) return { success: false, message: '请使用微信登录' }
  return { success: true, userInfo }
}

/**
 * 更新当前用户资料
 * @param {{ nickname?: string, phone?: string, gender?: number|string }} profile
 * @returns {{ success: boolean, message?: string, userInfo?: object }}
 */
async function updateUserProfile(profile = {}) {
  const nickname = (profile.nickname || '').trim()
  const phone = (profile.phone || '').trim()
  const gender = Number(profile.gender)

  if (!nickname) return { success: false, message: '请输入用户名' }
  if (nickname.length > 16) return { success: false, message: '用户名最多16个字' }
  if (!phone) return { success: false, message: '请输入手机号' }
  if (!/^1[3-9]\d{9}$/.test(phone)) return { success: false, message: '手机号格式不正确' }
  if (![1, 2].includes(gender)) return { success: false, message: '请选择性别' }

  const token = _getStoredToken()
  if (!token) return { success: false, message: '请先登录' }

  try {
    const updatedUser = await request({
      url: '/api/users/me',
      method: 'PUT',
      data: { nickname, phone, gender }
    })
    const userInfo = _normalizeUserInfo(updatedUser, token)
    _setLoginStorage(userInfo)
    return { success: true, userInfo }
  } catch (err) {
    return { success: false, message: err.message || '保存失败', handled: !!err.handled }
  }
}

/**
 * 获取订单状态统计（用于"我的"页面快捷入口角标）
 * @returns {{ unpaid: number, unshipped: number, unreceived: number, finished: number }}
 */
async function getOrderStats() {
  if (!_getStoredToken()) return _emptyOrderStats()

  const stats = await request({ url: '/api/orders/my-orders/stats' })
  return {
    unpaid: Number(stats.unpaidCount || stats.unpaid || 0),
    unshipped: Number(stats.unshippedCount || stats.unshipped || 0),
    unreceived: Number(stats.unreceivedCount || stats.unreceived || 0),
    finished: Number(stats.finishedCount || stats.finished || 0)
  }
}

// ==================== 导出 ====================

module.exports = {
  // 首页
  getHomeData,
  getHomeProducts,
  // 分类
  getCategoriesWithProducts,
  getProductsByCategory,
  // 搜索
  searchProducts,
  // 商品详情
  getProductDetail,
  getSkuById,
  // 购物车
  getCartItems,
  addToCart,
  toggleCartItem,
  toggleAllCartItems,
  updateCartItemCount,
  removeCartItem,
  // 订单
  getOrders,
  getOrderDetail,
  getOrderConfirmData,
  createOrder,
  createDirectOrder,
  payOrder,
  cancelOrder,
  remindShipOrder,
  confirmReceiveOrder,
  deleteOrder,
  // 地址
  getAddresses,
  getAddressById,
  getDefaultAddress,
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  // 个人中心
  getUserInfo,
  fetchUserInfo,
  wxLogin,
  mockLogin,
  updateUserProfile,
  getOrderStats
}
