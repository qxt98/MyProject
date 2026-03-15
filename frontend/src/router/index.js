/**
 * 路由配置：各功能模块对应视图；meta.title 用于标题，meta.roles 用于权限（空表示任意已登录用户）
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore, ROUTE_ROLES } from '@/stores/auth'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录', public: true } },
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
  { path: '/drugs', name: 'Drugs', component: () => import('@/views/DrugList.vue'), meta: { title: '药品信息' } },
  { path: '/stock', name: 'Stock', component: () => import('@/views/Stock.vue'), meta: { title: '库存管理' } },
  { path: '/purchase', name: 'Purchase', component: () => import('@/views/Purchase.vue'), meta: { title: '采购审批' } },
  { path: '/prescription', name: 'Prescription', component: () => import('@/views/Prescription.vue'), meta: { title: '处方审核' } },
  { path: '/guide', name: 'Guide', component: () => import('@/views/Guide.vue'), meta: { title: '用药指导', public: true } },
  { path: '/system', name: 'System', component: () => import('@/views/System.vue'), meta: { title: '系统管理' } },
  { path: '/operation-log', name: 'OperationLog', component: () => import('@/views/OperationLog.vue'), meta: { title: '操作日志' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 药品信息智能化管理系统` : '药品信息智能化管理系统'

  const authStore = useAuthStore()
  // 先从 localStorage 恢复登录态（解决刷新或直接输入 URL 时被误判为未登录）
  authStore.rehydrateFromStorage()
  // 若仍未登录且 localStorage 也无 token，则保持登出状态
  if (!authStore.isLoggedIn && typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('smart_pharma_token')
    if (!token) authStore.logout()
  }

  // 公开页：未登录也可访问；已登录访问登录页时重定向首页，其余公开页（如用药指导）可正常进入
  if (to.meta.public) {
    if (to.path === '/login' && authStore.isLoggedIn) return next('/')
    return next()
  }

  if (!authStore.isLoggedIn) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  const allowedRoles = ROUTE_ROLES[to.path]
  if (allowedRoles !== undefined && allowedRoles.length > 0 && !allowedRoles.includes(authStore.role)) {
    return next('/')
  }
  next()
})

export default router
