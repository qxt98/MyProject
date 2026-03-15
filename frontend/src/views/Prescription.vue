<!-- 处方审核（系统设计文档 §2.1、§4.2）：列表、新建处方与明细、提交、审核通过/驳回 -->
<template>
  <div class="page prescription-page">
    <h2>处方审核</h2>
    <div class="toolbar">
      <input v-model="filterPatientName" class="input" placeholder="患者姓名" />
      <input v-model="filterDoctorName" class="input" placeholder="开方医生" />
      <select v-model="filterStatus" class="input" @change="load()">
        <option value="">全部状态</option>
        <option value="DRAFT">草稿</option>
        <option value="SUBMITTED">已提交</option>
        <option value="APPROVED">已通过</option>
        <option value="REJECTED">已驳回</option>
      </select>
      <button class="btn" @click="load()">查询</button>
      <button v-if="!isPrescriptionReadOnly" class="btn btn-primary" @click="openCreate()">新建处方</button>
    </div>
    <table v-if="list.length" class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>患者姓名</th>
          <th>开方医生</th>
          <th>患者病情</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in list" :key="p.id">
          <td>{{ p.id }}</td>
          <td>{{ p.patientName }}</td>
          <td>{{ p.doctorName }}</td>
          <td class="condition-cell">{{ (p.patientCondition || '—').slice(0, 30) }}{{ (p.patientCondition && p.patientCondition.length > 30) ? '…' : '' }}</td>
          <td>{{ statusText(p.status) }}</td>
          <td>{{ p.createdAt }}</td>
          <td>
            <button class="btn btn-sm" @click="openDetail(p)">明细</button>
            <template v-if="!isPrescriptionReadOnly">
              <template v-if="p.status === 'DRAFT'">
                <button class="btn btn-sm" @click="openItems(p)">编辑明细</button>
                <button class="btn btn-sm btn-primary" @click="submit(p.id)">提交</button>
              </template>
              <template v-else-if="p.status === 'SUBMITTED' && canApprovePrescription">
                <button class="btn btn-sm btn-primary" @click="openApprove(p)">通过</button>
                <button class="btn btn-sm btn-danger" @click="openReject(p)">驳回</button>
              </template>
            </template>
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

    <!-- 新建处方 -->
    <div v-if="createVisible" class="modal-mask" @click.self="createVisible = false">
      <div class="modal">
        <h3>新建处方</h3>
        <form @submit.prevent="doCreate" class="form">
          <div class="form-row">
            <label>患者姓名 *</label>
            <input v-model="form.patientName" required class="input" />
          </div>
          <div class="form-row">
            <label>开方医生 *</label>
            <input v-model="form.doctorName" required class="input" />
          </div>
          <div class="form-row">
            <label>患者病情信息</label>
            <textarea v-model="form.patientCondition" class="input" rows="3" placeholder="主诉、诊断等，供审核参考"></textarea>
          </div>
          <div class="form-actions">
            <button type="button" class="btn" @click="createVisible = false">取消</button>
            <button type="submit" class="btn btn-primary">创建</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 处方明细（查看/编辑） -->
    <div v-if="itemsVisible" class="modal-mask" @click.self="itemsVisible = false">
      <div class="modal modal-lg">
        <h3>处方明细 — 处方 #{{ currentPrescription?.id }}</h3>
        <div class="detail-meta" v-if="currentPrescription">
          <p><strong>患者：</strong>{{ currentPrescription.patientName }} &nbsp; <strong>开方医生：</strong>{{ currentPrescription.doctorName }}</p>
          <p><strong>患者病情信息：</strong>{{ currentPrescription.patientCondition || '—' }}</p>
        </div>
        <table v-if="items.length" class="table">
          <thead>
            <tr>
              <th>药品</th>
              <th>剂量</th>
              <th>给药途径</th>
              <th>用药时长</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="it in items" :key="it.id">
              <td>{{ drugName(it.drugId) }}</td>
              <td>{{ it.dosage }}</td>
              <td>{{ it.route }}</td>
              <td>{{ it.duration }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty">暂无明细</div>
        <div v-if="currentPrescription?.status === 'DRAFT' && !isPrescriptionReadOnly" class="add-item">
          <h4>添加药品</h4>
          <p v-if="!drugList.length" class="hint">正在加载药品列表…若无数据请确认已登录且具有药品查看权限。</p>
          <div class="form-row">
            <select v-model="newItem.drugId" class="input">
              <option value="">请选择药品</option>
              <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}</option>
            </select>
            <input v-model="newItem.dosage" class="input" placeholder="剂量" />
            <input v-model="newItem.route" class="input" placeholder="给药途径" />
            <input v-model="newItem.duration" class="input" placeholder="用药时长" />
            <button type="button" class="btn btn-primary" @click="addItem">添加</button>
          </div>
        </div>
        <div class="form-actions" style="margin-top:1rem">
          <button type="button" class="btn" @click="itemsVisible = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 详情（只读，含患者/医生/状态/审核意见、处方药品明细） -->
    <div v-if="detailVisible" class="modal-mask" @click.self="detailVisible = false">
      <div class="modal modal-lg">
        <h3>处方详情 #{{ currentPrescription?.id }}</h3>
        <div class="detail-meta">
          <p><strong>患者：</strong>{{ currentPrescription?.patientName }} &nbsp; <strong>开方医生：</strong>{{ currentPrescription?.doctorName }}</p>
          <p><strong>患者病情信息：</strong>{{ currentPrescription?.patientCondition || '—' }}</p>
          <p><strong>状态：</strong>{{ statusText(currentPrescription?.status) }} &nbsp; <strong>创建时间：</strong>{{ currentPrescription?.createdAt || '—' }}</p>
          <p v-if="currentPrescription?.reviewRemark"><strong>审核意见：</strong>{{ currentPrescription.reviewRemark }}</p>
        </div>
        <section class="detail-items">
          <h4 class="detail-subtitle">处方药品</h4>
          <table v-if="items.length" class="table">
            <thead>
              <tr>
                <th>序号</th>
                <th>药品名称</th>
                <th>剂量</th>
                <th>给药途径</th>
                <th>用药时长</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(it, idx) in items" :key="it.id">
                <td>{{ idx + 1 }}</td>
                <td>{{ drugName(it.drugId) }}</td>
                <td>{{ it.dosage || '—' }}</td>
                <td>{{ it.route || '—' }}</td>
                <td>{{ it.duration || '—' }}</td>
              </tr>
            </tbody>
          </table>
          <div v-else class="empty">暂无药品明细</div>
        </section>
        <div class="form-actions" style="margin-top:1rem">
          <button type="button" class="btn" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 审核通过 -->
    <div v-if="approveVisible" class="modal-mask" @click.self="approveVisible = false">
      <div class="modal">
        <h3>审核通过</h3>
        <div class="form-row">
          <label>审核意见（选填）</label>
          <textarea v-model="approveRemark" class="input" rows="2"></textarea>
        </div>
        <div class="form-actions">
          <button type="button" class="btn" @click="approveVisible = false">取消</button>
          <button type="button" class="btn btn-primary" @click="doApprove">确认通过</button>
        </div>
      </div>
    </div>

    <!-- 驳回 -->
    <div v-if="rejectVisible" class="modal-mask" @click.self="rejectVisible = false">
      <div class="modal">
        <h3>驳回处方</h3>
        <div class="form-row">
          <label>驳回原因 *</label>
          <textarea v-model="rejectRemark" class="input" rows="3" placeholder="请填写驳回原因"></textarea>
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
import {
  getPrescriptionList,
  getPrescriptionById,
  getPrescriptionItems,
  createPrescription,
  addPrescriptionItem,
  submitPrescription,
  approvePrescription,
  rejectPrescription
} from '@/api/prescription'

const authStore = useAuthStore()
/** 护士仅可查看处方，不可新建/编辑/提交/审核（7.3 改进） */
const isPrescriptionReadOnly = computed(() => authStore.role === 'NURSE')
/** 仅管理员、审核员可审核处方 */
const canApprovePrescription = computed(() => ['ADMIN', 'REVIEWER'].includes(authStore.role || ''))

const list = ref([])
const drugList = ref([])
const page = ref(0)
const size = 10
const total = ref(0)
const totalPages = ref(0)
const filterPatientName = ref('')
const filterDoctorName = ref('')
const filterStatus = ref('')
const form = reactive({ patientName: '', doctorName: '', patientCondition: '' })
const createVisible = ref(false)
const currentPrescription = ref(null)
const items = ref([])
const itemsVisible = ref(false)
const detailVisible = ref(false)
const newItem = reactive({ drugId: '', dosage: '', route: '', duration: '' })
const approveVisible = ref(false)
const rejectVisible = ref(false)
const approveRemark = ref('')
const rejectRemark = ref('')

function statusText(s) {
  const m = { DRAFT: '草稿', SUBMITTED: '已提交', APPROVED: '已通过', REJECTED: '已驳回' }
  return m[s] || s
}

function drugName(id) {
  const d = drugList.value.find(x => x.id === id)
  return d ? d.name : id
}

async function load() {
  try {
    const res = await getPrescriptionList({
      patientName: filterPatientName.value || undefined,
      doctorName: filterDoctorName.value || undefined,
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

function openCreate() {
  form.patientName = ''
  form.doctorName = ''
  form.patientCondition = ''
  createVisible.value = true
}

async function doCreate() {
  try {
    await createPrescription({ patientName: form.patientName, doctorName: form.doctorName, patientCondition: form.patientCondition || undefined })
    createVisible.value = false
    load()
  } catch (e) {
    alert(e.message || '创建失败')
  }
}

async function openDetail(p) {
  currentPrescription.value = p
  items.value = []
  itemsVisible.value = false
  try {
    const res = await getPrescriptionById(p.id)
    // 接口返回 { code, data, message }，详情在 data 里
    currentPrescription.value = res?.data != null ? res.data : res || p
    const itemRes = await getPrescriptionItems(p.id)
    const list = itemRes?.data ?? itemRes
    items.value = Array.isArray(list) ? list : []
  } catch (e) {
    items.value = []
  }
  detailVisible.value = true
}

async function openItems(p) {
  currentPrescription.value = p
  try {
    const presRes = await getPrescriptionById(p.id)
    currentPrescription.value = presRes?.data != null ? presRes.data : presRes || p
    const res = await getPrescriptionItems(p.id)
    items.value = res?.data ?? res ?? []
  } catch (e) {
    items.value = []
  }
  newItem.drugId = ''
  newItem.dosage = ''
  newItem.route = ''
  newItem.duration = ''
  if (!drugList.value.length) await loadDrugs()
  itemsVisible.value = true
}

async function addItem() {
  if (!currentPrescription.value || !newItem.drugId) {
    alert('请选择药品')
    return
  }
  try {
    await addPrescriptionItem({
      prescriptionId: currentPrescription.value.id,
      drugId: Number(newItem.drugId),
      dosage: newItem.dosage || null,
      route: newItem.route || null,
      duration: newItem.duration || null
    })
    const res = await getPrescriptionItems(currentPrescription.value.id)
    items.value = res.data ?? []
    newItem.drugId = ''
    newItem.dosage = ''
    newItem.route = ''
    newItem.duration = ''
  } catch (e) {
    alert(e.message || '添加失败')
  }
}

async function submit(id) {
  if (!confirm('确定提交该处方？')) return
  try {
    await submitPrescription(id)
    load()
  } catch (e) {
    alert(e.message || '提交失败')
  }
}

function openApprove(p) {
  currentPrescription.value = p
  approveRemark.value = ''
  approveVisible.value = true
}

async function doApprove() {
  try {
    await approvePrescription(currentPrescription.value.id, approveRemark.value || undefined)
    approveVisible.value = false
    load()
  } catch (e) {
    alert(e.message || '操作失败')
  }
}

function openReject(p) {
  currentPrescription.value = p
  rejectRemark.value = ''
  rejectVisible.value = true
}

async function doReject() {
  if (!rejectRemark.value.trim()) {
    alert('请填写驳回原因')
    return
  }
  try {
    await rejectPrescription(currentPrescription.value.id, rejectRemark.value.trim())
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
.condition-cell { max-width: 180px; overflow: hidden; text-overflow: ellipsis; }
.empty { margin: 0.5rem 0; }
.pagination { display: flex; gap: 0.5rem; align-items: center; margin-top: 0.5rem; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; padding: 1.5rem; border-radius: 8px; max-width: 480px; width: 90%; max-height: 90vh; overflow: auto; }
.modal.modal-lg { max-width: 700px; }
.detail-meta p { margin: 0.35rem 0; }
.detail-items { margin-top: 1rem; padding: 0.75rem; border: 1px solid #ddd; border-radius: 6px; background: #fafafa; }
.detail-subtitle { margin: 0 0 0.5rem; font-size: 1rem; color: #333; }
.form-row { margin-bottom: 0.75rem; }
.form-row label { display: block; margin-bottom: 0.25rem; }
.form-row .input, .form-row textarea { width: 100%; box-sizing: border-box; }
.add-item .form-row { display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; }
.add-item .form-row .input { flex: 1; min-width: 80px; }
.form-actions { margin-top: 1rem; display: flex; gap: 0.5rem; justify-content: flex-end; }
.hint { font-size: 0.9rem; color: #666; margin: 0.25rem 0 0.5rem; }
</style>
