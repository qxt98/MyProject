/**
 * 文件上传 API
 */
import request from './request'

/**
 * 上传图片文件
 * @param {File} file 图片文件
 * @returns {Promise<string>} 图片URL
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    if (res.code === 200) {
      return res.data
    } else {
      throw new Error(res.message || '上传失败')
    }
  })
}

/**
 * 删除图片文件
 * @param {string} fileUrl 图片URL
 * @returns {Promise<void>}
 */
export function deleteImage(fileUrl) {
  return request.delete('/upload/image', {
    params: { fileUrl }
  }).then(res => {
    if (res.code === 200) {
      return
    } else {
      throw new Error(res.message || '删除失败')
    }
  })
}