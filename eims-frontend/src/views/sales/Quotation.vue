<template>
  <div class="quote-page">
    <el-card shadow="never" class="filter-card">
      <el-form inline>
        <el-form-item label="商机">
          <el-select v-model="selectedOppId" filterable placeholder="选择商机" style="width:320px" @change="loadQuotes">
            <el-option v-for="o in opportunities" :key="o.id"
              :label="`${o.opportunityCode} | ${o.customerName} | ¥${o.expectedAmount}`" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!selectedOppId || selectedOpp?.stage >= 4" @click="openDialog()">新建报价</el-button>
          <el-button :disabled="!selectedOppId" @click="openWorkflow">流程详情</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="selectedOpp" shadow="never">
      <template #header>
        <div class="card-header">
          <span>商机：{{ selectedOpp.opportunityCode }} — {{ selectedOpp.customerName }}</span>
          <el-tag :type="findMeta(OPP_STAGE, selectedOpp.stage).type">{{ selectedOpp.stageLabel }}</el-tag>
        </div>
      </template>
      <el-table :data="quotes" v-loading="loadingQuotes" border stripe empty-text="请选择商机并创建报价">
        <el-table-column prop="quotationCode" label="报价单号" width="150" />
        <el-table-column prop="totalAmount" label="报价金额" width="120">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(QUOTE_STATUS, row.status).type">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validUntil" label="有效期" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status < 2" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 0" link type="warning" @click="submit(row.id)">提交</el-button>
            <el-button v-if="row.status < 2" type="success" link @click="confirm(row)">确认并生成合同</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑报价' : '新建报价'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="报价金额" prop="totalAmount">
          <el-input-number v-model="form.totalAmount" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="有效期" prop="validUntil">
          <el-date-picker v-model="form.validUntil" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <SalesWorkflowDrawer ref="workflowRef" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import SalesWorkflowDrawer from '@/components/SalesWorkflowDrawer.vue'
import { OPP_STAGE, QUOTE_STATUS, findMeta } from '@/utils/salesWorkflow'

const router = useRouter()
const workflowRef = ref()
const opportunities = ref([])
const selectedOppId = ref(null)
const quotes = ref([])
const loadingQuotes = ref(false)
const visible = ref(false)
const formRef = ref()
const form = reactive({ id: null, opportunityId: null, totalAmount: null, validUntil: null })
const rules = { totalAmount: [{ required: true, message: '请输入报价金额', trigger: 'blur' }] }

const selectedOpp = computed(() => opportunities.value.find(o => o.id === selectedOppId.value))

onMounted(async () => {
  const res = await request.get('/sales/opportunities', { params: { current: 1, size: 100 } })
  opportunities.value = res.data.records.filter(o => Number(o.stage) < 5)
  if (opportunities.value.length) {
    selectedOppId.value = opportunities.value[0].id
    loadQuotes()
  }
})

async function loadQuotes() {
  if (!selectedOppId.value) { quotes.value = []; return }
  loadingQuotes.value = true
  try {
    const res = await request.get('/sales/quotations', { params: { current: 1, size: 100, opportunityId: selectedOppId.value } })
    quotes.value = res.data.records
  } finally { loadingQuotes.value = false }
}

function openDialog(row) {
  form.id = row?.id || null
  form.opportunityId = selectedOppId.value
  form.totalAmount = row?.totalAmount ?? null
  form.validUntil = row?.validUntil ?? null
  visible.value = true
}

async function save() {
  await formRef.value.validate()
  form.opportunityId = selectedOppId.value
  if (form.id) await request.put(`/sales/quotations/${form.id}`, form)
  else await request.post('/sales/quotations', form)
  ElMessage.success('报价已保存')
  visible.value = false
  loadQuotes()
  refreshOpp()
}

async function submit(id) {
  await request.post(`/sales/workflow/submit-quotation/${id}`)
  ElMessage.success('报价已提交')
  loadQuotes()
}

async function confirm(row) {
  await ElMessageBox.confirm(`确认报价 ¥${row.totalAmount} 并生成销售合同？`, '确认报价', { type: 'warning' })
  await request.post(`/sales/workflow/confirm-quotation/${row.id}`)
  ElMessage.success('报价已确认，合同已生成，请前往合同管理签章')
  loadQuotes()
  refreshOpp()
  router.push('/sales/contract')
}

async function refreshOpp() {
  const res = await request.get('/sales/opportunities', { params: { current: 1, size: 100 } })
  opportunities.value = res.data.records.filter(o => Number(o.stage) < 5)
}

function openWorkflow() {
  if (selectedOppId.value) workflowRef.value?.openByOpportunityId(selectedOppId.value)
}
</script>

<style scoped>
.quote-page { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
