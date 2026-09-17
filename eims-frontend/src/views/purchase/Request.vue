<template>
  <div class="crud-page">
    <el-card shadow="never">
      <template #header>
        <div class="toolbar">
          <span class="title">采购申请</span>
          <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增申请</el-button>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="requestCode" label="申请单号" width="160" />
        <el-table-column prop="applicantName" label="发起人" width="100" />
        <el-table-column prop="deptName" label="部门" width="100" />
        <el-table-column prop="totalAmount" label="总金额" width="110">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stageLabel" label="当前环节" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">流程详情</el-button>
            <el-button v-if="Number(row.status) === 0" link type="warning" @click="submit(row.id)">提交审批</el-button>
            <el-button v-if="Number(row.status) === 0" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm v-if="Number(row.status) === 0" title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference><el-button link type="danger">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pager" v-model:current-page="page.current" v-model:page-size="page.size"
        :total="page.total" layout="total, prev, pager, next" @current-change="loadData" />
    </el-card>

    <!-- 新增/编辑 -->
    <el-dialog v-model="visible" :title="form.id ? '编辑采购申请' : '新增采购申请'" width="860px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <div class="items-header">
          <span class="items-title">采购物料明细 <em>*</em></span>
          <el-button type="primary" link @click="addItem"><el-icon><Plus /></el-icon>添加物料</el-button>
        </div>
        <el-table :data="form.items" border size="small">
          <el-table-column label="物料" min-width="220">
            <template #default="{ row, $index }">
              <el-select
                v-model="row.materialId"
                filterable
                placeholder="选择物料"
                style="width:100%"
                @change="(id) => onMaterialChange(row, id, $index)"
              >
                <el-option
                  v-for="m in materials"
                  :key="m.id"
                  :label="`${m.materialCode} - ${m.materialName}`"
                  :value="m.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="materialCode" label="编码" width="130" />
          <el-table-column prop="materialName" label="名称" width="130" />
          <el-table-column label="数量" width="120">
            <template #default="{ row, $index }">
              <el-input-number v-model="row.quantity" :min="1" size="small" style="width:100%" @change="validateItemRow($index)" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="130">
            <template #default="{ row, $index }">
              <el-input-number v-model="row.unitPrice" :min="0.01" :precision="2" size="small" style="width:100%" @change="validateItemRow($index)" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="100">
            <template #default="{ row }">¥ {{ lineAmount(row) }}</template>
          </el-table-column>
          <el-table-column label="" width="60">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.items.splice($index, 1)">删</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="total-bar">合计金额：<strong>¥ {{ totalAmount }}</strong></div>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 流程详情 -->
    <el-drawer v-model="detailVisible" title="采购申请流程" size="640px">
      <PurchaseWorkflowPanel :detail="detail" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import PurchaseWorkflowPanel from '@/components/PurchaseWorkflowPanel.vue'
import { statusTagType, lineAmount, calcTotalAmount } from '@/utils/purchaseWorkflow'

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const materials = ref([])
const formRef = ref()
const form = reactive({ id: null, remark: '', items: [] })
const page = reactive({ current: 1, size: 10, total: 0 })

const formRules = {
  items: [{
    validator: (_, __, callback) => {
      if (!form.items.length) callback(new Error('请至少添加一条采购物料明细'))
      else callback()
    },
    trigger: 'change'
  }]
}

const totalAmount = computed(() => calcTotalAmount(form.items))

onMounted(() => {
  loadData()
  loadMaterials()
})

async function loadMaterials() {
  const res = await request.get('/base/materials', { params: { current: 1, size: 200 } })
  materials.value = res.data.records
}

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/purchase/requests', { params: { current: page.current, size: page.size } })
    list.value = res.data.records
    page.total = res.data.total
  } finally {
    loading.value = false
  }
}

function emptyItem() {
  return { materialId: null, materialCode: '', materialName: '', quantity: 1, unitPrice: null }
}

function addItem() {
  form.items.push(emptyItem())
}

function onMaterialChange(row, materialId, index) {
  const m = materials.value.find(x => x.id === materialId)
  if (!m) return
  row.materialCode = m.materialCode
  row.materialName = m.materialName
  validateItemRow(index)
}

function validateItemRow(index) {
  formRef.value?.validateField('items').catch(() => {})
}

function openDialog(row) {
  if (row?.id) {
    loadDetail(row.id).then(data => {
      form.id = data.id
      form.remark = data.remark || ''
      form.items = data.items?.length
        ? data.items.map(i => ({ ...i }))
        : [emptyItem()]
      visible.value = true
    })
  } else {
    form.id = null
    form.remark = ''
    form.items = [emptyItem()]
    visible.value = true
  }
}

async function openDetail(id) {
  detail.value = await loadDetail(id)
  detailVisible.value = true
}

async function loadDetail(id) {
  const res = await request.get(`/purchase/requests/${id}`)
  return res.data
}

async function handleSave() {
  if (!form.items.length) {
    ElMessage.warning('请至少添加一条采购物料明细')
    return
  }
  for (let i = 0; i < form.items.length; i++) {
    const row = form.items[i]
    if (!row.materialId) { ElMessage.warning(`第 ${i + 1} 行：请选择物料`); return }
    if (!row.quantity || row.quantity <= 0) { ElMessage.warning(`第 ${i + 1} 行：数量必须大于0`); return }
    if (!row.unitPrice || row.unitPrice <= 0) { ElMessage.warning(`第 ${i + 1} 行：单价必须大于0`); return }
  }
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填项后再保存')
    return
  }
  const payload = { remark: form.remark, items: form.items }
  if (form.id) await request.put(`/purchase/requests/${form.id}`, payload)
  else await request.post('/purchase/requests', payload)
  ElMessage.success('保存成功')
  visible.value = false
  loadData()
}

async function handleDelete(id) {
  await request.delete(`/purchase/requests/${id}`)
  ElMessage.success('删除成功')
  loadData()
}

async function submit(id) {
  await request.post(`/purchase/workflow/submit/${id}`)
  ElMessage.success('已提交审批，请前往「采购审批」处理')
  loadData()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.pager { margin-top: 16px; justify-content: flex-end; }
.items-header { display: flex; justify-content: space-between; align-items: center; margin: 12px 0 8px; }
.items-title { font-weight: 600; }
.items-title em { color: #f56c6c; font-style: normal; }
.total-bar { text-align: right; margin-top: 12px; font-size: 15px; }
.total-bar strong { color: #2563eb; font-size: 18px; }
</style>
