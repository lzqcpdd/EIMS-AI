<template>
  <div class="lead-page">
    <el-card shadow="never">
      <template #header>
        <div class="toolbar">
          <span class="title">销售线索</span>
          <el-button type="primary" @click="openDialog()">新增线索</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="leadCode" label="线索编号" width="150" />
        <el-table-column prop="customerName" label="客户名称" min-width="160" />
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="findMeta(LEAD_STATUS, row.status).type">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stageLabel" label="当前环节" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openWorkflow(row)">流程详情</el-button>
              <template v-if="row.status < 2">
                <el-button link type="warning" @click="follow(row.id)">标记跟进</el-button>
                <el-button link type="primary" @click="openConvert(row)">转商机</el-button>
                <el-dropdown trigger="click" @command="cmd => handleAction(cmd, row)">
                  <el-button link type="primary">更多</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="invalidate" divided>
                        <span class="danger-text">标记无效</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑线索' : '新增线索'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="潜在客户名称" />
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-select v-model="form.source" placeholder="选择来源" style="width:100%">
            <el-option v-for="s in sources" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级" prop="level">
          <el-select v-model="form.level" style="width:100%">
            <el-option :value="1" label="普通" />
            <el-option :value="2" label="重点" />
            <el-option :value="3" label="战略" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="convertVisible" title="线索转商机" width="480px" destroy-on-close>
      <p class="hint">将线索「{{ convertLead?.customerName }}」转为正式商机，请选择关联客户。</p>
      <el-form label-width="90px">
        <el-form-item label="关联客户">
          <el-select v-model="convertCustomerId" filterable placeholder="选择客户" style="width:100%">
            <el-option v-for="c in customers" :key="c.id" :label="`${c.customerName}（${c.customerCode}）`" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convertVisible = false">取消</el-button>
        <el-button type="primary" @click="doConvert">确认转商机</el-button>
      </template>
    </el-dialog>

    <SalesWorkflowDrawer ref="workflowRef" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import SalesWorkflowDrawer from '@/components/SalesWorkflowDrawer.vue'
import { LEAD_STATUS, findMeta } from '@/utils/salesWorkflow'

const router = useRouter()
const workflowRef = ref()
const list = ref([])
const loading = ref(false)
const visible = ref(false)
const convertVisible = ref(false)
const customers = ref([])
const convertLead = ref(null)
const convertCustomerId = ref(null)
const formRef = ref()
const form = reactive({ id: null, customerName: '', source: '官网', level: 1 })
const page = reactive({ current: 1, size: 10, total: 0 })
const sources = ['官网', '展会', '转介绍', '电话营销', '其他']
const rules = { customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }] }

onMounted(() => { loadData(); loadCustomers() })

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/sales/leads', { params: { current: page.current, size: page.size } })
    list.value = res.data.records
    page.total = res.data.total
  } finally { loading.value = false }
}

async function loadCustomers() {
  const res = await request.get('/base/customers', { params: { current: 1, size: 200 } })
  customers.value = res.data.records
}

function openDialog(row) {
  form.id = row?.id || null
  form.customerName = row?.customerName || ''
  form.source = row?.source || '官网'
  form.level = row?.level ?? 1
  visible.value = true
}

async function save() {
  await formRef.value.validate()
  if (form.id) await request.put(`/sales/leads/${form.id}`, form)
  else await request.post('/sales/leads', form)
  ElMessage.success('已保存')
  visible.value = false
  loadData()
}

async function follow(id) {
  await request.post(`/sales/workflow/follow-lead/${id}`)
  ElMessage.success('已标记为跟进中')
  loadData()
}

async function invalidate(id) {
  await request.post(`/sales/workflow/invalidate-lead/${id}`)
  ElMessage.success('已标记为无效')
  loadData()
}

function openConvert(row) {
  convertLead.value = row
  convertCustomerId.value = customers.value.find(c => c.customerName === row.customerName)?.id || null
  convertVisible.value = true
}

async function doConvert() {
  if (!convertCustomerId.value) { ElMessage.warning('请选择客户'); return }
  const res = await request.post('/sales/workflow/convert-lead', {
    leadId: convertLead.value.id,
    customerId: convertCustomerId.value
  })
  ElMessage.success('已转为商机，请前往商机管理继续')
  convertVisible.value = false
  loadData()
  if (res.data?.id) router.push('/sales/opportunity')
}

function openWorkflow(row) {
  if (row.opportunityId) {
    workflowRef.value?.openByOpportunityId(row.opportunityId)
  } else {
    ElMessage.info(row.stageLabel || '请先跟进并转商机')
  }
}

function handleAction(cmd, row) {
  if (cmd === 'edit') openDialog(row)
  else if (cmd === 'invalidate') invalidate(row.id)
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
.hint { color: #64748b; margin-bottom: 12px; }
.danger-text { color: var(--el-color-danger); }
</style>
