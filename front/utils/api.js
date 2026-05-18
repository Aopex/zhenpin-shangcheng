/**
 * utils/api.js
 * Mock API 服务层 — 页面调用这里，不直接读 db.js
 * 内部操作 db 数组，保证数据关联和一致性
 */

const db = require('./db')
const { BASE_URL, request } = require('./request')

function _formatPrice(price) {
  const value = Number(price || 0)
  return value.toFixed(2)
}

function _normalizeImageUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  return `${BASE_URL}${url.startsWith('/') ? url : `/${url}`}`
}

function _toHomeProduct(product) {
  return {
    id: product.id,
    no: product.productNo || product.product_no,
    title: product.title,
    price: _formatPrice(product.price),
    imageUrl: _normalizeImageUrl(product.imageUrl || product.main_image)
  }
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
    request({ url: '/api/products', data: { page: 1, pageSize: 40 } })
  ])

  const banners = (bannersData || []).map(b => ({
    id: b.id,
    title: b.title,
    imgUrl: _normalizeImageUrl(b.imageUrl || b.image_url),
    linkType: b.linkType || b.link_type,
    linkValue: b.linkValue || b.link_value
  }))

  const categories = (categoriesData || []).map(c => ({
    id: c.id,
    name: c.name,
    icon: c.iconUrl || c.icon_url
  }))

  const products = ((productsPage && productsPage.data) || []).map(_toHomeProduct)

  return { banners, categories, products }
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
  return {
    id: p.id,
    no: p.product_no,
    title: p.title,
    price: p.price.toFixed(2),
    imageUrl: p.main_image,
    stock: p.stock,
    sales: p.sales,
    categoryId: p.category_id,
    categoryName: categoryMap[p.category_id] || '',
    createdAt: p.created_at || ''
  }
}

/**
 * 商品搜索（按 product 表维度检索，SKU 价格和库存仍在详情页处理）
 * @param {{ keyword?: string, sortType?: string, categoryId?: number|string, minPrice?: number|string, maxPrice?: number|string, inStockOnly?: boolean }} params
 * @returns {{ products: Array, categories: Array, filterSummary: Array, total: number }}
 */
function searchProducts(params = {}) {
  const keyword = (params.keyword || '').trim().toLowerCase()
  const sortType = params.sortType || 'comprehensive'
  const categoryId = Number(params.categoryId || 0)
  const minPrice = params.minPrice === '' || params.minPrice === undefined ? null : Number(params.minPrice)
  const maxPrice = params.maxPrice === '' || params.maxPrice === undefined ? null : Number(params.maxPrice)
  const inStockOnly = !!params.inStockOnly

  const categories = db.category
    .filter(c => c.status === 1)
    .sort((a, b) => b.sort_order - a.sort_order)
    .map(c => ({ id: c.id, name: c.name }))

  const categoryMap = categories.reduce((map, item) => {
    map[item.id] = item.name
    return map
  }, {})

  let products = db.product.filter(p => {
    if (p.status !== 1) return false
    if (keyword && !p.title.toLowerCase().includes(keyword) && !p.product_no.toLowerCase().includes(keyword)) return false
    if (categoryId && p.category_id !== categoryId) return false
    if (minPrice !== null && !Number.isNaN(minPrice) && p.price < minPrice) return false
    if (maxPrice !== null && !Number.isNaN(maxPrice) && p.price > maxPrice) return false
    if (inStockOnly && p.stock <= 0) return false
    return true
  })

  products.sort((a, b) => {
    if (sortType === 'latest') {
      return new Date(b.created_at || 0) - new Date(a.created_at || 0)
    }
    if (sortType === 'priceAsc') return a.price - b.price
    if (sortType === 'priceDesc') return b.price - a.price
    if (sortType === 'sales') return b.sales - a.sales
    return (b.sort_order - a.sort_order) || (b.sales - a.sales)
  })

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
    total: products.length
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
 * @returns {object|null}
 */
function getSkuById(skuId) {
  const sku = db.product_sku.find(s => s.id === skuId)
  if (!sku) return null
  const p = db.product.find(item => item.id === sku.product_id)
  return {
    id: sku.id,
    product_id: sku.product_id,
    sku_no: sku.sku_no,
    spec_values: sku.spec_values,
    price: sku.price || (p ? p.price : 0),
    stock: sku.stock,
    image_url: sku.image_url
  }
}

// ==================== 购物车 ====================

/**
 * 获取当前用户的购物车列表（已关联商品和SKU信息）
 * @returns {Array}
 */
function getCartItems() {
  return db.cart_item
    .filter(item => item.user_id === 1)
    .map(item => {
      const p = db.product.find(prod => prod.id === item.product_id)
      const sku = db.product_sku.find(s => s.id === item.sku_id)
      if (!p) return null
      return {
        id: item.id,
        product_id: item.product_id,
        sku_id: item.sku_id,
        no: p.product_no,
        title: p.title,
        price: (sku && sku.price) ? sku.price.toFixed(2) : p.price.toFixed(2),
        imageUrl: p.main_image,
        spec_values: sku ? sku.spec_values : '',
        count: item.quantity,
        checked: item.checked === 1
      }
    })
    .filter(Boolean)
}

/**
 * 添加商品到购物车
 * @param {number} productId
 * @param {number} skuId
 * @param {number} quantity
 * @returns {{ success: boolean, message: string }}
 */
function addToCart(productId, skuId, quantity) {
  // 检查是否已存在同 product+sku
  const existing = db.cart_item.find(
    item => item.user_id === 1 && item.product_id === productId && item.sku_id === skuId
  )
  if (existing) {
    existing.quantity += quantity
    existing.updated_at = _now()
    return { success: true, message: '数量已更新' }
  }

  const maxId = db.cart_item.length > 0 ? Math.max(...db.cart_item.map(i => i.id)) : 0
  db.cart_item.push({
    id: maxId + 1,
    user_id: 1,
    product_id: productId,
    sku_id: skuId,
    quantity: quantity,
    checked: 1,
    created_at: _now(),
    updated_at: _now()
  })
  return { success: true, message: '添加成功' }
}

/**
 * 切换购物车某项的选中状态
 * @param {number} cartItemId
 */
function toggleCartItem(cartItemId) {
  const item = db.cart_item.find(i => i.id === cartItemId)
  if (item) {
    item.checked = item.checked === 1 ? 0 : 1
    item.updated_at = _now()
  }
}

/**
 * 全选 / 取消全选
 * @param {boolean} checked
 */
function toggleAllCartItems(checked) {
  db.cart_item.filter(i => i.user_id === 1).forEach(item => {
    item.checked = checked ? 1 : 0
    item.updated_at = _now()
  })
}

/**
 * 修改购物车商品数量
 * @param {number} cartItemId
 * @param {'add'|'minus'} type
 * @returns {{ success: boolean, message?: string, count?: number }}
 */
function updateCartItemCount(cartItemId, type) {
  const item = db.cart_item.find(i => i.id === cartItemId)
  if (!item) return { success: false }

  if (type === 'add') {
    item.quantity++
  } else if (type === 'minus') {
    if (item.quantity <= 1) {
      return { success: false, message: '受不了了，宝贝不能再少了' }
    }
    item.quantity--
  }
  item.updated_at = _now()
  return { success: true, count: item.quantity }
}

/**
 * 从购物车删除某项
 * @param {number} cartItemId
 */
function removeCartItem(cartItemId) {
  const idx = db.cart_item.findIndex(i => i.id === cartItemId)
  if (idx !== -1) db.cart_item.splice(idx, 1)
}

// ==================== 订单 ====================

function _formatAddress(addr) {
  if (!addr) return null
  return {
    id: addr.id,
    user_id: addr.user_id,
    name: addr.name,
    phone: addr.phone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail,
    is_default: addr.is_default,
    fullAddress: addr.province + addr.city + addr.district + addr.detail,
    created_at: addr.created_at,
    updated_at: addr.updated_at
  }
}

function _getUserAddresses() {
  return db.address
    .filter(a => a.user_id === 1)
    .sort((a, b) => a.id - b.id)
}

function _normalizeDefaultAddress(addressId) {
  const addresses = db.address.filter(a => a.user_id === 1)
  if (addresses.length === 0) return

  let defaultId = Number(addressId || 0)
  const hasRequestedDefault = addresses.some(a => a.id === defaultId)
  if (!hasRequestedDefault) {
    const existingDefault = addresses.find(a => a.is_default === 1)
    defaultId = existingDefault ? existingDefault.id : addresses[0].id
  }

  addresses.forEach(addr => {
    addr.is_default = addr.id === defaultId ? 1 : 0
  })
}

function _getDefaultAddress() {
  return _formatAddress(db.address.find(a => a.user_id === 1 && a.is_default === 1) || db.address.find(a => a.user_id === 1))
}

function getAddresses() {
  _normalizeDefaultAddress()
  return _getUserAddresses().map(_formatAddress)
}

function getAddressById(addressId) {
  return _formatAddress(db.address.find(a => a.user_id === 1 && a.id === Number(addressId)))
}

function createAddress(payload = {}) {
  const now = _now()
  const maxId = db.address.length > 0 ? Math.max(...db.address.map(a => a.id)) : 0
  const shouldBeDefault = db.address.filter(a => a.user_id === 1).length === 0 || Number(payload.is_default) === 1
  const newAddress = {
    id: maxId + 1,
    user_id: 1,
    name: (payload.name || '').trim(),
    phone: (payload.phone || '').trim(),
    province: (payload.province || '').trim(),
    city: (payload.city || '').trim(),
    district: (payload.district || '').trim(),
    detail: (payload.detail || '').trim(),
    is_default: shouldBeDefault ? 1 : 0,
    created_at: now,
    updated_at: now
  }
  db.address.push(newAddress)
  if (shouldBeDefault) _normalizeDefaultAddress(newAddress.id)
  return { success: true, data: _formatAddress(newAddress) }
}

function updateAddress(addressId, payload = {}) {
  const addr = db.address.find(a => a.user_id === 1 && a.id === Number(addressId))
  if (!addr) return { success: false, message: '地址不存在' }

  addr.name = (payload.name || '').trim()
  addr.phone = (payload.phone || '').trim()
  addr.province = (payload.province || '').trim()
  addr.city = (payload.city || '').trim()
  addr.district = (payload.district || '').trim()
  addr.detail = (payload.detail || '').trim()
  addr.is_default = Number(payload.is_default) === 1 ? 1 : 0
  addr.updated_at = _now()

  if (addr.is_default === 1) _normalizeDefaultAddress(addr.id)
  else _normalizeDefaultAddress()

  return { success: true, data: _formatAddress(addr) }
}

function deleteAddress(addressId) {
  const idx = db.address.findIndex(a => a.user_id === 1 && a.id === Number(addressId))
  if (idx === -1) return { success: false, message: '地址不存在' }

  const removed = db.address[idx]
  db.address.splice(idx, 1)
  if (removed.is_default === 1) _normalizeDefaultAddress()

  return { success: true }
}

function setDefaultAddress(addressId) {
  const addr = db.address.find(a => a.user_id === 1 && a.id === Number(addressId))
  if (!addr) return { success: false, message: '地址不存在' }

  _normalizeDefaultAddress(addr.id)
  addr.updated_at = _now()
  return { success: true, data: _formatAddress(addr) }
}

function _buildOrderPreviewItems(sourceItems) {
  return sourceItems
    .map(item => {
      const p = db.product.find(prod => prod.id === item.product_id)
      const sku = db.product_sku.find(s => s.id === item.sku_id)
      if (!p || !sku) return null
      const price = sku.price || p.price
      const quantity = item.quantity || item.count || 1
      return {
        product_id: p.id,
        sku_id: sku.id,
        product_no: p.product_no,
        title: p.title,
        image_url: sku.image_url || p.main_image,
        spec_text: sku.spec_values,
        price: price,
        priceText: price.toFixed(2),
        quantity: quantity,
        subtotal: price * quantity,
        subtotalText: (price * quantity).toFixed(2)
      }
    })
    .filter(Boolean)
}

function _buildOrderConfirmData(items) {
  const totalCount = items.reduce((sum, item) => sum + item.quantity, 0)
  const goodsAmount = items.reduce((sum, item) => sum + item.subtotal, 0)
  const freightAmount = goodsAmount > 0 ? 0 : 0
  const discountAmount = 0
  const totalAmount = goodsAmount + freightAmount - discountAmount

  return {
    address: _getDefaultAddress(),
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

/**
 * 获取订单确认页数据
 * @param {{ type: 'cart'|'direct', productNo?: string, skuId?: number, quantity?: number }} params
 */
function getOrderConfirmData(params) {
  const type = params && params.type ? params.type : 'cart'
  let sourceItems = []

  if (type === 'direct') {
    const p = db.product.find(item => item.product_no === params.productNo)
    const skuId = Number(params.skuId)
    const sku = db.product_sku.find(item => item.id === skuId && item.product_id === (p && p.id))
    if (!p || !sku) return { success: false, message: '商品信息不完整' }
    sourceItems = [{
      product_id: p.id,
      sku_id: skuId,
      quantity: Number(params.quantity) || 1
    }]
  } else {
    sourceItems = db.cart_item.filter(item => item.user_id === 1 && item.checked === 1)
    if (sourceItems.length === 0) return { success: false, message: '请先选择商品' }
  }

  const items = _buildOrderPreviewItems(sourceItems)
  if (items.length === 0) return { success: false, message: '暂无可下单商品' }

  return {
    success: true,
    data: _buildOrderConfirmData(items)
  }
}

function _createOrderFromItems(items, options = {}) {
  if (!items || items.length === 0) {
    return { success: false, message: '请先选择商品' }
  }

  const address = options.address || (options.addressId ? getAddressById(options.addressId) : _getDefaultAddress())
  if (!address) {
    return { success: false, message: '请先添加收货地址' }
  }

  const totalCount = items.reduce((sum, item) => sum + item.quantity, 0)
  const goodsAmount = items.reduce((sum, item) => sum + item.subtotal, 0)
  const freightAmount = Number(options.freightAmount || 0)
  const discountAmount = Number(options.discountAmount || 0)
  const totalAmount = goodsAmount + freightAmount - discountAmount
  const now = _now()

  const maxOrderId = db.order.length > 0 ? Math.max(...db.order.map(o => o.id)) : 0
  const orderNo = 'DD' + now.replace(/[-: ]/g, '').substring(0, 10) + String(maxOrderId + 1).padStart(3, '0')
  const newOrder = {
    id: maxOrderId + 1,
    order_no: orderNo,
    user_id: 1,
    status: 'UNPAID',
    total_count: totalCount,
    total_amount: totalAmount,
    freight_amount: freightAmount,
    discount_amount: discountAmount,
    address_snapshot: {
      id: address.id,
      name: address.name,
      phone: address.phone,
      province: address.province,
      city: address.city,
      district: address.district,
      detail: address.detail,
      address: address.fullAddress || (address.province + address.city + address.district + address.detail)
    },
    remark: options.remark || null,
    is_deleted: 0,
    pay_time: null,
    ship_time: null,
    receive_time: null,
    finish_time: null,
    cancel_time: null,
    created_at: now,
    updated_at: now
  }
  db.order.push(newOrder)

  const maxOrderItemId = db.order_item.length > 0 ? Math.max(...db.order_item.map(oi => oi.id)) : 0
  items.forEach((item, idx) => {
    db.order_item.push({
      id: maxOrderItemId + idx + 1,
      order_id: newOrder.id,
      product_id: item.product_id,
      sku_id: item.sku_id,
      product_no: item.product_no,
      title: item.title,
      image_url: item.image_url,
      spec_text: item.spec_text,
      price: item.price,
      quantity: item.quantity,
      subtotal: item.subtotal,
      created_at: now
    })
  })

  return { success: true, orderId: newOrder.id, orderNo: newOrder.order_no }
}

/**
 * 获取订单列表（含商品明细）
 * @param {string} status  'ALL' | 'UNPAID' | 'UNSHIPPED' | 'UNRECEIVED' | 'FINISHED'
 * @returns {Array}
 */
function getOrders(status) {
  const STATUS_TEXT = {
    UNPAID: '等待买家付款',
    UNSHIPPED: '买家已付款',
    UNRECEIVED: '卖家已发货',
    FINISHED: '交易成功',
    CANCELLED: '已取消'
  }

  let orders = db.order.filter(o => o.user_id === 1 && o.is_deleted !== 1)
  if (status && status !== 'ALL') {
    orders = orders.filter(o => o.status === status)
  }

  orders.sort((a, b) => new Date(b.created_at) - new Date(a.created_at))

  return orders.map(o => {
    const items = db.order_item.filter(oi => oi.order_id === o.id)
    return {
      id: o.id,
      orderNo: o.order_no,
      status: o.status,
      statusText: STATUS_TEXT[o.status] || o.status,
      totalCount: o.total_count,
      totalAmount: o.total_amount.toFixed(2),
      goods: items.map(oi => ({
        id: oi.id,
        title: oi.title,
        price: oi.price.toFixed(2),
        count: oi.quantity,
        imageUrl: oi.image_url
      }))
    }
  })
}

/**
 * 将购物车中已勾选的 items 创建为订单
 * @returns {{ success: boolean, orderId?: number, message?: string }}
 */
function createOrder(options = {}) {
  const selectedItems = db.cart_item.filter(i => i.user_id === 1 && i.checked === 1)
  if (selectedItems.length === 0) {
    return { success: false, message: '请先选择商品' }
  }
  const items = _buildOrderPreviewItems(selectedItems)
  const result = _createOrderFromItems(items, {
    remark: options.remark,
    freightAmount: options.freightAmount,
    discountAmount: options.discountAmount,
    addressId: options.addressId
  })
  if (!result.success) return result

  // 从购物车移除已勾选项
  selectedItems.forEach(item => {
    const idx = db.cart_item.findIndex(ci => ci.id === item.id)
    if (idx !== -1) db.cart_item.splice(idx, 1)
  })

  return result
}

/**
 * 立即购买创建订单
 * @param {{ productNo: string, skuId: number, quantity: number, remark?: string }} params
 */
function createDirectOrder(params) {
  const preview = getOrderConfirmData({
    type: 'direct',
    productNo: params.productNo,
    skuId: params.skuId,
    quantity: params.quantity
  })
  if (!preview.success) return preview
  const address = params.addressId ? getAddressById(params.addressId) : preview.data.address
  if (params.addressId && !address) return { success: false, message: '地址不存在' }
  return _createOrderFromItems(preview.data.goods, {
    remark: params.remark,
    freightAmount: preview.data.freight_amount,
    discountAmount: preview.data.discount_amount,
    address
  })
}

/**
 * 获取订单详情
 * @param {number} orderId
 * @returns {{ success: boolean, data?: object, message?: string }}
 */
function getOrderDetail(orderId) {
  const STATUS_TEXT = {
    UNPAID: '等待买家付款',
    UNSHIPPED: '买家已付款',
    UNRECEIVED: '卖家已发货',
    FINISHED: '交易成功',
    CANCELLED: '已取消'
  }
  const STATUS_SHORT_TEXT = {
    UNPAID: '待付款',
    UNSHIPPED: '待发货',
    UNRECEIVED: '待收货',
    FINISHED: '已完成',
    CANCELLED: '已取消'
  }
  const o = db.order.find(order => order.id === Number(orderId) && order.user_id === 1 && order.is_deleted !== 1)
  if (!o) return { success: false, message: '订单不存在' }

  const items = db.order_item.filter(oi => oi.order_id === o.id)
  const goodsAmount = items.reduce((sum, item) => sum + item.subtotal, 0)
  const address = o.address_snapshot || null
  const addressText = address
    ? (address.address || ((address.province || '') + (address.city || '') + (address.district || '') + (address.detail || '')))
    : ''

  return {
    success: true,
    data: {
      id: o.id,
      orderNo: o.order_no,
      status: o.status,
      statusText: STATUS_TEXT[o.status] || o.status,
      statusShortText: STATUS_SHORT_TEXT[o.status] || o.status,
      totalCount: o.total_count,
      totalAmount: o.total_amount.toFixed(2),
      freightAmount: o.freight_amount.toFixed(2),
      discountAmount: o.discount_amount.toFixed(2),
      goodsAmount: goodsAmount.toFixed(2),
      address: address ? {
        name: address.name || '',
        phone: address.phone || '',
        address: addressText
      } : null,
      remark: o.remark || '',
      createdAt: o.created_at,
      updatedAt: o.updated_at,
      payTime: o.pay_time || '',
      shipTime: o.ship_time || '',
      receiveTime: o.receive_time || '',
      finishTime: o.finish_time || '',
      cancelTime: o.cancel_time || '',
      goods: items.map(oi => ({
        id: oi.id,
        productId: oi.product_id,
        skuId: oi.sku_id,
        productNo: oi.product_no,
        title: oi.title,
        imageUrl: oi.image_url,
        specText: oi.spec_text,
        price: oi.price.toFixed(2),
        count: oi.quantity,
        subtotal: oi.subtotal.toFixed(2),
        createdAt: oi.created_at
      }))
    }
  }
}

/**
 * 支付订单（模拟）
 * @param {number} orderId
 */
function payOrder(orderId) {
  const o = db.order.find(order => order.id === orderId)
  if (o && o.status === 'UNPAID') {
    o.status = 'UNSHIPPED'
    o.pay_time = _now()
    o.updated_at = _now()
    _clearRemindShipCount(orderId)
  }
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
function remindShipOrder(orderId) {
  const o = db.order.find(order => order.id === Number(orderId) && order.user_id === 1)
  if (!o || o.is_deleted === 1) return { success: false, message: '订单不存在' }
  if (o.status !== 'UNSHIPPED') return { success: false, message: '当前订单不能提醒发货' }

  const reminder = _increaseRemindShipCount(orderId)
  if (!reminder.shouldShip) {
    return {
      success: true,
      shipped: false,
      remindCount: reminder.count,
      remaining: reminder.remaining,
      message: `再点${reminder.remaining}次发货`
    }
  }

  const now = _now()
  o.status = 'UNRECEIVED'
  o.ship_time = now
  o.updated_at = now
  _clearRemindShipCount(orderId)
  return { success: true, shipped: true, message: '已发货' }
}

/**
 * 取消订单
 * @param {number} orderId
 */
function cancelOrder(orderId) {
  const o = db.order.find(order => order.id === orderId)
  if (o && o.status === 'UNPAID') {
    o.status = 'CANCELLED'
    o.cancel_time = _now()
    o.updated_at = _now()
  }
}

/**
 * 确认收货（模拟）
 * @param {number} orderId
 * @returns {{ success: boolean, message?: string }}
 */
function confirmReceiveOrder(orderId) {
  const o = db.order.find(order => order.id === Number(orderId))
  if (!o) return { success: false, message: '订单不存在' }
  if (o.is_deleted === 1) return { success: false, message: '订单不存在' }
  if (o.status !== 'UNRECEIVED') return { success: false, message: '当前订单不能确认收货' }
  const now = _now()
  o.status = 'FINISHED'
  o.receive_time = now
  o.finish_time = now
  o.updated_at = now
  return { success: true }
}

/**
 * 用户侧逻辑删除订单
 * @param {number} orderId
 * @returns {{ success: boolean, message?: string }}
 */
function deleteOrder(orderId) {
  const o = db.order.find(order => order.id === Number(orderId) && order.user_id === 1)
  if (!o || o.is_deleted === 1) return { success: false, message: '订单不存在' }
  if (!['FINISHED', 'CANCELLED'].includes(o.status)) {
    return { success: false, message: '当前订单不能删除' }
  }
  o.is_deleted = 1
  o.updated_at = _now()
  return { success: true }
}

// ==================== 个人中心 ====================

/**
 * 获取当前用户信息
 * @returns {object}
 */
function getUserInfo() {
  const u = db.user.find(user => user.id === 1)
  if (!u) return { id: 0, nickname: '未登录', avatar_url: '', phone: '', openid: '', isLoggedIn: false }
  return {
    id: u.id,
    openid: u.openid,
    union_id: u.union_id,
    nickname: u.nickname,
    avatar_url: u.avatar_url,
    phone: u.phone,
    gender: u.gender,
    status: u.status,
    created_at: u.created_at,
    updated_at: u.updated_at,
    isLoggedIn: true
  }
}

/**
 * 模拟登录并返回完整用户信息
 * @returns {{ success: boolean, userInfo: object }}
 */
function mockLogin() {
  const u = db.user.find(user => user.id === 1)
  if (!u) return { success: false, message: '用户不存在' }
  u.updated_at = _now()
  return { success: true, userInfo: getUserInfo() }
}

/**
 * 更新当前用户资料
 * @param {{ nickname?: string, phone?: string, gender?: number|string }} profile
 * @returns {{ success: boolean, message?: string, userInfo?: object }}
 */
function updateUserProfile(profile = {}) {
  const u = db.user.find(user => user.id === 1)
  if (!u) return { success: false, message: '用户不存在' }

  const nickname = (profile.nickname || '').trim()
  const phone = (profile.phone || '').trim()
  const gender = Number(profile.gender)

  if (!nickname) return { success: false, message: '请输入用户名' }
  if (nickname.length > 16) return { success: false, message: '用户名最多16个字' }
  if (!phone) return { success: false, message: '请输入手机号' }
  if (!/^[0-9*+\-\s]{6,20}$/.test(phone)) return { success: false, message: '手机号格式不正确' }
  if (![1, 2].includes(gender)) return { success: false, message: '请选择性别' }

  u.nickname = nickname
  u.phone = phone
  u.gender = gender
  u.updated_at = _now()

  return { success: true, userInfo: getUserInfo() }
}

/**
 * 获取订单状态统计（用于"我的"页面快捷入口角标）
 * @returns {{ unpaid: number, unshipped: number, unreceived: number, finished: number }}
 */
function getOrderStats() {
  const orders = db.order.filter(o => o.user_id === 1 && o.is_deleted !== 1)
  return {
    unpaid: orders.filter(o => o.status === 'UNPAID').length,
    unshipped: orders.filter(o => o.status === 'UNSHIPPED').length,
    unreceived: orders.filter(o => o.status === 'UNRECEIVED').length,
    finished: orders.filter(o => o.status === 'FINISHED').length
  }
}

// ==================== 工具函数 ====================

function _now() {
  const d = new Date()
  const pad = n => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// ==================== 导出 ====================

module.exports = {
  // 首页
  getHomeData,
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
  createAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  // 个人中心
  getUserInfo,
  mockLogin,
  updateUserProfile,
  getOrderStats
}
