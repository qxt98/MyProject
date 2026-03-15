/**
 * 封装 Axios：统一 baseURL 为 /api，请求头自动携带 JWT，响应解析 Result，code≠200 时抛错
 */
import axios from 'axios'

const TOKEN_KEY = 'smart_pharma_token'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data && data.code !== undefined && data.code !== 200) {
      const err = new Error(data.message || '请求失败')
      err.code = data.code
      return Promise.reject(err)
    }
    return res.data
  },
  (err) => {
    // 仅 401（未认证/ token 失效）时清除登录态并需重新登录；403 为无权限，不应登出
    if (err.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem('smart_pharma_user')
    }
    return Promise.reject(err)
  }
)

export default request
