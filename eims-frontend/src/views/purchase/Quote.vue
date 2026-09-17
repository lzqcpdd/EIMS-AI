<template>
  <div class="quote-page">
    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="采购申请">
          <el-select
            v-model="selectedRequestId"
            filterable
            placeholder="选择已审批通过的申请"
            style="width:320px"
            @change="loadQuotes"
          >
            <el-option
              v-for="r in approvedRequests"
              :key="r.id"
              :label="`${r.requestCode} | ¥${r.totalAmount} | ${r.applicantName}`"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!selectedRequestId" @click="openQuoteDialog()">录入供应商报价</el-button>
        </el-form-item>
      </el-form>
      <el-alert
        v-if="!approvedRequests.length && !loadingRequests"
        type="warning"
        :closable="false"
        show-icon
        title="暂无可询价的采购申请"
        description="请先在「采购申请」完成审批，状态为「审批通过」的申请才能进行询价比价。"
      />
    </el-card>

    <el-card v-if="currentRequest" shadow="never" class="request-card">
      <template #header>
        <div class="card-header">
          <span>申请信息：{{ currentRequest.requestCode }}</span>
          <el-tag type="success">{{ currentRequest.statusLabel }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="4" size="small">
        <el-descriptions-item label="发起人">{{ currentRequest.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentRequest.deptName }}</el-descriptions-item>
        <el-descriptions-item label="预算金额">¥ {{ currentRequest.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="当前环节">{{ currentRequest.stageLabel }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>供应商报价对比</span>
          <span v-if="quotes.length" class="hint">共 {{ quotes.length }} 家报价，可对比价格、交期后选定生成订单</span>
        </div>
      </template>
      <el-table :data="quotes" v-loading="loadingQuotes" border stripe empty-text="请先选择采购申请并录入报价">
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="quoteAmount" label="报价金额" width="120">
          <template #default="{ row }">
            <span :class="{ highlight: row.lowestPrice }">¥ {{ row.quoteAmount }}</span>
            <el-tag v-if="row.lowestPrice" size="small" type="success" style="margin-left:4px">最低</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amountDiff" label="较预算" width="110">
          <template #default="{ row }">
            <span :class="diffClass(row.amountDiff)">
              {{ row.amountDiff > 0 ? '+' : '' }}{{ row.amountDiff }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryDays" label="交期(天)" width="100">
          <template #default="{ row }">
            {{ row.deliveryDays }}
            <el-tag v-if="row.fastestDelivery" size="small" type="warning" style="margin-left:4px">最快</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qualityScore" label="质量评分" width="100" />
        <el-table-column prop="selectedLabel" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.isSelected" type="success">已选中</el-tag>
            <span v-else class="muted">待选</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.isSelected" link type="primary" @click="openQuoteDialog(row)">编辑</el-button>
            <el-popconfirm v-if="!row.isSelected" title="确认删除此报价?" @confirm="removeQuote(row.id)">
              <template #reference><el-button link type="danger">删除</el-button></template>
            </el-popconfirm>
            <el-button
              v-if="!row.isSelected"
              type="success"
              link
              @click="selectQuote(row)"
            >选中并生成订单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="quoteVisible" :title="quoteForm.id ? '编辑报价' : '录入报价'" width="480px" destroy-on-close>
      <el-form ref="quoteFormRef" :model="quoteForm" :rules="quoteRules" label-width="100px">
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="quoteForm.supplierId" filterable placeholder="选择供应商" style="width:100%">
            <el-option
              v-for="s in availableSuppliers"
              :key="s.id"
              :label="`${s.supplierName} (${s.supplierCode})`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报价金额" prop="quoteAmount">
          <el-input-number v-model="quoteForm.quoteAmount" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="交期(天)" prop="deliveryDays">
          <el-input-number v-model="quoteForm.deliveryDays" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="质量评分" prop="qualityScore">
          <el-input-number v-model="quoteForm.qualityScore" :min="0" :max="100" :precision="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quoteVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuote">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const approvedRequests = ref([])
const loadingRequests = ref(false)
const selectedRequestId = ref(null)
const quotes = ref([])
const loadingQuotes = ref(false)
const suppliers = ref([])
const quoteVisible = ref(false)
const quoteFormRef = ref()
const quoteForm = reactive({
  id: null,
  requestId: null,
  supplierId: null,
  quoteAmount: null,
  deliveryDays: 7,
  qualityScore: 85
})

const quoteRules = {
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  quoteAmount: [{ required: true, message: '请输入报价金额', trigger: 'blur' }],
  deliveryDays: [{ required: true, message: '请输入交期', trigger: 'blur' }]
}

const currentRequest = computed(() =>
  approvedRequests.value.find(r => r.id === selectedRequestId.value)
)

const availableSuppliers = computed(() => {
  const quotedIds = new Set(
    quotes.value
      .filter(q => !quoteForm.id || q.id !== quoteForm.id)
      .map(q => Number(q.supplierId))
  )
  return suppliers.value.filter(s => !quotedIds.has(Number(s.id)))
})

onMounted(async () => {
  await loadSuppliers()
  await loadApprovedRequests()
})

async function loadApprovedRequests() {
  loadingRequests.value = true
  try {
    const res = await request.get('/purchase/requests', { params: { current: 1, size: 100, status: 2 } })
    approvedRequests.value = res.data.records.filter(r => Number(r.status) === 2)
    if (approvedRequests.value.length && !selectedRequestId.value) {
      selectedRequestId.value = approvedRequests.value[0].id
      await loadQuotes()
    }
  } finally {
    loadingRequests.value = false
  }
}

async function loadSuppliers() {
  const res = await request.get('/base/suppliers', { params: { current: 1, size: 200 } })
  suppliers.value = res.data.records
}

async function loadQuotes() {
  if (!selectedRequestId.value) {
    quotes.value = []
    return
  }
  loadingQuotes.value = true
  try {
    const res = await request.get('/purchase/quotes', {
      params: { current: 1, size: 100, requestId: selectedRequestId.value }
    })
    quotes.value = res.data.records
      .filter(q => Number(q.requestId) === Number(selectedRequestId.value))
      .map(q => ({
        ...q,
        supplierName: q.supplierName || suppliers.value.find(s => s.id === q.supplierId)?.supplierName || `供应商#${q.supplierId}`,
        selectedLabel: q.isSelected ? '已选中' : '—'
      }))
  } finally {
    loadingQuotes.value = false
  }
}

function diffClass(diff) {
  if (diff == null) return ''
  if (Number(diff) > 0) return 'over'
  if (Number(diff) < 0) return 'under'
  return ''
}

function openQuoteDialog(row) {
  quoteForm.id = row?.id || null
  quoteForm.requestId = selectedRequestId.value
  quoteForm.supplierId = row?.supplierId || null
  quoteForm.quoteAmount = row?.quoteAmount ?? null
  quoteForm.deliveryDays = row?.deliveryDays ?? 7
  quoteForm.qualityScore = row?.qualityScore ?? 85
  quoteVisible.value = true
}

async function saveQuote() {
  try {
    await quoteFormRef.value.validate()
  } catch {
    ElMessage.warning('请完善必填项后再保存')
    return
  }
  quoteForm.requestId = selectedRequestId.value
  try {
    if (quoteForm.id) {
      await request.put(`/purchase/quotes/${quoteForm.id}`, quoteForm)
    } else {
      await request.post('/purchase/quotes', quoteForm)
    }
    ElMessage.success('报价已保存')
    quoteVisible.value = false
    loadQuotes()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  }
}

async function removeQuote(id) {
  await request.delete(`/purchase/quotes/${id}`)
  ElMessage.success('已删除')
  loadQuotes()
}

async function selectQuote(row) {
  await ElMessageBox.confirm(
    `确认选用「${row.supplierName}」的报价 ¥${row.quoteAmount}，并生成采购订单？`,
    '生成采购订单',
    { type: 'warning' }
  )
  await request.post('/purchase/workflow/create-order', {
    quoteId: row.id,
    requestId: row.requestId || selectedRequestId.value
  })
  ElMessage.success('采购订单已生成，请前往「采购订单」查看并下载详情')
  loadApprovedRequests()
  loadQuotes()
}
</script>

<style scoped>
.quote-page { display: flex; flex-direction: column; gap: 16px; }
.filter-card, .request-card { margin-bottom: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.hint { font-size: 13px; color: #64748b; font-weight: normal; }
.highlight { color: #16a34a; font-weight: 600; }
.over { color: #dc2626; }
.under { color: #16a34a; }
.muted { color: #94a3b8; }
</style>
