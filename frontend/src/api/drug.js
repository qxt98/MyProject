/** 药品信息管理 API（需求 3.1） */
import request from './request'

export function getDrugList(params) {
  return request.get('/drugs', { params })
}

export function getDrugById(id) {
  return request.get(`/drugs/${id}`)
}

export function createDrug(data) {
  return request.post('/drugs', data)
}

export function updateDrug(id, data) {
  return request.put(`/drugs/${id}`, data)
}

export function deleteDrug(id) {
  return request.delete(`/drugs/${id}`)
}
