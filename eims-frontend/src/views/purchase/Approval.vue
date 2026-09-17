<template>
  <el-card shadow="never">
    <template #header>
      <div class="toolbar">
        <span class="title">采购审批</span>
        <el-tag type="warning">待审批 {{ pendingCount }} 单</el-tag>
      </div>
    </template>
    <el-alert
      v-if="!loading && pendingCount === 0"
      type="info"
      :closable="false"
      show-icon
      title="暂无待审批申请"
      description="采购专员在「采购申请」提交后，会在此显示待您审批的单据。"
      style="margin-bottom: 16px"
    />
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="requestCode" label="申请单号" width="160" />
      <el-table-column prop="applicantName" label="发起人" width="100" />
      <el-table-column prop="deptName" label="部门" width="100" />
      <el-table-column prop="totalAmount" label="金额" width="110">
        <template #default="{ row }">¥ {{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stageLabel" label="当前环节" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">查看流程</el-button>
          <template v-if="isPending(row)">
            <el-button type="success" link @click="openApprove(row, 1)">通过</el-button>
            <el-button type="danger" link @click="openApprove(row, 2)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="detailVisible" title="审批流程详情" size="640px">
      <PurchaseWorkflowPanel :detail="detail" />
    </el-drawer>

    <el-dialog v-model="approveVisible" :title="approveResult === 1 ? '审批通过' : '审批驳回'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="申请单号">{{ approveRow?.requestCode }}</el-form-item>
        <el-form-item label="当前环节">{{ approveRow?.stageLabel }}</el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveComment" type="textarea" :rows="3" :placeholder="approveResult === 1 ? '同意' : '请填写驳回原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button :type="approveResult === 1 ? 'success' : 'danger'" @click="confirmApprove">确认</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import PurchaseWorkflowPanel from '@/components/PurchaseWorkflowPanel.vue'
import { statusTagType } from '@/utils/purchaseWorkflow'

const list = ref([])
const loading = ref(false)
const detail = ref(null)
const detailVisible = ref(false)
const approveVisible = ref(false)
const approveRow = ref(null)
const approveResult = ref(1)
const approveComment = ref('')

const pendingCount = computed(() => list.value.filter(isPending).length)

onMounted(load)

function isPending(row) {
  return Number(row.status) === 1
}

async function load() {
  loading.value = true
  try {
    const res = await request.get('/purchase/requests', { params: { current: 1, size: 50 } })
    list.value = res.data.records.filter(r => Number(r.status) <= 3)
  } finally {
    loading.value = false
  }
}

async function openDetail(id) {
  const res = await request.get(`/purchase/requests/${id}`)
  detail.value = res.data
  detailVisible.value = true
}

function openApprove(row, result) {
  approveRow.value = row
  approveResult.value = result
  approveComment.value = result === 1 ? '同意' : ''
  approveVisible.value = true
}

async function confirmApprove() {
  if (approveResult.value === 2 && !approveComment.value.trim()) {
    ElMessage.warning('驳回时必须填写审批意见')
    return
  }
  await request.post('/purchase/workflow/approve', {
    requestId: approveRow.value.id,
    result: approveResult.value,
    comment: approveComment.value.trim()
  })
  ElMessage.success('审批完成')
  approveVisible.value = false
  load()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
</style>
