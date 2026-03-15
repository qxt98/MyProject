<template>
  <div id="app">
    <header class="app-header" v-if="authStore.isLoggedIn">
      <h1>药品信息智能化管理系统</h1>
      <nav>
        <router-link v-for="item in authStore.navItems" :key="item.path" :to="item.path">{{ item.name }}</router-link>
      </nav>
      <div class="user-bar">
        <span class="user-name">{{ authStore.user?.realName || authStore.user?.username }}</span>
        <span class="user-role">({{ roleLabel }})</span>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(() => {
  if (authStore.isLoggedIn) authStore.fetchMe()
})

const roleLabels = {
  ADMIN: '管理员',
  PHARMACIST: '药师',
  PURCHASER: '采购员',
  REVIEWER: '审核员',
  DOCTOR: '医生',
  NURSE: '护士',
}
const roleLabel = computed(() => roleLabels[authStore.role] || authStore.role || '—')

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
  * { box-sizing: border-box; }
  body { margin: 0; font-family: 'Microsoft YaHei', sans-serif; }
  #app { min-height: 100vh; display: flex; flex-direction: column; }
  .app-header {
    background: #1a5fb4;
    color: #fff;
    padding: 0.75rem 1.5rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  .app-header h1 { margin: 0; font-size: 1.25rem; }
  .app-header nav { display: flex; gap: 1rem; flex-wrap: wrap; }
  .app-header a { color: #fff; text-decoration: none; }
  .app-header a.router-link-active { text-decoration: underline; }
  .user-bar {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  .user-name { font-weight: 500; }
  .user-role { font-size: 0.9rem; opacity: 0.9; }
  .btn-logout {
    margin-left: 0.5rem;
    padding: 0.25rem 0.6rem;
    font-size: 0.9rem;
    background: rgba(255,255,255,0.2);
    border: 1px solid rgba(255,255,255,0.5);
    color: #fff;
    border-radius: 4px;
    cursor: pointer;
  }
  .btn-logout:hover { background: rgba(255,255,255,0.3); }
  .app-main { flex: 1; padding: 1.5rem; }
</style>
