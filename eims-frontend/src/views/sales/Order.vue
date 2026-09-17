<template>
  <div class="order-page">
    <el-card shadow="never">
      <template #header><span class="title">销售订单</span></template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderCode" label="订单号" width="150" />
        <el-table-column prop="customerName" label="客户" min-width="140" />
        <el-table-column prop="contractCode" label="关联合同" width="150" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(ORDER_STATUS, row.status).type">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="回款" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(PAYMENT_STATUS, row.paymentStatus).type">{{ row.paymentStatusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">查看详情</el-button>
            <el-button link type="primary" @click="openWorkflow(row)">流程详情</el-button>
            <el-button v-if="canDeliver(row)" link type="warning" @click="openDetail(row.id, true)">发货出库</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <el-drawer v-model="detailVisible" title="销售订单详情" size="720px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderCode }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="findMeta(ORDER_STATUS, detail.status).type">{{ detail.statusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户">{{ detail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="关联合同">{{ detail.contractCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥ {{ detail.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="回款状态">
            <el-tag :type="findMeta(PAYMENT_STATUS, detail.paymentStatus).type">{{ detail.paymentStatusLabel }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ detail.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title">物料明细</div>
        <el-table :data="detail.items || []" border size="small">
          <el-table-column prop="materialCode" label="物料编码" />
          <el-table-column prop="materialName" label="物料名称" />
          <el-table-column prop="quantity" label="订购数量" width="90" />
          <el-table-column label="已发货" width="90">
            <template #default="{ row }">{{ row.deliveredQuantity || 0 }}</template>
          </el-table-column>
          <el-table-column label="待发货" width="90">
            <template #default="{ row }">{{ pendingDeliver(row) }}</template>
          </el-table-column>
          <el-table-column prop="unitPrice" label="单价" width="100">
            <template #default="{ row }">¥ {{ row.unitPrice }}</template>
          </el-table-column>
        </el-table>

        <div class="total-bar">
          <el-button type="primary" @click="openWorkflow(detail)">查看销售流程</el-button>
          <el-button v-if="canDeliver(detail)" type="warning" @click="openDeliverDialog">发货出库</el-button>
        </div>
      </template>
    </el-drawer>

    <SalesWorkflowDrawer ref="workflowRef" />

    <el-dialog v-model="deliverVisible" title="发货出库" width="480px" destroy-on-close>
      <el-form ref="deliverFormRef" :model="deliverForm" :rules="deliverRules" label-width="90px">
        <el-form-item label="出库仓库" prop="warehouseId">
          <el-select v-model="deliverForm.warehouseId" filterable style="width:100%">
            <el-option v-for="w in warehouses" :key="w.id" :label="`${w.warehouseName}（${w.warehouseCode}）`" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物料" prop="materialCode">
          <el-select v-model="deliverForm.materialCode" style="width:100%" @change="onMaterialChange">
            <el-option v-for="item in deliverableItems" :key="item.materialCode"
              :label="`${item.materialName}（待发 ${pendingDeliver(item)}）`" :value="item.materialCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货数量" prop="quantity">
          <el-input-number v-model="deliverForm.quantity" :min="1" :max="maxDeliverQty" style="width:100%" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="deliverForm.logisticsNo" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deliverVisible = false">取消</el-button>
        <el-button type="primary" :loading="delivering" @click="submitDeliver">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import SalesWorkflowDrawer from '@/components/SalesWorkflowDrawer.vue'
import { ORDER_STATUS, PAYMENT_STATUS, findMeta, pendingDeliver } from '@/utils/salesWorkflow'

const workflowRef = ref()

const list = ref([])
const loading = ref(false)
const detail = ref(null)
const detailVisible = ref(false)
const deliverVisible = ref(false)
const delivering = ref(false)
const warehouses = ref([])
const deliverFormRef = ref()
const deliverForm = reactive({ warehouseId: null, materialCode: '', quantity: 1, logisticsNo: '' })
const deliverRules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  materialCode: [{ required: true, message: '请选择物料', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}
const page = reactive({ current: 1, size: 10, total: 0 })

const deliverableItems = computed(() => (detail.value?.items || []).filter(i => pendingDeliver(i) > 0))
const maxDeliverQty = computed(() => {
  const item = (detail.value?.items || []).find(i => i.materialCode === deliverForm.materialCode)
  return item ? pendingDeliver(item) : 1
})

onMounted(() => { loadData(); loadWarehouses() })

function canDeliver(row) {
  const s = Number(row?.status)
  return s === 1 || s === 2
}

async function loadWarehouses() {
  const res = await request.get('/base/warehouses', { params: { current: 1, size: 100 } })
  warehouses.value = res.data.records || []
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales/orders', { params: { current: page.current, size: page.size } })
    list.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

async function openDetail(id, openDeliver = false) {
  const res = await request.get(`/sales/orders/${id}`)
  detail.value = res.data
  detailVisible.value = true
  if (openDeliver && canDeliver(detail.value)) openDeliverDialog()
}

function openDeliverDialog() {
  if (!deliverableItems.value.length) {
    ElMessage.warning('该订单所有物料均已发货')
    return
  }
  deliverForm.warehouseId = warehouses.value[0]?.id ?? null
  const first = deliverableItems.value[0]
  deliverForm.materialCode = first.materialCode
  deliverForm.quantity = pendingDeliver(first)
  deliverForm.logisticsNo = ''
  deliverVisible.value = true
}

function onMaterialChange(code) {
  const item = (detail.value?.items || []).find(i => i.materialCode === code)
  if (item) deliverForm.quantity = pendingDeliver(item)
}

async function submitDeliver() {
  await deliverFormRef.value.validate()
  delivering.value = true
  try {
    await request.post('/sales/workflow/deliver', {
      orderId: detail.value.id,
      warehouseId: deliverForm.warehouseId,
      materialCode: deliverForm.materialCode,
      quantity: deliverForm.quantity,
      logisticsNo: deliverForm.logisticsNo || undefined
    })
    ElMessage.success('发货成功')
    deliverVisible.value = false
    await openDetail(detail.value.id)
    loadData()
  } catch (e) {
    ElMessage.error(e?.message || '发货失败')
  } finally { delivering.value = false }
}

function openWorkflow(row) {
  if (row.opportunityId) workflowRef.value?.openByOpportunityId(row.opportunityId)
  else workflowRef.value?.openByOrderId(row.id)
}
</script>

<style scoped>
.title { font-size: 16px; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
.section-title { font-weight: 600; margin: 20px 0 10px; }
.total-bar { text-align: right; margin-top: 16px; }
</style>
