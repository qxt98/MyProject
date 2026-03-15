/** 认证接口：登录、获取当前用户 */
import request from './request'

/** 登录，返回 { token, user } */
export function login(username, password) {
  return request.post('/auth/login', { username, password })
}

/** 获取当前登录用户（需携带 token） */
export function getMe() {
  return request.get('/auth/me')
}
