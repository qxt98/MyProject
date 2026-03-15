/** 库存管理接口（需求 3.2）：入库、出库、流水、预警 */
import request from './request'

export function stockIn(data) {
  return request.post('/stock/in', data)
}

export function stockOut(data) {
  return request.post('/stock/out', data)
}

export function listStockIn(params) {
  return request.get('/stock/in/list', { params })
}

export function listStockOut(params) {
  return request.get('/stock/out/list', { params })
}

export function listStockByDrug(drugId) {
  return request.get('/stock/list', { params: { drugId } })
}

export function warnLow(threshold = 10) {
  return request.get('/stock/warn/low', { params: { threshold } })
}

export function warnExpiry(days = 180) {
  return request.get('/stock/warn/expiry', { params: { days } })
}
