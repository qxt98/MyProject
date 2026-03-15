/**
 * 前端入口：创建 Vue 应用，挂载 Pinia 状态管理、Vue Router 路由，并挂载到 #app
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
