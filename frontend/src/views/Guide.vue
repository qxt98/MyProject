<!-- 患者用药指导：未登录每 IP 每小时内限 3 次，仅供参考、请遵医嘱 -->
<template>
  <div class="page guide-page">
    <h2>患者用药指导</h2>
    <p class="tip">请输入您的问题或症状描述，系统将给出用药建议与风险提示。仅供参考，不能替代专业医疗建议，请遵医嘱。</p>
    <p v-if="remainingAsks !== null && !isLoggedIn" class="limit-tip">本小时内剩余提问次数：{{ remainingAsks }}（未登录用户限 3 次/小时，<router-link to="/login">登录</router-link>后不限）</p>
    <div class="card">
      <div class="form-row">
        <label>您的问题</label>
        <textarea v-model="question" class="input" rows="4" placeholder="例如：感冒发烧怎么办？服用某药后过敏怎么办？" maxlength="500"></textarea>
        <span class="char-hint">{{ question.length }}/500</span>
      </div>
      <button class="btn btn-primary" :disabled="loading" @click="ask">提交咨询</button>
    </div>
    <div v-if="limitError" class="card limit-msg">
      <p class="limit-err">{{ limitError }}</p>
      <p><router-link to="/login" class="login-link">登录后继续使用</router-link></p>
    </div>
    <div v-else-if="showReplyCard" class="card result">
      <h3>回复</h3>
      <div class="answer">{{ streamedAnswer }}<span v-if="loading" class="cursor">▌</span></div>
      <div v-if="result?.disclaimer" class="disclaimer">{{ result.disclaimer }}</div>
    </div>
    <p class="back-link"><router-link to="/login">工作人员请登录系统</router-link></p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { askGuideStream, askGuide } from '@/api/guide'

const question = ref('')
const loading = ref(false)
const result = ref(null)
const streamedAnswer = ref('')
const remainingAsks = ref(null)
const limitError = ref(null)

const isLoggedIn = computed(() => !!localStorage.getItem('smart_pharma_token'))
const showReplyCard = computed(() => result.value !== null || streamedAnswer.value !== '')

onMounted(() => {
  if (!isLoggedIn.value) remainingAsks.value = 3
})

async function ask() {
  const q = question.value?.trim()
  if (!q) {
    alert('请输入您的问题或症状描述')
    return
  }
  if (q.length > 500) {
    alert('问题长度不能超过 500 个字符')
    return
  }
  loading.value = true
  result.value = { answer: '', disclaimer: '' }
  streamedAnswer.value = ''
  limitError.value = null
  try {
    await askGuideStream(q, {
      onContent: (chunk) => {
        streamedAnswer.value += chunk
      },
      onDisclaimer: (text) => {
        if (result.value) result.value.disclaimer = text
      },
      onRemainingAsks: (n) => {
        remainingAsks.value = n
      },
      onError: (msg) => {
        if (msg.includes('仅可提问') || msg.includes('次数已用完')) {
          limitError.value = msg
          result.value = null
          streamedAnswer.value = ''
        } else {
          alert(msg || '请求失败')
          result.value = null
          streamedAnswer.value = ''
        }
      },
      onDone: () => {
        loading.value = false
      },
    })
  } catch (e) {
    loading.value = false
    result.value = null
    streamedAnswer.value = ''
    try {
      const res = await askGuide(q)
      if (res?.data) {
        result.value = { answer: res.data.answer || '', disclaimer: res.data.disclaimer || '' }
        streamedAnswer.value = res.data.answer || ''
        if (typeof res.data.remainingAsks === 'number') remainingAsks.value = res.data.remainingAsks
      }
    } catch (_) {
      alert(e.message || '请求失败')
    }
  }
}
</script>

<style scoped>
.page { max-width: 700px; }
.tip { color: #666; margin-bottom: 1rem; font-size: 0.95rem; }
.limit-tip { font-size: 0.9rem; color: #555; margin-bottom: 0.75rem; }
.limit-tip a { color: #1a5fb4; text-decoration: none; }
.limit-tip a:hover { text-decoration: underline; }
.card { border: 1px solid #ddd; border-radius: 8px; padding: 1.5rem; margin-bottom: 1rem; }
.form-row { margin-bottom: 1rem; }
.form-row label { display: block; margin-bottom: 0.25rem; font-weight: 500; }
.input { width: 100%; box-sizing: border-box; padding: 0.5rem; border: 1px solid #ccc; border-radius: 4px; }
.char-hint { font-size: 0.85rem; color: #888; }
.btn { padding: 0.5rem 1rem; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; background: #f5f5f5; }
.btn-primary { background: #1a5fb4; color: #fff; border-color: #1a5fb4; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.limit-msg { background: #fff8f0; border-color: #e0a060; }
.limit-err { color: #b35c00; margin: 0 0 0.5rem 0; }
.login-link { color: #1a5fb4; font-weight: 500; }
.result { background: #f9f9f9; }
.result h3 { margin-top: 0; }
.answer { white-space: pre-wrap; line-height: 1.6; margin-bottom: 1rem; }
.answer .cursor { animation: blink 0.8s step-end infinite; }
@keyframes blink { 50% { opacity: 0; } }
.disclaimer { font-size: 0.9rem; color: #888; border-top: 1px solid #eee; padding-top: 0.75rem; }
.back-link { margin-top: 1.5rem; text-align: center; font-size: 0.9rem; }
.back-link a { color: #1a5fb4; text-decoration: none; }
.back-link a:hover { text-decoration: underline; }
</style>
