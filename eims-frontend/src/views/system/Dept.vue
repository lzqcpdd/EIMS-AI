<template>
  <div class="dept-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="toolbar">
          <span class="title">部门管理</span>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>新增部门
          </el-button>
        </div>
      </template>

      <el-table
        :data="treeData"
        v-loading="loading"
        row-key="id"
        default-expand-all
        stripe
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="deptCode" label="编码" width="120" />
        <el-table-column prop="sortNo" label="排序" width="80" align="right" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑部门' : '新增部门'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            check-strictly
            :render-after-expand="false"
            style="width:100%"
            placeholder="选择上级部门"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode" />
        </el-form-item>
        <el-form-item label="排序" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" style="width:100%" />
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
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const visible = ref(false)
const treeData = ref([])
const formRef = ref()
const form = reactive({
  id: null,
  parentId: 0,
  deptName: '',
  deptCode: '',
  sortNo: 0,
  status: 1
})

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }]
}

const parentOptions = computed(() => [
  { value: 0, label: '顶级部门', children: mapTreeOptions(treeData.value) }
])

onMounted(loadTree)

async function loadTree() {
  loading.value = true
  try {
    const res = await request.get('/system/dept/tree')
    treeData.value = res.data || []
  } finally {
    loading.value = false
  }
}

function mapTreeOptions(nodes) {
  return (nodes || []).map(node => ({
    value: node.id,
    label: node.deptName,
    children: mapTreeOptions(node.children)
  }))
}

function openDialog(row) {
  form.id = row?.id || null
  form.parentId = row?.parentId ?? 0
  form.deptName = row?.deptName || ''
  form.deptCode = row?.deptCode || ''
  form.sortNo = row?.sortNo ?? 0
  form.status = row?.status ?? 1
  visible.value = true
}

async function save() {
  await formRef.value.validate()
  if (form.id) await request.put(`/system/dept/${form.id}`, form)
  else await request.post('/system/dept', form)
  ElMessage.success('保存成功')
  visible.value = false
  loadTree()
}

async function handleDelete(id) {
  await request.delete(`/system/dept/${id}`)
  ElMessage.success('删除成功')
  loadTree()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.page-card { animation: card-enter 0.35s ease both; }
@keyframes card-enter {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
