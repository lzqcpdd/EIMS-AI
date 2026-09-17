<template>
  <div class="contract-page">
    <el-card shadow="never">
      <template #header><span class="title">销售合同</span></template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="contractCode" label="合同编号" width="150" />
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column prop="quotationCode" label="关联报价" width="150" />
        <el-table-column prop="totalAmount" label="合同金额" width="120">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="signStatus" label="签章状态" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(CONTRACT_SIGN, row.signStatus).type">{{ row.signStatusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signTime" label="签章时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openWorkflow(row)">流程详情</el-button>
            <el-button v-if="row.signStatus === 0" type="success" link @click="sign(row)">签章并生成订单</el-button>
            <span v-else-if="row.signStatus === 1" class="muted">已签章</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <SalesWorkflowDrawer ref="workflowRef" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import SalesWorkflowDrawer from '@/components/SalesWorkflowDrawer.vue'
import { CONTRACT_SIGN, findMeta } from '@/utils/salesWorkflow'

const router = useRouter()
const workflowRef = ref()
const list = ref([])
const loading = ref(false)
const page = reactive({ current: 1, size: 10, total: 0 })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales/contracts', { params: { current: page.current, size: page.size } })
    list.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

async function sign(row) {
  await ElMessageBox.confirm(
    `确认合同「${row.contractCode}」签章并生成销售订单？`,
    '合同签章',
    { type: 'warning' }
  )
  await request.post(`/sales/workflow/sign-contract/${row.id}`)
  ElMessage.success('合同已签章，销售订单已生成，请前往销售订单发货')
  loadData()
  router.push('/sales/order')
}

function openWorkflow(row) {
  if (row.opportunityId) workflowRef.value?.openByOpportunityId(row.opportunityId)
  else ElMessage.warning('未找到关联商机')
}
</script>

<style scoped>
.title { font-size: 16px; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
.muted { color: #94a3b8; font-size: 13px; }
</style>
