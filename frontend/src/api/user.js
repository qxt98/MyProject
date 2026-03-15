/** 系统用户接口（需求 3.6），认证登录见 api/auth.js */
import request from './request'

export function getUserList() {
  return request.get('/user')
}

export function getUserById(id) {
  return request.get(`/user/${id}`)
}

export function createUser(data) {
  return request.post('/user', data)
}

export function updateUser(id, data) {
  return request.put(`/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
