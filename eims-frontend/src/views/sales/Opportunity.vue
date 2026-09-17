<template>
  <div class="opp-page">
    <el-card shadow="never">
      <template #header><span class="title">商机管理</span></template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="opportunityCode" label="商机编号" width="150" />
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column prop="expectedAmount" label="预计金额" width="130" align="right">
          <template #default="{ row }">¥ {{ formatMoney(row.expectedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="stage" label="阶段" width="100">
          <template #default="{ row }">
            <el-tooltip :content="row.stageLabel" placement="top" :disabled="!row.stageLabel">
              <el-tag :type="findMeta(OPP_STAGE, row.stage).type" size="small">
                {{ findMeta(OPP_STAGE, row.stage).label }}
              </el-tag>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">流程详情</el-button>
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
import request from '@/utils/request'
import SalesWorkflowDrawer from '@/components/SalesWorkflowDrawer.vue'
import { OPP_STAGE, findMeta } from '@/utils/salesWorkflow'
import { formatMoney } from '@/utils/format'

const list = ref([])
const loading = ref(false)
const workflowRef = ref()
const page = reactive({ current: 1, size: 10, total: 0 })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales/opportunities', { params: { current: page.current, size: page.size } })
    list.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

function openDetail(id) {
  workflowRef.value?.openByOpportunityId(id)
}
</script>

<style scoped>
.title { font-size: 16px; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
