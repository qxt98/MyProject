<!-- 采购审批（系统设计文档 §2.1、§4.2）：列表、新增申请、审批/驳回 -->
<template>
  <div class="page purchase-page">
    <h2>采购审批</h2>
    <div class="toolbar">
      <select v-model="filterStatus" class="input" @change="load()">
        <option value="">全部状态</option>
        <option value="PENDING">待审批</option>
        <option value="APPROVED">已通过</option>
        <option value="REJECTED">已驳回</option>
      </select>
      <button class="btn" @click="load()">查询</button>
      <button class="btn btn-primary" @click="openForm()">新建申请</button>
    </div>
    <table v-if="list.length" class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>药品</th>
          <th>数量</th>
          <th>供应商</th>
          <th>预算金额</th>
          <th>状态</th>
          <th>申请时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in list" :key="p.id">
          <td>{{ p.id }}</td>
          <td>{{ drugName(p.drugId) }}</td>
          <td>{{ p.quantity }}</td>
          <td>{{ p.supplier }}</td>
          <td>{{ p.budgetAmount }}</td>
          <td>{{ statusText(p.status) }}</td>
          <td>{{ p.createdAt }}</td>
          <td>
            <template v-if="p.status === 'PENDING' && canApprove">
              <button class="btn btn-sm btn-primary" @click="approve(p.id)">通过</button>
              <button class="btn btn-sm btn-danger" @click="openReject(p)">驳回</button>
            </template>
            <span v-else>—</span>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-else class="empty">暂无数据</div>
    <div v-if="total > 0" class="pagination">
      <button class="btn btn-sm" :disabled="page <= 0" @click="page--; load()">上一页</button>
      <span>第 {{ page + 1 }} 页 / 共 {{ totalPages }} 页（{{ total }} 条）</span>
      <button class="btn btn-sm" :disabled="page >= totalPages - 1" @click="page++; load()">下一页</button>
    </div>

    <!-- 新建申请弹窗 -->
    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <div class="modal">
        <h3>新建采购申请</h3>
        <form @submit.prevent="submitForm" class="form">
          <div class="form-row">
            <label>药品 *</label>
            <select v-model="form.drugId" required class="input">
              <option value="">请选择</option>
              <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>数量 *</label>
            <input v-model.number="form.quantity" type="number" min="1" required class="input" />
          </div>
          <div class="form-row">
            <label>供应商</label>
            <input v-model="form.supplier" class="input" />
          </div>
          <div class="form-row">
            <label>预算金额</label>
            <input v-model.number="form.budgetAmount" type="number" step="0.01" class="input" />
          </div>
          <div class="form-row">
            <label>申请理由</label>
            <textarea v-model="form.reason" class="input" rows="3"></textarea>
          </div>
          <div class="form-actions">
            <button type="button" class="btn" @click="formVisible = false">取消</button>
            <button type="submit" class="btn btn-primary">提交</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 驳回理由弹窗 -->
    <div v-if="rejectVisible" class="modal-mask" @click.self="rejectVisible = false">
      <div class="modal">
        <h3>驳回采购申请</h3>
        <div class="form-row">
          <label>驳回原因 *</label>
          <textarea v-model="rejectReason" class="input" rows="3" placeholder="请填写驳回原因"></textarea>
        </div>
        <div class="form-actions">
          <button type="button" class="btn" @click="rejectVisible = false">取消</button>
          <button type="button" class="btn btn-danger" @click="doReject">确认驳回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getDrugList } from '@/api/drug'
import { getPurchaseList, createPurchase, approvePurchase, rejectPurchase } from '@/api/purchase'

const authStore = useAuthStore()
/** 仅管理员、审核员可审批（7.3 改进：按动作细分） */
const canApprove = computed(() => ['ADMIN', 'REVIEWER'].includes(authStore.role || ''))

const list = ref([])
const drugList = ref([])
const page = ref(0)
const size = 10
const total = ref(0)
const totalPages = ref(0)
const filterStatus = ref('')
const formVisible = ref(false)
const form = reactive({ drugId: '', quantity: null, supplier: '', budgetAmount: null, reason: '' })
const rejectVisible = ref(false)
const rejectId = ref(null)
const rejectReason = ref('')
const approverId = 1

function statusText(s) {
  const m = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }
  return m[s] || s
}

function drugName(id) {
  const d = drugList.value.find(x => x.id === id)
  return d ? d.name : id
}

async function load() {
  try {
    const res = await getPurchaseList({
      status: filterStatus.value || undefined,
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

async function loadDrugs() {
  try {
    const res = await getDrugList({ page: 0, size: 500, includeDisabled: false })
    drugList.value = res.data?.content ?? []
  } catch (e) {}
}

function openForm() {
  form.drugId = ''
  form.quantity = null
  form.supplier = ''
  form.budgetAmount = null
  form.reason = ''
  formVisible.value = true
}

async function submitForm() {
  try {
    await createPurchase({
      drugId: Number(form.drugId),
      quantity: form.quantity,
      supplier: form.supplier || null,
      budgetAmount: form.budgetAmount,
      reason: form.reason || null
    })
    alert('提交成功')
    formVisible.value = false
    load()
  } catch (e) {
    alert(e.message || '提交失败')
  }
}

async function approve(id) {
  if (!confirm('确定通过该采购申请？')) return
  try {
    await approvePurchase(id, approverId)
    load()
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

function openReject(p) {
  rejectId.value = p.id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() {
  if (!rejectReason.value.trim()) {
    alert('请填写驳回原因')
    return
  }
  try {
    await rejectPurchase(rejectId.value, approverId, rejectReason.value.trim())
    rejectVisible.value = false
    load()
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

onMounted(() => { loadDrugs(); load() })
</script>

<style scoped>
.page { max-width: 1100px; }
.toolbar { margin: 1rem 0; display: flex; gap: 0.5rem; align-items: center; }
.input { padding: 0.35rem 0.5rem; border: 1px solid #ccc; border-radius: 4px; }
.btn { padding: 0.4rem 0.8rem; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; background: #f5f5f5; }
.btn-primary { background: #1a5fb4; color: #fff; border-color: #1a5fb4; }
.btn-sm { padding: 0.2rem 0.5rem; font-size: 0.9rem; }
.btn-danger { color: #c00; border-color: #c00; }
.table { border-collapse: collapse; width: 100%; }
.table th, .table td { border: 1px solid #ddd; padding: 0.5rem; text-align: left; }
.table th { background: #f0f0f0; }
.empty { margin: 0.5rem 0; }
.pagination { display: flex; gap: 0.5rem; align-items: center; margin-top: 0.5rem; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; padding: 1.5rem; border-radius: 8px; max-width: 480px; width: 90%; }
.form-row { margin-bottom: 0.75rem; }
.form-row label { display: block; margin-bottom: 0.25rem; }
.form-row .input, .form-row textarea { width: 100%; box-sizing: border-box; }
.form-actions { margin-top: 1rem; display: flex; gap: 0.5rem; justify-content: flex-end; }
</style>
