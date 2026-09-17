<template>
  <div class="user-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="toolbar">
          <span class="title">用户管理</span>
          <div class="toolbar-right">
            <el-input
              v-model="keyword"
              class="search-input"
              clearable
              placeholder="搜索用户名/昵称/手机"
              @keyup.enter="loadData"
              @clear="loadData"
            >
              <template #append>
                <el-button @click="loadData">搜索</el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="openDialog()">
              <el-icon><Plus /></el-icon>新增
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="nickName" label="昵称" width="120" />
        <el-table-column prop="deptName" label="部门" min-width="120" />
        <el-table-column prop="roleNames" label="角色" min-width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="warning" @click="openRoleDialog(row)">分配角色</el-button>
              <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新增用户'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" filterable clearable style="width:100%" placeholder="选择部门">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width:100%">
            <el-option :value="1" label="正常" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleVisible" title="分配角色" width="480px" destroy-on-close>
      <p class="hint">为用户「{{ currentUser?.nickName || currentUser?.userName }}」选择角色</p>
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in roles" :key="role.id" :label="role.id">
          {{ role.roleName }}（{{ role.roleCode }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSaving" @click="saveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const depts = ref([])
const roles = ref([])
const loading = ref(false)
const saving = ref(false)
const roleSaving = ref(false)
const visible = ref(false)
const roleVisible = ref(false)
const keyword = ref('')
const formRef = ref()
const currentUser = ref(null)
const selectedRoleIds = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({
  id: null,
  userName: '',
  nickName: '',
  deptId: null,
  phone: '',
  email: '',
  password: '',
  status: 1
})

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机', trigger: 'blur' }]
}

onMounted(async () => {
  await Promise.all([loadDepts(), loadRoles()])
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/system/user', { params })
    list.value = res.data.records
    page.total = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadDepts() {
  const res = await request.get('/system/dept', { params: { current: 1, size: 200 } })
  depts.value = res.data.records || []
}

async function loadRoles() {
  const res = await request.get('/system/role', { params: { current: 1, size: 200 } })
  roles.value = res.data.records || []
}

function openDialog(row) {
  form.id = row?.id || null
  form.userName = row?.userName || ''
  form.nickName = row?.nickName || ''
  form.deptId = row?.deptId || null
  form.phone = row?.phone || ''
  form.email = row?.email || ''
  form.password = ''
  form.status = row?.status ?? 1
  visible.value = true
}

async function save() {
  if (!formRef.value) return
  if (!form.id && !form.password) {
    ElMessage.warning('请输入密码')
    return
  }
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善必填项')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (form.id) {
      delete payload.password
      await request.put(`/system/user/${form.id}`, payload)
    } else {
      await request.post('/system/user', payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function openRoleDialog(row) {
  currentUser.value = row
  const res = await request.get(`/system/user/${row.id}/roles`)
  selectedRoleIds.value = res.data || []
  roleVisible.value = true
}

async function saveRoles() {
  roleSaving.value = true
  try {
    await request.put(`/system/user/${currentUser.value.id}/roles`, { roleIds: selectedRoleIds.value })
    ElMessage.success('角色已更新')
    roleVisible.value = false
    loadData()
  } finally {
    roleSaving.value = false
  }
}

async function handleDelete(id) {
  await request.delete(`/system/user/${id}`)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.toolbar-right { display: flex; align-items: center; gap: 12px; }
.title { font-size: 16px; font-weight: 600; }
.search-input { width: 260px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.hint { color: #64748b; margin-bottom: 12px; }
.page-card { animation: card-enter 0.35s ease both; }
@keyframes card-enter {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
