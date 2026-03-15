<!-- 库存管理（系统设计文档 §2.1、§4.2）：入库/出库、流水、按药品查库存、预警 -->
<template>
  <div class="page stock-page">
    <h2>库存管理</h2>

    <div class="tabs">
      <button :class="['tab', { active: tab === 'in' }]" @click="tab = 'in'">入库登记</button>
      <button :class="['tab', { active: tab === 'out' }]" @click="tab = 'out'">出库登记</button>
      <button :class="['tab', { active: tab === 'return' }]" @click="tab = 'return'">退库登记</button>
      <button :class="['tab', { active: tab === 'inList' }]" @click="tab = 'inList'; loadInList()">入库流水</button>
      <button :class="['tab', { active: tab === 'outList' }]" @click="tab = 'outList'; loadOutList()">出库流水</button>
      <button :class="['tab', { active: tab === 'byDrug' }]" @click="tab = 'byDrug'">按药品查库存</button>
      <button :class="['tab', { active: tab === 'warn' }]" @click="tab = 'warn'; loadWarn()">预警</button>
    </div>

    <!-- 入库登记 -->
    <section v-if="tab === 'in'" class="card">
      <h3>入库登记</h3>
      <form @submit.prevent="doStockIn" class="form">
        <div class="form-row">
          <label>药品 *</label>
          <select v-model="formIn.drugId" required class="input">
            <option value="">请选择</option>
            <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}（{{ d.dosageForm }}）</option>
          </select>
        </div>
        <div class="form-row">
          <label>批次号</label>
          <input v-model="formIn.batchNo" class="input" />
        </div>
        <div class="form-row">
          <label>数量 *</label>
          <input v-model.number="formIn.quantity" type="number" min="1" required class="input" />
        </div>
        <div class="form-row two-col">
          <div>
            <label>生产日期</label>
            <input v-model="formIn.productionDate" type="date" class="input" />
          </div>
          <div>
            <label>有效期至</label>
            <input v-model="formIn.expiryDate" type="date" class="input" />
          </div>
        </div>
        <div class="form-row">
          <label>备注</label>
          <input v-model="formIn.remark" class="input" />
        </div>
        <button type="submit" class="btn btn-primary">提交入库</button>
      </form>
    </section>

    <!-- 退库登记（需求 F-Stock-03：将已出库药品退回库存，调用入库接口并备注退库） -->
    <section v-if="tab === 'return'" class="card">
      <h3>退库登记</h3>
      <p class="tip">将已出库药品退回库存，提交后库存数量增加并记入入库流水（备注为退库）。</p>
      <form @submit.prevent="doStockReturn" class="form">
        <div class="form-row">
          <label>药品 *</label>
          <select v-model="formReturn.drugId" required class="input">
            <option value="">请选择</option>
            <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}（{{ d.dosageForm }}）</option>
          </select>
        </div>
        <div class="form-row">
          <label>批次号</label>
          <input v-model="formReturn.batchNo" class="input" />
        </div>
        <div class="form-row">
          <label>退回数量 *</label>
          <input v-model.number="formReturn.quantity" type="number" min="1" required class="input" />
        </div>
        <div class="form-row two-col">
          <div>
            <label>生产日期</label>
            <input v-model="formReturn.productionDate" type="date" class="input" />
          </div>
          <div>
            <label>有效期至</label>
            <input v-model="formReturn.expiryDate" type="date" class="input" />
          </div>
        </div>
        <div class="form-row">
          <label>退库原因/备注</label>
          <input v-model="formReturn.remark" class="input" placeholder="选填" />
        </div>
        <button type="submit" class="btn btn-primary">提交退库</button>
      </form>
    </section>

    <!-- 出库登记 -->
    <section v-if="tab === 'out'" class="card">
      <h3>出库登记</h3>
      <form @submit.prevent="doStockOut" class="form">
        <div class="form-row">
          <label>药品 *</label>
          <select v-model="formOut.drugId" required class="input">
            <option value="">请选择</option>
            <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>批次号</label>
          <input v-model="formOut.batchNo" class="input" placeholder="不填则按先进先出" />
        </div>
        <div class="form-row">
          <label>数量 *</label>
          <input v-model.number="formOut.quantity" type="number" min="1" required class="input" />
        </div>
        <div class="form-row">
          <label>备注</label>
          <input v-model="formOut.remark" class="input" />
        </div>
        <button type="submit" class="btn btn-primary">提交出库</button>
      </form>
    </section>

    <!-- 入库流水 -->
    <section v-if="tab === 'inList'" class="card">
      <h3>入库流水</h3>
      <div class="toolbar">
        <select v-model="filterIn.drugId" class="input" @change="loadInList()">
          <option value="">全部药品</option>
          <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}</option>
        </select>
        <button class="btn" @click="loadInList()">查询</button>
      </div>
      <table v-if="inList.length" class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>药品ID</th>
            <th>批次号</th>
            <th>数量</th>
            <th>生产日期</th>
            <th>有效期至</th>
            <th>入库时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in inList" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.drugId }}</td>
            <td>{{ r.batchNo }}</td>
            <td>{{ r.quantity }}</td>
            <td>{{ r.productionDate }}</td>
            <td>{{ r.expiryDate }}</td>
            <td>{{ r.createdAt }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无数据</div>
      <div v-if="inTotal > 0" class="pagination">
        <button class="btn btn-sm" :disabled="pageIn <= 0" @click="pageIn--; loadInList()">上一页</button>
        <span>第 {{ pageIn + 1 }} 页 / 共 {{ totalPagesIn }} 页</span>
        <button class="btn btn-sm" :disabled="pageIn >= totalPagesIn - 1" @click="pageIn++; loadInList()">下一页</button>
      </div>
    </section>

    <!-- 出库流水 -->
    <section v-if="tab === 'outList'" class="card">
      <h3>出库流水</h3>
      <div class="toolbar">
        <select v-model="filterOut.drugId" class="input" @change="loadOutList()">
          <option value="">全部药品</option>
          <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}</option>
        </select>
        <button class="btn" @click="loadOutList()">查询</button>
      </div>
      <table v-if="outList.length" class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>药品ID</th>
            <th>批次号</th>
            <th>数量</th>
            <th>出库时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in outList" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.drugId }}</td>
            <td>{{ r.batchNo }}</td>
            <td>{{ r.quantity }}</td>
            <td>{{ r.createdAt }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">暂无数据</div>
      <div v-if="outTotal > 0" class="pagination">
        <button class="btn btn-sm" :disabled="pageOut <= 0" @click="pageOut--; loadOutList()">上一页</button>
        <span>第 {{ pageOut + 1 }} 页 / 共 {{ totalPagesOut }} 页</span>
        <button class="btn btn-sm" :disabled="pageOut >= totalPagesOut - 1" @click="pageOut++; loadOutList()">下一页</button>
      </div>
    </section>

    <!-- 按药品查库存 -->
    <section v-if="tab === 'byDrug'" class="card">
      <h3>按药品查库存</h3>
      <div class="toolbar">
        <select v-model="queryDrugId" class="input">
          <option value="">请选择药品</option>
          <option v-for="d in drugList" :key="d.id" :value="d.id">{{ d.name }}</option>
        </select>
        <button class="btn btn-primary" @click="loadByDrug">查询</button>
      </div>
      <table v-if="stockByDrug.length" class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>药品ID</th>
            <th>批次号</th>
            <th>数量</th>
            <th>生产日期</th>
            <th>有效期至</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in stockByDrug" :key="s.id">
            <td>{{ s.id }}</td>
            <td>{{ s.drugId }}</td>
            <td>{{ s.batchNo }}</td>
            <td>{{ s.quantity }}</td>
            <td>{{ s.productionDate }}</td>
            <td>{{ s.expiryDate }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else-if="queryDrugId && !loadingByDrug" class="empty">该药品暂无库存记录</div>
    </section>

    <!-- 预警 -->
    <section v-if="tab === 'warn'" class="card">
      <h3>库存预警</h3>
      <div class="toolbar">
        <label>低库存阈值：<input v-model.number="lowThreshold" type="number" min="0" class="input narrow" /> 天内效期：<input v-model.number="expiryDays" type="number" min="1" class="input narrow" /></label>
        <button class="btn" @click="loadWarn()">刷新</button>
      </div>
      <h4>低库存预警（数量 ≤ {{ lowThreshold }}）</h4>
      <table v-if="warnLowList.length" class="table">
        <thead>
          <tr>
            <th>药品ID</th>
            <th>批次号</th>
            <th>当前数量</th>
            <th>有效期至</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in warnLowList" :key="s.id">
            <td>{{ s.drugId }}</td>
            <td>{{ s.batchNo }}</td>
            <td>{{ s.quantity }}</td>
            <td>{{ s.expiryDate }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">无低库存预警</div>
      <h4 style="margin-top:1rem">近效期预警（{{ expiryDays }} 天内到期）</h4>
      <table v-if="warnExpiryList.length" class="table">
        <thead>
          <tr>
            <th>药品ID</th>
            <th>批次号</th>
            <th>数量</th>
            <th>有效期至</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in warnExpiryList" :key="s.id">
            <td>{{ s.drugId }}</td>
            <td>{{ s.batchNo }}</td>
            <td>{{ s.quantity }}</td>
            <td>{{ s.expiryDate }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">无近效期预警</div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDrugList } from '@/api/drug'
import { stockIn, stockOut, listStockIn, listStockOut, listStockByDrug, warnLow, warnExpiry } from '@/api/stock'

const tab = ref('in')
const drugList = ref([])

const formIn = reactive({ drugId: '', batchNo: '', quantity: null, productionDate: '', expiryDate: '', remark: '' })
const formReturn = reactive({ drugId: '', batchNo: '', quantity: null, productionDate: '', expiryDate: '', remark: '' })
const formOut = reactive({ drugId: '', batchNo: '', quantity: null, remark: '' })

const inList = ref([])
const pageIn = ref(0)
const inTotal = ref(0)
const totalPagesIn = ref(0)
const filterIn = reactive({ drugId: '' })

const outList = ref([])
const pageOut = ref(0)
const outTotal = ref(0)
const totalPagesOut = ref(0)
const filterOut = reactive({ drugId: '' })

const queryDrugId = ref('')
const stockByDrug = ref([])
const loadingByDrug = ref(false)

const lowThreshold = ref(10)
const expiryDays = ref(180)
const warnLowList = ref([])
const warnExpiryList = ref([])

async function loadDrugs() {
  try {
    const res = await getDrugList({ page: 0, size: 500, includeDisabled: false })
    drugList.value = res.data?.content ?? []
  } catch (e) {
    console.error(e)
  }
}

async function doStockIn() {
  try {
    await stockIn({
      drugId: Number(formIn.drugId),
      batchNo: formIn.batchNo || null,
      quantity: formIn.quantity,
      productionDate: formIn.productionDate || null,
      expiryDate: formIn.expiryDate || null,
      remark: formIn.remark || null
    })
    alert('入库成功')
    formIn.drugId = ''
    formIn.batchNo = ''
    formIn.quantity = null
    formIn.productionDate = ''
    formIn.expiryDate = ''
    formIn.remark = ''
  } catch (e) {
    alert(e.message || '入库失败')
  }
}

async function doStockReturn() {
  try {
    const remark = formReturn.remark ? `退库：${formReturn.remark}` : '退库'
    await stockIn({
      drugId: Number(formReturn.drugId),
      batchNo: formReturn.batchNo || null,
      quantity: formReturn.quantity,
      productionDate: formReturn.productionDate || null,
      expiryDate: formReturn.expiryDate || null,
      remark
    })
    alert('退库成功')
    formReturn.drugId = ''
    formReturn.batchNo = ''
    formReturn.quantity = null
    formReturn.productionDate = ''
    formReturn.expiryDate = ''
    formReturn.remark = ''
  } catch (e) {
    alert(e.message || '退库失败')
  }
}

async function doStockOut() {
  try {
    await stockOut({
      drugId: Number(formOut.drugId),
      batchNo: formOut.batchNo || null,
      quantity: formOut.quantity,
      remark: formOut.remark || null
    })
    alert('出库成功')
    formOut.drugId = ''
    formOut.batchNo = ''
    formOut.quantity = null
    formOut.remark = ''
  } catch (e) {
    alert(e.message || '出库失败')
  }
}

async function loadInList() {
  try {
    const res = await listStockIn({
      drugId: filterIn.drugId || undefined,
      page: pageIn.value,
      size: 10
    })
    const data = res.data
    inList.value = data?.content ?? []
    inTotal.value = data?.totalElements ?? 0
    totalPagesIn.value = data?.totalPages ?? 0
  } catch (e) {
    inList.value = []
  }
}

async function loadOutList() {
  try {
    const res = await listStockOut({
      drugId: filterOut.drugId || undefined,
      page: pageOut.value,
      size: 10
    })
    const data = res.data
    outList.value = data?.content ?? []
    outTotal.value = data?.totalElements ?? 0
    totalPagesOut.value = data?.totalPages ?? 0
  } catch (e) {
    outList.value = []
  }
}

async function loadByDrug() {
  if (!queryDrugId.value) { alert('请选择药品'); return }
  loadingByDrug.value = true
  try {
    const res = await listStockByDrug(Number(queryDrugId.value))
    stockByDrug.value = res.data ?? []
  } catch (e) {
    stockByDrug.value = []
  }
  loadingByDrug.value = false
}

async function loadWarn() {
  try {
    const [lowRes, expRes] = await Promise.all([
      warnLow(lowThreshold.value),
      warnExpiry(expiryDays.value)
    ])
    warnLowList.value = lowRes.data ?? []
    warnExpiryList.value = expRes.data ?? []
  } catch (e) {
    warnLowList.value = []
    warnExpiryList.value = []
  }
}

onMounted(loadDrugs)
</script>

<style scoped>
.page { max-width: 1100px; }
.tabs { display: flex; gap: 0.25rem; flex-wrap: wrap; margin-bottom: 1rem; }
.tab { padding: 0.4rem 0.8rem; border: 1px solid #ccc; background: #f5f5f5; cursor: pointer; border-radius: 4px; }
.tab.active { background: #1a5fb4; color: #fff; border-color: #1a5fb4; }
.card { border: 1px solid #ddd; border-radius: 8px; padding: 1rem; margin-bottom: 1rem; }
.toolbar { margin-bottom: 0.75rem; display: flex; gap: 0.5rem; align-items: center; }
.input { padding: 0.35rem 0.5rem; border: 1px solid #ccc; border-radius: 4px; }
.input.narrow { width: 80px; }
.form-row { margin-bottom: 0.75rem; }
.form-row label { display: block; margin-bottom: 0.25rem; }
.form-row.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.btn { padding: 0.4rem 0.8rem; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; background: #f5f5f5; }
.btn-primary { background: #1a5fb4; color: #fff; border-color: #1a5fb4; }
.btn-sm { padding: 0.2rem 0.5rem; font-size: 0.9rem; }
.table { border-collapse: collapse; width: 100%; }
.table th, .table td { border: 1px solid #ddd; padding: 0.5rem; text-align: left; }
.table th { background: #f0f0f0; }
.tip { color: #666; font-size: 0.9rem; margin-bottom: 0.75rem; }
.empty { color: #666; margin: 0.5rem 0; }
.pagination { display: flex; gap: 0.5rem; align-items: center; margin-top: 0.5rem; }
</style>
