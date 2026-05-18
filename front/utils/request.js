const BASE_URL = 'http://localhost:8080'
const LOGIN_URL = '/api/auth/wx-login'

function buildUrl(url) {
  if (/^https?:\/\//.test(url)) return url
  return `${BASE_URL}${url.startsWith('/') ? url : `/${url}`}`
}

function isLoginRequest(url) {
  return buildUrl(url) === buildUrl(LOGIN_URL)
}

function getStoredToken() {
  if (typeof wx === 'undefined' || !wx.getStorageSync) return ''
  try {
    return wx.getStorageSync('token') || ''
  } catch (err) {
    return ''
  }
}

function clearAuthStorage() {
  if (typeof wx === 'undefined') return
  try {
    wx.removeStorageSync('token')
    wx.removeStorageSync('userId')
    wx.removeStorageSync('userInfo')
  } catch (err) {
    // 忽略本地存储清理失败，避免掩盖真实接口错误。
  }
}

function getAuthErrorMessage(url, hadToken) {
  if (hadToken) return '登录已失效，请重新登录'
  if (buildUrl(url).indexOf(buildUrl('/api/cart')) === 0) return '请先登录后查看购物车'
  return '请先登录后再继续'
}

function buildHeaders(url, header) {
  const headers = {
    'content-type': 'application/json',
    ...header
  }
  const token = getStoredToken()
  if (token && !headers.Authorization && !isLoginRequest(url)) {
    headers.Authorization = `Bearer ${token}`
  }
  return headers
}

function request(options = {}) {
  const {
    url,
    method = 'GET',
    data = {},
    header = {}
  } = options

  return new Promise((resolve, reject) => {
    wx.request({
      url: buildUrl(url),
      method,
      data,
      header: buildHeaders(url, header),
      success(res) {
        const body = res.data || {}
        if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 200) {
          resolve(body.data)
          return
        }
        if (res.statusCode === 401 || body.code === 401) {
          const authMessage = getAuthErrorMessage(url, !!getStoredToken())
          clearAuthStorage()
          if (typeof wx !== 'undefined' && wx.showToast) {
            wx.showToast({ title: authMessage, icon: 'none' })
          }
          const error = new Error(authMessage)
          error.handled = true
          reject(error)
          return
        }
        reject(new Error(body.message || `请求失败：${res.statusCode}`))
      },
      fail(err) {
        reject(new Error(err.errMsg || '网络请求失败'))
      }
    })
  })
}

module.exports = {
  BASE_URL,
  request,
  clearAuthStorage
}
