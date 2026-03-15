/** 操作日志接口（需求 F-Sys-04），仅管理员可访问 */
import request from './request'

export function getOperationLogList(params) {
  return request.get('/operation-log', { params })
}
