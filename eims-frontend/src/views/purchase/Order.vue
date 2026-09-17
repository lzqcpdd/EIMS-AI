<template>

  <div class="order-page">

    <el-card shadow="never">

      <template #header>

        <span class="title">采购订单</span>

      </template>

      <el-table :data="list" v-loading="loading" stripe>

        <el-table-column prop="orderCode" label="订单号" width="160" />

        <el-table-column prop="requestCode" label="关联申请" width="150" />

        <el-table-column prop="supplierName" label="供应商" min-width="160" />

        <el-table-column prop="applicantName" label="申请人" width="100" />

        <el-table-column prop="totalAmount" label="订单金额" width="110">

          <template #default="{ row }">¥ {{ row.totalAmount }}</template>

        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">

          <template #default="{ row }">

            <el-tag :type="resolveOrderStatusType(row)">{{ resolveOrderStatusLabel(row) }}</el-tag>

          </template>

        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="170" />

        <el-table-column label="操作" width="260" fixed="right">

          <template #default="{ row }">

            <el-button link type="primary" @click="openDetail(row.id)">查看详情</el-button>

            <el-button v-if="canReceive(row)" link type="warning" @click="openDetail(row.id, true)">到货入库</el-button>

            <el-button link type="success" @click="handleDownload(row)">下载详情</el-button>

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



    <el-drawer v-model="detailVisible" title="采购订单详情" size="720px">

      <template v-if="detail">

        <el-descriptions :column="2" border size="small">

          <el-descriptions-item label="订单号">{{ detail.orderCode }}</el-descriptions-item>

          <el-descriptions-item label="状态">

            <el-tag :type="resolveOrderStatusType(detail)">{{ resolveOrderStatusLabel(detail) }}</el-tag>

          </el-descriptions-item>

          <el-descriptions-item label="供应商">{{ detail.supplierName }}</el-descriptions-item>

          <el-descriptions-item label="联系人">{{ detail.supplierContact }} / {{ detail.supplierPhone }}</el-descriptions-item>

          <el-descriptions-item label="关联申请">{{ detail.requestCode }}</el-descriptions-item>

          <el-descriptions-item label="申请人">{{ detail.applicantName }}（{{ detail.deptName }}）</el-descriptions-item>

          <el-descriptions-item label="订单金额">¥ {{ detail.totalAmount }}</el-descriptions-item>

          <el-descriptions-item label="预计到货">{{ detail.expectedArrivalTime || '—' }}</el-descriptions-item>

          <el-descriptions-item label="创建时间" :span="2">{{ detail.createTime }}</el-descriptions-item>

        </el-descriptions>



        <div class="section-title">物料明细</div>

        <el-table :data="detail.items || []" border size="small">

          <el-table-column prop="materialCode" label="物料编码" />

          <el-table-column prop="materialName" label="物料名称" />

          <el-table-column prop="quantity" label="订购数量" width="90" />

          <el-table-column label="已入库" width="90">

            <template #default="{ row }">

              <span :class="{ done: row.receivedQuantity >= row.quantity }">{{ row.receivedQuantity || 0 }}</span>

            </template>

          </el-table-column>

          <el-table-column label="待入库" width="90">

            <template #default="{ row }">{{ pendingQty(row) }}</template>

          </el-table-column>

          <el-table-column prop="unitPrice" label="单价" width="100">

            <template #default="{ row }">¥ {{ row.unitPrice }}</template>

          </el-table-column>

          <el-table-column label="小计" width="100">

            <template #default="{ row }">¥ {{ lineAmount(row) }}</template>

          </el-table-column>

        </el-table>

        <div class="total-bar">

          合计：<strong>¥ {{ detail.items?.length ? detailTotal : detail.totalAmount }}</strong>

          <el-button

            v-if="canReceive(detail)"

            type="warning"

            style="margin-left:16px"

            @click="openReceiveDialog"

          >到货入库</el-button>

          <el-button type="success" style="margin-left:8px" @click="handleDownload(detail)">下载订单详情</el-button>

        </div>

      </template>

    </el-drawer>



    <el-dialog v-model="receiveVisible" title="到货入库" width="480px" destroy-on-close @closed="resetReceiveForm">

      <el-alert

        type="info"

        :closable="false"

        show-icon

        style="margin-bottom:16px"

        title="确认供应商货物已到仓后，选择仓库和物料完成入库。入库后库存将增加，订单状态自动更新。"

      />

      <el-form ref="receiveFormRef" :model="receiveForm" :rules="receiveRules" label-width="90px">

        <el-form-item label="入库仓库" prop="warehouseId">

          <el-select v-model="receiveForm.warehouseId" filterable placeholder="选择仓库" style="width:100%">

            <el-option

              v-for="w in warehouses"

              :key="w.id"

              :label="`${w.warehouseName}（${w.warehouseCode}）`"

              :value="w.id"

            />

          </el-select>

        </el-form-item>

        <el-form-item label="物料" prop="materialCode">

          <el-select v-model="receiveForm.materialCode" placeholder="选择物料" style="width:100%" @change="onMaterialChange">

            <el-option

              v-for="item in receivableItems"

              :key="item.materialCode"

              :label="`${item.materialName}（待入库 ${pendingQty(item)}）`"

              :value="item.materialCode"

            />

          </el-select>

        </el-form-item>

        <el-form-item label="入库数量" prop="quantity">

          <el-input-number

            v-model="receiveForm.quantity"

            :min="1"

            :max="maxReceiveQty"

            style="width:100%"

          />

        </el-form-item>

      </el-form>

      <template #footer>

        <el-button @click="receiveVisible = false">取消</el-button>

        <el-button type="primary" :loading="receiving" @click="submitReceive">确认入库</el-button>

      </template>

    </el-dialog>

  </div>

</template>



<script setup>

import { ref, reactive, computed, onMounted } from 'vue'

import request from '@/utils/request'

import { ElMessage } from 'element-plus'

import { downloadFile } from '@/utils/download'

import { lineAmount, calcTotalAmount, orderStatusLabel, orderStatusType } from '@/utils/purchaseWorkflow'



const list = ref([])

const loading = ref(false)

const detail = ref(null)

const detailVisible = ref(false)

const page = reactive({ current: 1, size: 10, total: 0 })



const warehouses = ref([])

const receiveVisible = ref(false)

const receiving = ref(false)

const receiveFormRef = ref()

const receiveForm = reactive({

  warehouseId: null,

  materialCode: '',

  quantity: 1

})



const receiveRules = {

  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],

  materialCode: [{ required: true, message: '请选择物料', trigger: 'change' }],

  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]

}



const detailTotal = computed(() => detail.value?.items ? calcTotalAmount(detail.value.items) : '0.00')



const receivableItems = computed(() =>

  (detail.value?.items || []).filter(item => pendingQty(item) > 0)

)



const maxReceiveQty = computed(() => {

  const item = (detail.value?.items || []).find(i => i.materialCode === receiveForm.materialCode)

  return item ? pendingQty(item) : 1

})



onMounted(() => {

  loadData()

  loadWarehouses()

})



function canReceive(row) {

  const s = Number(row?.status)

  return s === 0 || s === 1

}



function pendingQty(row) {

  const ordered = Number(row.quantity) || 0

  const received = Number(row.receivedQuantity) || 0

  return Math.max(ordered - received, 0)

}



function resolveOrderStatusLabel(row) {

  return row.statusLabel || orderStatusLabel(row.status)

}



function resolveOrderStatusType(row) {

  return orderStatusType(row.status)

}



async function loadWarehouses() {

  const res = await request.get('/base/warehouses', { params: { current: 1, size: 100 } })

  warehouses.value = res.data.records || []

}



async function loadData() {

  loading.value = true

  try {

    const res = await request.get('/purchase/orders', { params: { current: page.current, size: page.size } })

    list.value = res.data.records

    page.total = res.data.total

  } finally {

    loading.value = false

  }

}



async function openDetail(id, openReceive = false) {

  const res = await request.get(`/purchase/orders/${id}`)

  detail.value = res.data

  detailVisible.value = true

  if (openReceive && canReceive(detail.value)) {

    openReceiveDialog()

  }

}



function openReceiveDialog() {

  if (!receivableItems.value.length) {

    ElMessage.warning('该订单所有物料均已入库')

    return

  }

  receiveForm.warehouseId = warehouses.value[0]?.id ?? null

  const first = receivableItems.value[0]

  receiveForm.materialCode = first.materialCode

  receiveForm.quantity = pendingQty(first)

  receiveVisible.value = true

}



function onMaterialChange(code) {

  const item = (detail.value?.items || []).find(i => i.materialCode === code)

  if (item) receiveForm.quantity = pendingQty(item)

}



function resetReceiveForm() {

  receiveForm.warehouseId = null

  receiveForm.materialCode = ''

  receiveForm.quantity = 1

}



async function submitReceive() {

  try {

    await receiveFormRef.value.validate()

  } catch {

    return

  }

  receiving.value = true

  try {

    await request.post('/purchase/workflow/receive', {

      orderId: detail.value.id,

      warehouseId: receiveForm.warehouseId,

      materialCode: receiveForm.materialCode,

      quantity: receiveForm.quantity

    })

    ElMessage.success('入库成功')

    receiveVisible.value = false

    await openDetail(detail.value.id)

    loadData()

  } catch (e) {

    ElMessage.error(e?.message || '入库失败')

  } finally {

    receiving.value = false

  }

}



async function handleDownload(row) {

  try {

    await downloadFile(`/purchase/orders/${row.id}/export`, `${row.orderCode}_订单详情.csv`)

    ElMessage.success('下载成功')

  } catch (e) {

    ElMessage.error(e.message || '下载失败')

  }

}

</script>



<style scoped>

.title { font-size: 16px; font-weight: 600; }

.pager { margin-top: 16px; justify-content: flex-end; }

.section-title { font-weight: 600; margin: 20px 0 10px; }

.total-bar { text-align: right; margin-top: 16px; font-size: 15px; }

.total-bar strong { color: #2563eb; font-size: 18px; }

.done { color: #16a34a; font-weight: 600; }

</style>


