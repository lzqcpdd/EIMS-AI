<template>
  <div class="delivery-page">
    <el-card shadow="never">
      <template #header><span class="title">发货管理</span></template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="deliveryCode" label="发货单号" width="150" />
        <el-table-column prop="orderCode" label="销售订单" width="150" />
        <el-table-column prop="warehouseName" label="出库仓库" width="140" />
        <el-table-column prop="logisticsNo" label="物流单号" min-width="140">
          <template #default="{ row }">{{ row.logisticsNo || '—' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(DELIVERY_STATUS, row.status).type">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="success" @click="sign(row)">确认签收</el-button>
            <span v-else-if="row.status === 2" class="muted">已签收</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { DELIVERY_STATUS, findMeta } from '@/utils/salesWorkflow'

const list = ref([])
const loading = ref(false)
const page = reactive({ current: 1, size: 10, total: 0 })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales/deliveries', { params: { current: page.current, size: page.size } })
    list.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

async function sign(row) {
  await request.post(`/sales/workflow/sign-delivery/${row.id}`, { logisticsNo: row.logisticsNo })
  ElMessage.success('已确认签收')
  loadData()
}
</script>

<style scoped>
.title { font-size: 16px; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
.muted { color: #94a3b8; font-size: 13px; }
</style>
