/** 患者用药指导接口（需求 3.5）：智能问答，仅供参考；支持流式回答 */
import request from './request'

const getAuthHeaders = () => {
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('smart_pharma_token') : null
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  return headers
}

export function askGuide(question) {
  return request.post('/guide/ask', { question })
}

function unquoteData(data) {
  if (typeof data !== 'string' || data.length < 2) return data
  if (data.startsWith('"') && data.endsWith('"')) {
    try {
      const parsed = JSON.parse(data)
      return typeof parsed === 'string' ? parsed : data
    } catch (_) { return data }
  }
  return data
}

/**
 * 流式咨询：边收边展示。回调：onContent(chunk)、onDisclaimer(text)、onRemainingAsks(n)、onError(msg)、onDone()
 */
export async function askGuideStream(question, { onContent, onDisclaimer, onRemainingAsks, onError, onDone }) {
  const baseURL = typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_BASE != null
    ? import.meta.env.VITE_API_BASE
    : '/api'
  const res = await fetch(`${baseURL}/guide/ask/stream`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ question }),
  })
  if (!res.ok) {
    const text = await res.text()
    onError?.(text || '请求失败')
    onDone?.()
    return
  }
  const reader = res.body?.getReader()
  if (!reader) {
    onError?.('不支持流式响应')
    onDone?.()
    return
  }
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      buffer = buffer.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
      const parts = buffer.split(/\n\n+/)
      buffer = parts.pop() || ''
      for (const part of parts) {
        let event = ''
        let dataLines = []
        for (const line of part.split('\n')) {
          const trimmed = line.trim()
          if (trimmed.startsWith('event:')) event = trimmed.slice(6).trim().toLowerCase()
          else if (trimmed.startsWith('data:')) dataLines.push(trimmed.slice(5).trimStart())
        }
        const rawData = dataLines.join('\n').trim()
        const data = rawData !== undefined ? unquoteData(rawData) : rawData
        if (event === 'content' && data !== undefined && data !== '') onContent?.(data)
        else if (event === 'message' && data !== undefined && data !== '') onContent?.(data)
        else if (event === 'disclaimer' && data !== undefined) onDisclaimer?.(data)
        else if (event === 'remainingasks' && data !== undefined) onRemainingAsks?.(parseInt(data, 10))
        else if (event === 'error' && data !== undefined) onError?.(data)
        else if (event === 'start') { /* 建立流，可忽略 */ }
        else if (data !== undefined && data !== '' && !event) onContent?.(data)
      }
    }
  } finally {
    reader.releaseLock?.()
  }
  onDone?.()
}
