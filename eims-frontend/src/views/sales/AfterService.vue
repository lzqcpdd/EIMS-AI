<template>
  <div class="service-page">
    <el-card shadow="never">
      <template #header>
        <div class="toolbar">
          <span class="title">售后服务</span>
          <el-button type="primary" @click="openCreate">新建售后</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="serviceCode" label="售后单号" width="150" />
        <el-table-column prop="orderCode" label="销售订单" width="150" />
        <el-table-column prop="customerName" label="客户" min-width="120" />
        <el-table-column prop="serviceType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(SERVICE_TYPE, row.serviceType).type">{{ row.serviceTypeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(SERVICE_STATUS, row.status).type">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="success" @click="complete(row.id)">完成处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="createVisible" title="新建售后服务" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="销售订单" prop="orderId">
          <el-select v-model="form.orderId" filterable placeholder="选择订单" style="width:100%">
            <el-option v-for="o in orders" :key="o.id" :label="`${o.orderCode} | ${o.customerName}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-select v-model="form.serviceType" style="width:100%">
            <el-option :value="1" label="客户回访" />
            <el-option :value="2" label="退货处理" />
            <el-option :value="3" label="投诉处理" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { SERVICE_TYPE, SERVICE_STATUS, findMeta } from '@/utils/salesWorkflow'

const list = ref([])
const loading = ref(false)
const createVisible = ref(false)
const orders = ref([])
const formRef = ref()
const form = reactive({ orderId: null, serviceType: 1, content: '' })
const page = reactive({ current: 1, size: 10, total: 0 })
const rules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  content: [{ required: true, message: '请输入服务内容', trigger: 'blur' }]
}

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales/after-services', { params: { current: page.current, size: page.size } })
    list.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

async function openCreate() {
  const res = await request.get('/sales/orders', { params: { current: 1, size: 100 } })
  orders.value = res.data.records
  form.orderId = null
  form.serviceType = 1
  form.content = ''
  createVisible.value = true
}

async function submitCreate() {
  await formRef.value.validate()
  await request.post('/sales/workflow/after-service', form)
  ElMessage.success('售后单已创建')
  createVisible.value = false
  loadData()
}

async function complete(id) {
  await request.post(`/sales/workflow/complete-service/${id}`)
  ElMessage.success('已标记为完成')
  loadData()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>
