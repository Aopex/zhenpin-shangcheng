const api = require('../../../utils/api')

Page({
  data: {
    nickname: '',
    phone: '',
    gender: 1,
    genderOptions: [
      { value: 1, label: '男' },
      { value: 2, label: '女' }
    ],
    saving: false
  },

  onLoad() {
    this.loadProfile()
  },

  onShow() {
    this.loadProfile()
  },

  loadProfile() {
    const userInfo = api.getUserInfo()
    const nickname = userInfo.nickname || ''
    const gender = Number(userInfo.gender || 1)
    this.setData({
      nickname,
      phone: userInfo.phone || '',
      gender: [1, 2].includes(gender) ? gender : 1
    })
  },

  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },

  selectGender(e) {
    this.setData({ gender: Number(e.currentTarget.dataset.value || 0) })
  },

  saveProfile() {
    if (this.data.saving) return

    this.setData({ saving: true })
    const result = api.updateUserProfile({
      nickname: this.data.nickname,
      phone: this.data.phone,
      gender: this.data.gender
    })

    wx.showToast({
      title: result.success ? '已保存' : (result.message || '保存失败'),
      icon: result.success ? 'success' : 'none'
    })

    if (result.success) {
      getApp().globalData = getApp().globalData || {}
      getApp().globalData.userInfo = result.userInfo
      setTimeout(() => wx.navigateBack(), 500)
      return
    }

    this.setData({ saving: false })
  }
})
