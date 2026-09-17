<template>
  <div class="role-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="toolbar">
          <span class="title">角色管理</span>
          <div class="toolbar-right">
            <el-input
              v-model="keyword"
              class="search-input"
              clearable
              placeholder="搜索角色名/编码"
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
        <el-table-column prop="roleName" label="角色名" min-width="140" />
        <el-table-column prop="roleCode" label="编码" width="120" />
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
              <el-button link type="warning" @click="openMenuDialog(row)">菜单权限</el-button>
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

    <el-dialog v-model="visible" :title="form.id ? '编辑角色' : '新增角色'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!form.id" />
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

    <el-drawer v-model="menuVisible" title="菜单权限配置" size="420px" destroy-on-close>
      <p class="hint">为角色「{{ currentRole?.roleName }}」勾选可访问菜单</p>
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        node-key="id"
        show-checkbox
        default-expand-all
        :props="{ label: 'menuName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="menuVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSaving" @click="saveMenus">保存权限</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const list = ref([])
const menuTree = ref([])
const loading = ref(false)
const saving = ref(false)
const menuSaving = ref(false)
const visible = ref(false)
const menuVisible = ref(false)
const keyword = ref('')
const formRef = ref()
const menuTreeRef = ref()
const currentRole = ref(null)
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入编码', trigger: 'blur' }]
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/system/role', { params })
    list.value = res.data.records
    page.total = res.data.total
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  form.id = row?.id || null
  form.roleName = row?.roleName || ''
  form.roleCode = row?.roleCode || ''
  form.status = row?.status ?? 1
  visible.value = true
}

async function save() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) await request.put(`/system/role/${form.id}`, form)
    else await request.post('/system/role', form)
    ElMessage.success('保存成功')
    visible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function openMenuDialog(row) {
  currentRole.value = row
  const [treeRes, menuRes] = await Promise.all([
    request.get('/system/role/menus/tree'),
    request.get(`/system/role/${row.id}/menus`)
  ])
  menuTree.value = treeRes.data || []
  menuVisible.value = true
  await nextTick()
  menuTreeRef.value?.setCheckedKeys(menuRes.data || [], false)
}

async function saveMenus() {
  menuSaving.value = true
  try {
    const checked = menuTreeRef.value?.getCheckedKeys(false) || []
    const halfChecked = menuTreeRef.value?.getHalfCheckedKeys() || []
    const menuIds = [...new Set([...checked, ...halfChecked])]
    await request.put(`/system/role/${currentRole.value.id}/menus`, { menuIds })
    ElMessage.success('菜单权限已更新')
    menuVisible.value = false
  } finally {
    menuSaving.value = false
  }
}

async function handleDelete(id) {
  await request.delete(`/system/role/${id}`)
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
