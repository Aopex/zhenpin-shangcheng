const api = require('../../../utils/api')

const EMPTY_FORM = {
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  is_default: 0
}

Page({
  data: {
    mode: 'manage',
    isSelectMode: false,
    loading: true,
    addresses: [],
    selectedAddressId: 0,
    showEditor: false,
    editorActive: false,
    editingId: 0,
    region: [],
    form: { ...EMPTY_FORM },
    errors: {},
    submitting: false,
    touchStartX: 0,
    touchStartY: 0,
    touchStartOffset: 0,
    touchIndex: -1,
    rpxRatio: 2,
    deleteWidth: 132
  },

  editorTimer: null,

  onLoad(options = {}) {
    const isSelectMode = options.mode === 'select'
    const app = getApp()
    const selectedAddressId = app.globalData && app.globalData.selectedAddressId
    const systemInfo = wx.getSystemInfoSync()

    wx.setNavigationBarTitle({
      title: isSelectMode ? '选择收货地址' : '地址管理'
    })

    this.setData({
      mode: isSelectMode ? 'select' : 'manage',
      isSelectMode,
      selectedAddressId: Number(selectedAddressId || 0),
      rpxRatio: 750 / systemInfo.windowWidth
    })
    this.loadAddresses()
  },

  async loadAddresses() {
    this.setData({ loading: true })
    try {
      const addresses = (await api.getAddresses()).map(item => ({
        ...item,
        swipeX: 0,
        isSwiping: false
      }))
      const selectedAddress = addresses.find(item => item.id === Number(this.data.selectedAddressId))
      const defaultAddress = addresses.find(item => item.is_default === 1)

      this.setData({
        addresses,
        selectedAddressId: selectedAddress ? selectedAddress.id : (defaultAddress ? defaultAddress.id : 0),
        loading: false
      })
    } catch (err) {
      this.setData({ addresses: [], selectedAddressId: 0, loading: false })
      if (!err.handled) wx.showToast({ title: err.message || '地址加载失败', icon: 'none' })
    }
  },

  async selectAddress(e) {
    if (!this.data.isSelectMode) {
      this.openEdit(e)
      return
    }
    const addressId = Number(e.currentTarget.dataset.id)
    let address = null
    try {
      address = await api.getAddressById(addressId)
      if (!address) {
        wx.showToast({ title: '地址不存在', icon: 'none' })
        return
      }
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '地址不存在', icon: 'none' })
      return
    }

    const app = getApp()
    app.globalData = app.globalData || {}
    app.globalData.selectedAddressId = address.id
    wx.navigateBack()
  },

  onAddressTouchStart(e) {
    if (this.data.isSelectMode) return
    const index = e.currentTarget.dataset.index
    const touch = e.touches[0]
    const currentItem = this.data.addresses[index] || {}
    const addresses = this.data.addresses.map((item, itemIndex) => ({
      ...item,
      swipeX: itemIndex === index ? (item.swipeX || 0) : 0,
      isSwiping: itemIndex === index
    }))

    this.setData({
      addresses,
      touchStartX: touch.clientX,
      touchStartY: touch.clientY,
      touchStartOffset: currentItem.swipeX || 0,
      touchIndex: index
    })
  },

  onAddressTouchMove(e) {
    if (this.data.isSelectMode) return
    const index = e.currentTarget.dataset.index
    if (index !== this.data.touchIndex) return

    const touch = e.touches[0]
    const moveX = touch.clientX - this.data.touchStartX
    const moveY = touch.clientY - this.data.touchStartY
    if (Math.abs(moveY) > Math.abs(moveX) && this.data.touchStartOffset === 0) return

    const moveRpx = moveX * this.data.rpxRatio
    const nextSwipeX = Math.max(-this.data.deleteWidth, Math.min(0, this.data.touchStartOffset + moveRpx))

    this.setData({
      [`addresses[${index}].swipeX`]: nextSwipeX
    })
  },

  onAddressTouchEnd(e) {
    if (this.data.isSelectMode) return
    const index = e.currentTarget.dataset.index
    const item = this.data.addresses[index]
    if (!item) return

    const shouldOpen = Math.abs(item.swipeX || 0) > this.data.deleteWidth / 2
    this.setData({
      [`addresses[${index}].swipeX`]: shouldOpen ? -this.data.deleteWidth : 0,
      [`addresses[${index}].isSwiping`]: false,
      touchIndex: -1
    })
  },

  openCreate() {
    if (this.editorTimer) clearTimeout(this.editorTimer)
    this.setData({
      showEditor: true,
      editorActive: false,
      editingId: 0,
      region: [],
      form: { ...EMPTY_FORM },
      errors: {}
    })
    setTimeout(() => {
      this.setData({ editorActive: true })
    }, 30)
  },

  async openEdit(e) {
    if (this.editorTimer) clearTimeout(this.editorTimer)
    const addressId = Number(e.currentTarget.dataset.id)
    let address = null
    try {
      address = await api.getAddressById(addressId)
      if (!address) {
        wx.showToast({ title: '地址不存在', icon: 'none' })
        return
      }
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '地址不存在', icon: 'none' })
      return
    }

    this.setData({
      showEditor: true,
      editorActive: false,
      editingId: address.id,
      region: [address.province, address.city, address.district],
      form: {
        name: address.name,
        phone: address.phone,
        province: address.province,
        city: address.city,
        district: address.district,
        detail: address.detail,
        is_default: address.is_default
      },
      errors: {}
    })
    setTimeout(() => {
      this.setData({ editorActive: true })
    }, 30)
  },

  closeEditor() {
    if (this.data.submitting) return
    if (this.editorTimer) clearTimeout(this.editorTimer)
    this.setData({ editorActive: false })
    this.editorTimer = setTimeout(() => {
      this.setData({
        showEditor: false,
        editingId: 0,
        region: [],
        form: { ...EMPTY_FORM },
        errors: {}
      })
      this.editorTimer = null
    }, 280)
  },

  stopTap() {},

  onNameInput(e) {
    this.updateFormField('name', e.detail.value)
  },

  onPhoneInput(e) {
    this.updateFormField('phone', e.detail.value)
  },

  onDetailInput(e) {
    this.updateFormField('detail', e.detail.value)
  },

  onDefaultChange(e) {
    this.updateFormField('is_default', e.detail.value ? 1 : 0)
  },

  onRegionChange(e) {
    const region = e.detail.value || []
    const form = {
      ...this.data.form,
      province: region[0] || '',
      city: region[1] || '',
      district: region[2] || ''
    }
    const errors = { ...this.data.errors, region: '' }
    this.setData({ region, form, errors })
  },

  updateFormField(field, value) {
    const form = {
      ...this.data.form,
      [field]: value
    }
    const errors = {
      ...this.data.errors,
      [field]: ''
    }
    this.setData({ form, errors })
  },

  validateForm() {
    const form = this.data.form
    const errors = {}

    if (!form.name.trim()) errors.name = '请填写收货人'
    if (!/^1\d{10}$/.test(form.phone.trim())) errors.phone = '请填写 11 位手机号'
    if (!form.province || !form.city || !form.district) errors.region = '请选择省市区'
    if (!form.detail.trim()) errors.detail = '请填写详细地址'

    this.setData({ errors })
    return Object.keys(errors).length === 0
  },

  async saveAddress() {
    if (this.data.submitting || !this.validateForm()) return

    this.setData({ submitting: true })
    const payload = {
      ...this.data.form,
      name: this.data.form.name.trim(),
      phone: this.data.form.phone.trim(),
      detail: this.data.form.detail.trim()
    }
    try {
      const result = this.data.editingId
        ? await api.updateAddress(this.data.editingId, payload)
        : await api.createAddress(payload)

      if (!result.success) {
        wx.showToast({ title: result.message || '保存失败', icon: 'none' })
        return
      }

      wx.showToast({ title: '已保存', icon: 'success' })
      this.setData({ submitting: false })
      this.closeEditor()
      await this.loadAddresses()
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '保存失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  async setDefault(e) {
    const addressId = Number(e.currentTarget.dataset.id)
    const current = this.data.addresses.find(item => item.id === addressId)
    if (current && current.is_default === 1) return

    try {
      const result = await api.setDefaultAddress(addressId)
      if (!result.success) {
        wx.showToast({ title: result.message || '设置失败', icon: 'none' })
        return
      }

      wx.showToast({ title: '已设为默认', icon: 'success' })
      await this.loadAddresses()
    } catch (err) {
      if (!err.handled) wx.showToast({ title: err.message || '设置失败', icon: 'none' })
    }
  },

  deleteAddress(e) {
    const addressId = Number(e.currentTarget.dataset.id)
    wx.showModal({
      title: '删除地址',
      content: '删除后不会影响已生成订单的地址快照。',
      confirmText: '删除',
      confirmColor: '#CB4042',
      success: async res => {
        if (!res.confirm) return

        try {
          const result = await api.deleteAddress(addressId)
          if (!result.success) {
            wx.showToast({ title: result.message || '删除失败', icon: 'none' })
            return
          }

          const app = getApp()
          if (app.globalData && Number(app.globalData.selectedAddressId) === addressId) {
            app.globalData.selectedAddressId = 0
          }

          wx.showToast({ title: '已删除', icon: 'success' })
          await this.loadAddresses()
        } catch (err) {
          if (!err.handled) wx.showToast({ title: err.message || '删除失败', icon: 'none' })
          return
        }
      }
    })
  }
})
