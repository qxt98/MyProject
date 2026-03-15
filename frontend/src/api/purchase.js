/** 采购审批接口（需求 3.3） */
import request from './request'

export function getPurchaseList(params) {
  return request.get('/purchase', { params })
}

export function getPurchaseById(id) {
  return request.get(`/purchase/${id}`)
}

export function createPurchase(data) {
  return request.post('/purchase', data)
}

export function approvePurchase(id, approverId = 1) {
  return request.post(`/purchase/${id}/approve`, null, { params: { approverId } })
}

export function rejectPurchase(id, approverId = 1, rejectReason) {
  return request.post(`/purchase/${id}/reject`, null, { params: { approverId, rejectReason } })
}
