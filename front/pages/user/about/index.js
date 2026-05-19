Page({
  data: {
    showAnimation: false,
    team: [
      { role: '组长', name: '胡宇衡' },
      { role: '前端开发', name: '何周烨' },
      { role: '后端开发', name: '孙宏烨' }
    ],
    techStack: [
      { title: '前端', desc: '微信小程序原生' },
      { title: '后端', desc: 'Spring Boot, MyBatis' }
    ]
  },
  onLoad() {
    setTimeout(() => {
      this.setData({ showAnimation: true });
    }, 50);
  }
})
