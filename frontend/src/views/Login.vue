<!-- 登录页：用户名密码登录，成功后写入 token 与用户信息并跳转首页 -->
<template>
  <div class="login-page">
    <div class="login-card">
      <h1>药品信息智能化管理系统</h1>
      <p class="subtitle">请使用您的账号登录</p>
      <form @submit.prevent="submit" class="form">
        <div class="form-row">
          <label>用户名</label>
          <input v-model="username" type="text" required autocomplete="username" class="input" placeholder="请输入用户名" />
        </div>
        <div class="form-row">
          <label>密码</label>
          <input v-model="password" type="password" required autocomplete="current-password" class="input" placeholder="请输入密码" />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="btn btn-primary" :disabled="loading">登录</button>
      </form>
      <p class="guide-link">
        <router-link to="/guide">患者用药指导（无需登录）</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(username.value.trim(), password.value)
    const redirect = router.currentRoute.value.query.redirect || '/'
    router.replace(redirect)
  } catch (e) {
    error.value = e.message || '登录失败，请检查用户名和密码'
  }
  loading.value = false
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a5fb4 0%, #3584e4 100%);
}
.login-card {
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  width: 100%;
  max-width: 380px;
}
.login-card h1 {
  margin: 0 0 0.25rem;
  font-size: 1.35rem;
  text-align: center;
  color: #1a1a1a;
}
.subtitle {
  margin: 0 0 1.5rem;
  text-align: center;
  color: #666;
  font-size: 0.95rem;
}
.guide-link {
  margin: 1.25rem 0 0;
  text-align: center;
  font-size: 0.9rem;
}
.guide-link a {
  color: #1a5fb4;
  text-decoration: none;
}
.guide-link a:hover { text-decoration: underline; }
.form-row {
  margin-bottom: 1rem;
}
.form-row label {
  display: block;
  margin-bottom: 0.35rem;
  font-weight: 500;
  color: #333;
}
.input {
  width: 100%;
  box-sizing: border-box;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 1rem;
}
.input:focus {
  outline: none;
  border-color: #1a5fb4;
}
.error {
  color: #c00;
  font-size: 0.9rem;
  margin: 0 0 0.75rem;
}
.btn {
  width: 100%;
  padding: 0.6rem;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.btn-primary {
  background: #1a5fb4;
  color: #fff;
}
.btn-primary:hover:not(:disabled) {
  background: #155a9e;
}
.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
