<!-- 系统管理（系统设计文档 §2.1、§4.2）：用户列表、新增/编辑用户，密码脱敏 -->
<template>
  <div class="page system-page">
    <h2>系统管理</h2>
    <div class="toolbar">
      <button class="btn btn-primary" @click="openForm()">新增用户</button>
    </div>
    <table v-if="list.length" class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>真实姓名</th>
          <th>角色</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in list" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>{{ u.realName }}</td>
          <td>{{ u.role || '—' }}</td>
          <td>{{ u.enabled ? '启用' : '禁用' }}</td>
          <td>
            <button class="btn btn-sm" @click="openForm(u)">编辑</button>
            <button class="btn btn-sm btn-danger" @click="handleDelete(u)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-else class="empty">暂无用户</div>

    <!-- 新增/编辑用户弹窗 -->
    <div v-if="formVisible" class="modal-mask" @click.self="formVisible = false">
      <div class="modal">
        <h3>{{ form.id ? '编辑用户' : '新增用户' }}</h3>
        <form @submit.prevent="submitForm" class="form">
          <div class="form-row">
            <label>用户名 *</label>
            <input v-model="form.username" required class="input" :readonly="!!form.id" placeholder="登录名，创建后不可改" />
          </div>
          <div class="form-row">
            <label>密码 {{ form.id ? '（不填则不修改）' : '*' }}</label>
            <input v-model="form.password" type="password" :required="!form.id" class="input" autocomplete="new-password" />
          </div>
          <div class="form-row">
            <label>真实姓名</label>
            <input v-model="form.realName" class="input" />
          </div>
          <div class="form-row">
            <label>角色</label>
            <select v-model="form.role" class="input">
              <option value="">请选择</option>
              <option value="ADMIN">管理员</option>
              <option value="PHARMACIST">药师</option>
              <option value="PURCHASER">采购员</option>
              <option value="REVIEWER">审核员</option>
              <option value="DOCTOR">医生</option>
              <option value="NURSE">护士</option>
            </select>
          </div>
          <div class="form-row">
            <label><input type="checkbox" v-model="form.enabled" /> 启用</label>
          </div>
          <div class="form-actions">
            <button type="button" class="btn" @click="formVisible = false">取消</button>
            <button type="submit" class="btn btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, getUserById, createUser, updateUser, deleteUser } from '@/api/user'

const list = ref([])
const formVisible = ref(false)
const form = reactive({
  id: null, username: '', password: '', realName: '', role: '', enabled: true
})

function resetForm() {
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = ''
  form.enabled = true
}

async function load() {
  try {
    const res = await getUserList()
    list.value = res.data ?? []
  } catch (e) {
    list.value = []
  }
}

function openForm(user) {
  resetForm()
  if (user) {
    form.id = user.id
    form.username = user.username
    form.realName = user.realName || ''
    form.role = user.role || ''
    form.enabled = user.enabled !== false
  }
  formVisible.value = true
}

async function submitForm() {
  if (!form.username.trim()) {
    alert('请输入用户名')
    return
  }
  if (!form.id && !form.password) {
    alert('请输入密码')
    return
  }
  try {
    if (form.id) {
      const payload = {
        username: form.username,
        realName: form.realName || null,
        role: form.role || null,
        enabled: form.enabled
      }
      if (form.password) payload.password = form.password
      await updateUser(form.id, payload)
      alert('修改成功')
    } else {
      await createUser({
        username: form.username.trim(),
        password: form.password,
        realName: form.realName || null,
        role: form.role || null,
        enabled: form.enabled
      })
      alert('新增成功')
    }
    formVisible.value = false
    load()
  } catch (e) {
    alert(e.message || '保存失败')
  }
}

async function handleDelete(u) {
  if (!confirm(`确定删除用户「${u.realName || u.username}」？删除后不可恢复。`)) return
  try {
    await deleteUser(u.id)
    load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 900px; }
.toolbar { margin: 1rem 0; }
.btn { padding: 0.4rem 0.8rem; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; background: #f5f5f5; }
.btn-primary { background: #1a5fb4; color: #fff; border-color: #1a5fb4; }
.btn-sm { padding: 0.2rem 0.5rem; font-size: 0.9rem; }
.table { border-collapse: collapse; width: 100%; }
.table th, .table td { border: 1px solid #ddd; padding: 0.5rem; text-align: left; }
.table th { background: #f0f0f0; }
.empty { margin: 0.5rem 0; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; padding: 1.5rem; border-radius: 8px; max-width: 420px; width: 90%; }
.form-row { margin-bottom: 0.75rem; }
.form-row label { display: block; margin-bottom: 0.25rem; }
.form-row .input { width: 100%; box-sizing: border-box; padding: 0.35rem 0.5rem; border: 1px solid #ccc; border-radius: 4px; }
.form-actions { margin-top: 1rem; display: flex; gap: 0.5rem; justify-content: flex-end; }
.btn-danger { color: #c00; border-color: #c00; }
</style>
