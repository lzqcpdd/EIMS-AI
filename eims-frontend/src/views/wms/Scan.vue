<template>
  <div class="scan-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="header">
          <span class="title">扫码 / RFID 出入库</span>
          <el-tag type="info">库存变更将自动写入流水</el-tag>
        </div>
      </template>

      <el-row :gutter="24">
        <el-col :md="14" :span="24">
          <el-form :model="form" label-width="100px" class="scan-form">
            <el-form-item label="物料">
              <el-select
                v-model="form.materialCode"
                filterable
                placeholder="选择或搜索物料"
                style="width:100%"
                @change="loadCurrentStock"
              >
                <el-option
                  v-for="m in materials"
                  :key="m.id"
                  :label="`${m.materialName}（${m.materialCode}）`"
                  :value="m.materialCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="仓库">
              <el-select
                v-model="form.warehouseId"
                filterable
                placeholder="选择仓库"
                style="width:100%"
                @change="loadCurrentStock"
              >
                <el-option
                  v-for="w in warehouses"
                  :key="w.id"
                  :label="`${w.warehouseName}（${w.warehouseCode}）`"
                  :value="w.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="数量">
              <el-input-number v-model="form.quantity" :min="1" style="width:100%" />
            </el-form-item>
            <el-form-item label="类型">
              <el-radio-group v-model="form.type">
                <el-radio label="IN">入库</el-radio>
                <el-radio label="OUT">出库</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="选填" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="submit">确认提交</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :md="10" :span="24">
          <transition name="page-fade" mode="out-in">
            <el-card v-if="currentStock !== null" shadow="never" class="stock-panel" :key="stockKey">
              <div class="stock-title">当前库存</div>
              <div class="stock-value">{{ currentStock }}</div>
              <p class="stock-hint">提交后数量将变为 {{ predictedQuantity }}</p>
            </el-card>
            <el-empty v-else description="选择物料和仓库后显示库存" />
          </transition>

          <transition name="page-fade">
            <el-alert
              v-if="result"
              class="result-alert"
              :title="`流水号 ${result.recordCode}`"
              :description="`变动后库存：${result.afterQuantity}`"
              type="success"
              show-icon
              closable
              @close="result = null"
            />
          </transition>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const materials = ref([])
const warehouses = ref([])
const loading = ref(false)
const result = ref(null)
const currentStock = ref(null)
const stockKey = ref(0)
const form = reactive({
  materialCode: '',
  warehouseId: null,
  quantity: 1,
  type: 'IN',
  remark: ''
})

const predictedQuantity = computed(() => {
  const base = Number(currentStock.value || 0)
  const qty = Number(form.quantity || 0)
  return form.type === 'IN' ? base + qty : Math.max(base - qty, 0)
})

onMounted(async () => {
  await Promise.all([loadMaterials(), loadWarehouses()])
  if (materials.value.length) form.materialCode = materials.value[0].materialCode
  if (warehouses.value.length) form.warehouseId = warehouses.value[0].id
  loadCurrentStock()
})

async function loadMaterials() {
  const res = await request.get('/base/materials', { params: { current: 1, size: 200 } })
  materials.value = res.data.records || []
}

async function loadWarehouses() {
  const res = await request.get('/base/warehouses', { params: { current: 1, size: 200 } })
  warehouses.value = res.data.records || []
}

async function loadCurrentStock() {
  if (!form.materialCode || !form.warehouseId) {
    currentStock.value = null
    return
  }
  const res = await request.get('/wms/stock/by-code', {
    params: { materialCode: form.materialCode, warehouseId: form.warehouseId }
  })
  currentStock.value = res.data?.quantity ?? 0
  stockKey.value += 1
}

function resetForm() {
  form.quantity = 1
  form.type = 'IN'
  form.remark = ''
  result.value = null
  loadCurrentStock()
}

async function submit() {
  if (!form.materialCode || !form.warehouseId) {
    ElMessage.warning('请选择物料和仓库')
    return
  }
  loading.value = true
  try {
    const res = await request.post('/wms/scan', form)
    result.value = res.data
    ElMessage.success('操作成功')
    await loadCurrentStock()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.title { font-size: 16px; font-weight: 600; }
.scan-form { max-width: 560px; }
.stock-panel {
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  border: 1px solid #dbeafe;
  margin-bottom: 16px;
}
.stock-title { color: #64748b; font-size: 13px; }
.stock-value { font-size: 36px; font-weight: 700; color: #2563eb; margin: 8px 0; }
.stock-hint { color: #64748b; font-size: 13px; }
.result-alert { margin-top: 12px; }
.page-card { animation: card-enter 0.35s ease both; }
@keyframes card-enter {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
