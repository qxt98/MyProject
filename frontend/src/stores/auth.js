/**
 * 认证状态：登录态（token、用户信息）、角色与前端路由权限（按中国医院流程配置）
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, getMe } from '@/api/auth'

const TOKEN_KEY = 'smart_pharma_token'
const USER_KEY = 'smart_pharma_user'

/** 各路由所需角色（空数组表示任意已登录用户均可） */
export const ROUTE_ROLES = {
  '/': [],
  '/drugs': ['ADMIN', 'PHARMACIST'],
  '/stock': ['ADMIN', 'PHARMACIST', 'PURCHASER'],
  '/purchase': ['ADMIN', 'PHARMACIST', 'PURCHASER', 'REVIEWER'],
  '/prescription': ['ADMIN', 'REVIEWER', 'DOCTOR', 'NURSE'],
  '/guide': [],
  '/system': ['ADMIN'],
  '/operation-log': ['ADMIN'],
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(null)

  const userJson = localStorage.getItem(USER_KEY)
  if (userJson) {
    try {
      user.value = JSON.parse(userJson)
    } catch (_) {}
  }

  /** 从 localStorage 恢复登录态（解决刷新或直接输入 URL 时 store 未同步的问题） */
  function rehydrateFromStorage() {
    if (typeof localStorage === 'undefined') return
    const t = localStorage.getItem(TOKEN_KEY)
    const u = localStorage.getItem(USER_KEY)
    if (t) {
      token.value = t
      if (u) {
        try {
          user.value = JSON.parse(u)
        } catch (_) {}
      }
    } else {
      token.value = ''
      user.value = null
    }
  }

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')

  /** 当前用户是否有权访问某路由 */
  function canAccess(path) {
    const allowed = ROUTE_ROLES[path]
    if (allowed === undefined) return true
    if (!allowed || allowed.length === 0) return isLoggedIn.value
    return allowed.includes(role.value)
  }

  /** 可展示的导航项：有权限且已登录 */
  const navItems = computed(() => {
    const items = [
      { path: '/', name: '首页' },
      { path: '/drugs', name: '药品信息' },
      { path: '/stock', name: '库存管理' },
      { path: '/purchase', name: '采购审批' },
      { path: '/prescription', name: '处方审核' },
      { path: '/guide', name: '用药指导' },
      { path: '/system', name: '系统管理' },
      { path: '/operation-log', name: '操作日志' },
    ]
    if (!isLoggedIn.value) return []
    return items.filter((item) => canAccess(item.path))
  })

  async function login(username, password) {
    const res = await apiLogin(username, password)
    const data = res.data
    const t = data?.token
    const u = data?.user
    if (!t || !u) throw new Error('登录返回数据异常')
    token.value = t
    user.value = u
    localStorage.setItem(TOKEN_KEY, t)
    localStorage.setItem(USER_KEY, JSON.stringify(u))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  /** 从服务端刷新当前用户（如进入系统时恢复 session） */
  async function fetchMe() {
    if (!token.value) return
    try {
      const res = await getMe()
      const u = res.data
      if (u) {
        user.value = u
        localStorage.setItem(USER_KEY, JSON.stringify(u))
      }
    } catch (_) {
      logout()
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    role,
    canAccess,
    navItems,
    login,
    logout,
    fetchMe,
    rehydrateFromStorage,
  }
})
