<!-- 药品信息管理（系统设计文档 §2.1、§4.2）：列表、分页、新增/编辑/删除 -->
<template>
  <div class="page drug-list">
    <h2>药品信息管理</h2>
    <div class="toolbar">
      <input v-model="query.name" placeholder="药品名称" class="input" />
      <input v-model="query.category" placeholder="类别" class="input" />
      <label><input type="checkbox" v-model="query.includeDisabled" /> 含停用</label>
      <button class="btn btn-primary" @click="load">查询</button>
      <button class="btn" @click="openForm()">新增药品</button>
    </div>
    <table v-if="list.length" class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>名称</th>
          <th>类别</th>
          <th>剂型规格</th>
          <th>采购价</th>
          <th>销售价</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in list" :key="d.id">
          <td>{{ d.id }}</td>
          <td>{{ d.name }}</td>
          <td>{{ d.category }}</td>
          <td>{{ d.dosageForm }}</td>
          <td>{{ d.purchasePrice }}</td>
          <td>{{ d.salePrice }}</td>
          <td>{{ d.disabled ? '停用' : '在用' }}</td>
          <td>
            <button class="btn btn-sm" @click="openForm(d)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(d.id)">删除</button>
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

    <!-- 新增/编辑弹窗 -->
    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <div class="modal">
        <h3>{{ form.id ? '编辑药品' : '新增药品' }}</h3>
        <form @submit.prevent="submitForm" class="form">
          <div class="form-row">
            <label>药品名称 *</label>
            <input v-model="form.name" required class="input" />
          </div>
          <div class="form-row">
            <label>类别</label>
            <input v-model="form.category" class="input" placeholder="如：西药、中药" />
          </div>
          <div class="form-row">
            <label>剂型规格</label>
            <input v-model="form.dosageForm" class="input" />
          </div>
          <div class="form-row">
            <label>适应症</label>
            <textarea v-model="form.indication" class="input" rows="2"></textarea>
          </div>
          <div class="form-row">
            <label>生产厂家</label>
            <input v-model="form.manufacturer" class="input" />
          </div>
          <div class="form-row">
            <label>供应商</label>
            <input v-model="form.supplier" class="input" />
          </div>
          <div class="form-row two-col">
            <div>
              <label>采购价</label>
              <input v-model.number="form.purchasePrice" type="number" step="0.01" class="input" />
            </div>
            <div>
              <label>销售价</label>
              <input v-model.number="form.salePrice" type="number" step="0.01" class="input" />
            </div>
          </div>
          <div class="form-row">
            <label>药品图片</label>
            <div>
              <input type="file" @change="handleFileUpload" accept="image/*" :disabled="uploading" class="input" />
              <div v-if="uploading" style="color: #1a5fb4; margin-top: 0.25rem;">上传中...</div>
              <div v-if="uploadError" style="color: #c00; margin-top: 0.25rem;">{{ uploadError }}</div>
            </div>
          </div>
          
          <div class="form-row" v-if="imagePreview || form.imageUrl">
            <label>图片预览</label>
            <div>
              <img 
                :src="imagePreview || form.imageUrl" 
                alt="药品图片预览" 
                style="max-width: 200px; max-height: 200px; border: 1px solid #ddd; border-radius: 4px; margin-top: 0.5rem;" 
              />
            </div>
          </div>
          
          <div class="form-row">
            <label>药品图片 URL（或手动输入）</label>
            <input v-model="form.imageUrl" class="input" placeholder="上传图片后自动填充，或手动输入图片URL" />
          </div>
          
          <div class="form-row">
            <label><input type="checkbox" v-model="form.disabled" /> 停用</label>
          </div>
          <div class="form-actions">
            <button type="button" class="btn" @click="formVisible = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="uploading">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDrugList, getDrugById, createDrug, updateDrug, deleteDrug } from '@/api/drug'
import { uploadImage } from '@/api/upload'

const query = reactive({ name: '', category: '', includeDisabled: false })
const list = ref([])
const page = ref(0)
const size = 20
const total = ref(0)
const totalPages = ref(0)
const formVisible = ref(false)
const form = reactive({
  id: null, name: '', category: '', dosageForm: '', indication: '', manufacturer: '', supplier: '',
  purchasePrice: null, salePrice: null, imageUrl: '', disabled: false
})
const uploading = ref(false)
const uploadError = ref('')
const selectedFile = ref(null)
const imagePreview = ref('')

function resetForm() {
  form.id = null
  form.name = ''
  form.category = ''
  form.dosageForm = ''
  form.indication = ''
  form.manufacturer = ''
  form.supplier = ''
  form.purchasePrice = null
  form.salePrice = null
  form.imageUrl = ''
  form.disabled = false
  uploading.value = false
  uploadError.value = ''
  selectedFile.value = null
  imagePreview.value = ''
}

function handleFileUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  if (!validTypes.includes(file.type)) {
    uploadError.value = '只支持图片文件格式: jpg, jpeg, png, gif, webp, bmp'
    return
  }
  
  // 验证文件大小（10MB）
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    uploadError.value = '文件大小不能超过10MB'
    return
  }
  
  selectedFile.value = file
  uploadError.value = ''
  
  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    imagePreview.value = e.target.result
  }
  reader.readAsDataURL(file)
  
  // 自动上传
  uploadFile(file)
}

async function uploadFile(file) {
  if (!file) return
  
  uploading.value = true
  uploadError.value = ''
  
  try {
    const imageUrl = await uploadImage(file)
    form.imageUrl = imageUrl
    uploadError.value = ''
  } catch (error) {
    uploadError.value = error.message || '上传失败'
    selectedFile.value = null
    imagePreview.value = ''
  } finally {
    uploading.value = false
  }
}

async function load() {
  try {
    const res = await getDrugList({
      name: query.name || undefined,
      category: query.category || undefined,
      includeDisabled: query.includeDisabled,
      page: page.value,
      size
    })
    const data = res.data
    list.value = data?.content ?? []
    total.value = data?.totalElements ?? 0
    totalPages.value = data?.totalPages ?? 0
  } catch (e) {
    console.error(e)
    list.value = []
  }
}

function openForm(drug) {
  resetForm()
  if (drug) {
    form.id = drug.id
    form.name = drug.name
    form.category = drug.category
    form.dosageForm = drug.dosageForm
    form.indication = drug.indication
    form.manufacturer = drug.manufacturer
    form.supplier = drug.supplier
    form.purchasePrice = drug.purchasePrice
    form.salePrice = drug.salePrice
    form.imageUrl = drug.imageUrl
    form.disabled = !!drug.disabled
    
    // 如果药品有图片URL，设置预览
    if (drug.imageUrl) {
      imagePreview.value = drug.imageUrl
    }
  }
  formVisible.value = true
}

async function submitForm() {
  try {
    const payload = {
      name: form.name,
      category: form.category || null,
      dosageForm: form.dosageForm || null,
      indication: form.indication || null,
      manufacturer: form.manufacturer || null,
      supplier: form.supplier || null,
      purchasePrice: form.purchasePrice,
      salePrice: form.salePrice,
      imageUrl: form.imageUrl || null,
      disabled: form.disabled
    }
    if (form.id) {
      await updateDrug(form.id, payload)
      alert('修改成功')
    } else {
      await createDrug(payload)
      alert('新增成功')
    }
    formVisible.value = false
    load()
  } catch (e) {
    alert(e.message || '保存失败')
  }
}

async function handleDelete(id) {
  if (!confirm('确定删除该药品？')) return
  try {
    await deleteDrug(id)
    load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; }
.toolbar { margin: 1rem 0; display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; }
.input { padding: 0.35rem 0.5rem; margin: 0 0.25rem; border: 1px solid #ccc; border-radius: 4px; }
.btn { padding: 0.4rem 0.8rem; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; background: #f5f5f5; }
.btn:hover { background: #eee; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #1a5fb4; color: #fff; border-color: #1a5fb4; }
.btn-primary:hover { background: #155a9e; }
.btn-sm { padding: 0.2rem 0.5rem; font-size: 0.9rem; }
.btn-danger { color: #c00; border-color: #c00; }
.table { border-collapse: collapse; width: 100%; margin-top: 0.5rem; }
.table th, .table td { border: 1px solid #ddd; padding: 0.5rem; text-align: left; }
.table th { background: #f0f0f0; }
.empty, .pagination { margin-top: 1rem; }
.pagination { display: flex; gap: 1rem; align-items: center; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; padding: 1.5rem; border-radius: 8px; max-width: 500px; width: 90%; max-height: 90vh; overflow: auto; }
.modal h3 { margin-top: 0; }
.form-row { margin-bottom: 0.75rem; }
.form-row label { display: block; margin-bottom: 0.25rem; font-weight: 500; }
.form-row .input, .form-row textarea { width: 100%; box-sizing: border-box; }
.form-row.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-actions { margin-top: 1rem; display: flex; gap: 0.5rem; justify-content: flex-end; }
</style>
