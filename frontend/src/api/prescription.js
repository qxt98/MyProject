/** 处方审核接口（需求 3.4） */
import request from './request'

export function getPrescriptionList(params) {
  return request.get('/prescription', { params })
}

export function getPrescriptionById(id) {
  return request.get(`/prescription/${id}`)
}

export function getPrescriptionItems(id) {
  return request.get(`/prescription/${id}/items`)
}

export function createPrescription(data) {
  return request.post('/prescription', data)
}

export function addPrescriptionItem(data) {
  return request.post('/prescription/item', data)
}

export function submitPrescription(id) {
  return request.post(`/prescription/${id}/submit`)
}

export function approvePrescription(id, remark) {
  return request.post(`/prescription/${id}/approve`, null, { params: { remark } })
}

export function rejectPrescription(id, remark) {
  return request.post(`/prescription/${id}/reject`, null, { params: { remark } })
}
