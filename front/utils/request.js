const BASE_URL = 'http://localhost:8080'

function buildUrl(url) {
  if (/^https?:\/\//.test(url)) return url
  return `${BASE_URL}${url.startsWith('/') ? url : `/${url}`}`
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
      header: {
        'content-type': 'application/json',
        ...header
      },
      success(res) {
        const body = res.data || {}
        if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 200) {
          resolve(body.data)
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
  request
}
