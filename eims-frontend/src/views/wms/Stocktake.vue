<template>
  <div class="stocktake-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="toolbar">
          <span class="title">盘点管理</span>
          <div class="toolbar-right">
            <el-input
              v-model="keyword"
              class="search-input"
              clearable
              placeholder="搜索盘点单号"
              @keyup.enter="loadData"
              @clear="loadData"
            >
              <template #append>
                <el-button @click="loadData">搜索</el-button>
              </template>
            </el-input>
            <el-button type="primary" @click="openCreate()">
              <el-icon><Plus /></el-icon>新建盘点
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="stocktakeCode" label="盘点单号" width="150" />
        <el-table-column prop="warehouseName" label="仓库" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openDetail(row)">盘点明细</el-button>
              <el-popconfirm v-if="row.status < 2" title="确认删除?" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
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

    <el-dialog v-model="createVisible" title="新建盘点单" width="480px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="仓库" required>
          <el-select v-model="createForm.warehouseId" filterable placeholder="选择仓库" style="width:100%">
            <el-option
              v-for="w in warehouses"
              :key="w.id"
              :label="`${w.warehouseName}（${w.warehouseCode}）`"
              :value="w.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createStocktake">创建</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" :title="`盘点明细 - ${detail?.stocktakeCode || ''}`" size="860px" destroy-on-close>
      <template v-if="detail">
        <el-descriptions :column="3" border size="small" class="summary">
          <el-descriptions-item label="盘点单号">{{ detail.stocktakeCode }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ detail.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.status)" size="small">{{ detail.statusLabel }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.status < 2" class="detail-toolbar">
          <el-button @click="initItems">载入仓库库存</el-button>
          <el-button type="primary" @click="openItemDialog()">添加明细</el-button>
          <el-button type="warning" :disabled="!detail.items?.length" @click="completeStocktake">完成盘点</el-button>
          <el-button type="success" :disabled="detail.status !== 1" @click="confirmStocktake">确认调整库存</el-button>
        </div>

        <el-table :data="detail.items" stripe size="small" class="item-table">
          <el-table-column prop="materialCode" label="物料编码" width="140" />
          <el-table-column prop="materialName" label="物料名称" min-width="140" />
          <el-table-column prop="systemQuantity" label="账面数量" width="100" align="right" />
          <el-table-column prop="actualQuantity" label="实盘数量" width="100" align="right" />
          <el-table-column prop="diffQuantity" label="差异" width="90" align="right">
            <template #default="{ row }">
              <span :class="diffClass(row.diffQuantity)">{{ formatDiff(row.diffQuantity) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="detail.status < 2" label="操作" width="120">
            <template #default="{ row }">
              <el-button link type="primary" @click="openItemDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除?" @confirm="deleteItem(row.id)">
                <template #reference>
                  <el-button link type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>

    <el-dialog v-model="itemVisible" :title="itemForm.materialId ? '编辑盘点明细' : '添加盘点明细'" width="480px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="物料" required>
          <el-select
            v-model="itemForm.materialId"
            filterable
            :disabled="!!itemForm.id"
            placeholder="选择物料"
            style="width:100%"
          >
            <el-option
              v-for="m in materials"
              :key="m.id"
              :label="`${m.materialName}（${m.materialCode}）`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="实盘数量" required>
          <el-input-number v-model="itemForm.actualQuantity" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const warehouses = ref([])
const materials = ref([])
const detail = ref(null)
const loading = ref(false)
const creating = ref(false)
const createVisible = ref(false)
const detailVisible = ref(false)
const itemVisible = ref(false)
const keyword = ref('')
const page = reactive({ current: 1, size: 10, total: 0 })
const createForm = reactive({ warehouseId: null })
const itemForm = reactive({ id: null, materialId: null, actualQuantity: 0 })

onMounted(async () => {
  await Promise.all([loadWarehouses(), loadMaterials()])
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (keyword.value) params.keyword = keyword.value
    const res = await request.get('/wms/stocktake', { params })
    list.value = res.data.records
    page.total = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadWarehouses() {
  const res = await request.get('/base/warehouses', { params: { current: 1, size: 200 } })
  warehouses.value = res.data.records || []
}

async function loadMaterials() {
  const res = await request.get('/base/materials', { params: { current: 1, size: 500 } })
  materials.value = res.data.records || []
}

function statusLabel(status) {
  return ['进行中', '已完成', '已确认'][status] || '未知'
}

function statusType(status) {
  return ['warning', 'success', 'info'][status] || 'info'
}

function formatDiff(value) {
  if (value > 0) return `+${value}`
  return String(value ?? 0)
}

function diffClass(value) {
  if (value > 0) return 'diff-plus'
  if (value < 0) return 'diff-minus'
  return ''
}

function openCreate() {
  createForm.warehouseId = warehouses.value[0]?.id || null
  createVisible.value = true
}

async function createStocktake() {
  if (!createForm.warehouseId) {
    ElMessage.warning('请选择仓库')
    return
  }
  creating.value = true
  try {
    const res = await request.post('/wms/stocktake', { warehouseId: createForm.warehouseId })
    ElMessage.success('盘点单已创建')
    createVisible.value = false
    loadData()
    openDetail({ id: res.data.id })
  } finally {
    creating.value = false
  }
}

async function openDetail(row) {
  const res = await request.get(`/wms/stocktake/${row.id}/detail`)
  detail.value = res.data
  detailVisible.value = true
}

async function reloadDetail() {
  if (!detail.value?.id) return
  const res = await request.get(`/wms/stocktake/${detail.value.id}/detail`)
  detail.value = res.data
  loadData()
}

async function initItems() {
  const res = await request.post(`/wms/stocktake/${detail.value.id}/init-items`)
  ElMessage.success(`已载入 ${res.data} 条库存明细`)
  reloadDetail()
}

function openItemDialog(row) {
  itemForm.id = row?.id || null
  itemForm.materialId = row?.materialId || null
  itemForm.actualQuantity = row?.actualQuantity ?? 0
  itemVisible.value = true
}

async function saveItem() {
  if (!itemForm.materialId) {
    ElMessage.warning('请选择物料')
    return
  }
  await request.post(`/wms/stocktake/${detail.value.id}/items`, {
    materialId: itemForm.materialId,
    actualQuantity: itemForm.actualQuantity
  })
  ElMessage.success('明细已保存')
  itemVisible.value = false
  reloadDetail()
}

async function deleteItem(itemId) {
  await request.delete(`/wms/stocktake/items/${itemId}`)
  ElMessage.success('已删除')
  reloadDetail()
}

async function completeStocktake() {
  await request.post(`/wms/stocktake/${detail.value.id}/complete`)
  ElMessage.success('盘点已完成，可确认调整库存')
  reloadDetail()
}

async function confirmStocktake() {
  await ElMessageBox.confirm('确认后将按差异自动调整库存并写入流水，是否继续？', '确认调整', { type: 'warning' })
  await request.post(`/wms/stocktake/${detail.value.id}/confirm`)
  ElMessage.success('库存已调整，盘点流程结束')
  reloadDetail()
}

async function handleDelete(id) {
  await request.delete(`/wms/stocktake/${id}`)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.toolbar-right { display: flex; align-items: center; gap: 12px; }
.title { font-size: 16px; font-weight: 600; }
.search-input { width: 260px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.summary { margin-bottom: 16px; }
.detail-toolbar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.item-table { margin-top: 8px; }
.diff-plus { color: #16a34a; font-weight: 600; }
.diff-minus { color: #dc2626; font-weight: 600; }
.page-card { animation: card-enter 0.35s ease both; }
@keyframes card-enter {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
