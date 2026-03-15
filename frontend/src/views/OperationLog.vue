<!-- 操作日志（需求 F-Sys-04）：按模块、用户、分页查询，仅管理员 -->
<template>
  <div class="page log-page">
    <h2>操作日志</h2>
    <div class="toolbar">
      <select v-model="filterModule" class="input" @change="load()">
        <option value="">全部模块</option>
        <option value="认证">认证</option>
        <option value="药品信息">药品信息</option>
        <option value="库存管理">库存管理</option>
        <option value="采购审批">采购审批</option>
        <option value="处方审核">处方审核</option>
        <option value="系统管理">系统管理</option>
      </select>
      <button class="btn" @click="load()">查询</button>
    </div>
    <table v-if="list.length" class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>用户ID</th>
          <th>模块</th>
          <th>操作</th>
          <th>摘要</th>
          <th>IP</th>
          <th>时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="log in list" :key="log.id">
          <td>{{ log.id }}</td>
          <td>{{ log.userId ?? '—' }}</td>
          <td>{{ log.module }}</td>
          <td>{{ log.action }}</td>
          <td>{{ log.detail }}</td>
          <td>{{ log.ip ?? '—' }}</td>
          <td>{{ log.createdAt }}</td>
        </tr>
      </tbody>
    </table>
    <div v-else class="empty">暂无日志</div>
    <div v-if="total > 0" class="pagination">
      <button class="btn btn-sm" :disabled="page <= 0" @click="page--; load()">上一页</button>
      <span>第 {{ page + 1 }} 页 / 共 {{ totalPages }} 页（{{ total }} 条）</span>
      <button class="btn btn-sm" :disabled="page >= totalPages - 1" @click="page++; load()">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOperationLogList } from '@/api/operationLog'

const list = ref([])
const page = ref(0)
const size = 20
const total = ref(0)
const totalPages = ref(0)
const filterModule = ref('')

async function load() {
  try {
    const res = await getOperationLogList({
      module: filterModule.value || undefined,
      page: page.value,
      size
    })
    const data = res.data
    list.value = data?.content ?? []
    total.value = data?.totalElements ?? 0
    totalPages.value = data?.totalPages ?? 0
  } catch (e) {
    list.value = []
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1100px; }
.toolbar { margin: 1rem 0; display: flex; gap: 0.5rem; align-items: center; }
.input { padding: 0.35rem 0.5rem; border: 1px solid #ccc; border-radius: 4px; }
.btn { padding: 0.4rem 0.8rem; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; background: #f5f5f5; }
.btn-sm { padding: 0.2rem 0.5rem; font-size: 0.9rem; }
.table { border-collapse: collapse; width: 100%; }
.table th, .table td { border: 1px solid #ddd; padding: 0.5rem; text-align: left; font-size: 0.9rem; }
.table th { background: #f0f0f0; }
.table td:nth-child(5) { max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty { margin: 0.5rem 0; }
.pagination { display: flex; gap: 0.5rem; align-items: center; margin-top: 0.5rem; }
</style>
