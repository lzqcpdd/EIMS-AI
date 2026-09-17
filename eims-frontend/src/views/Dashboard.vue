<template>
  <div class="dashboard">
    <!-- KPI 卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="item in statCards" :key="item.key">
        <div class="stat-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="stat-value">{{ formatValue(item, stats[item.key]) }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表第一行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>近7日库存变动</template>
          <div ref="trendRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>库存分类占比</template>
          <div ref="pieRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表第二行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>近30日销售/采购金额</template>
          <div ref="amountRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>各仓库库存占比</template>
          <div ref="warehouseRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表第三行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>销售漏斗</template>
          <div ref="funnelRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>客户区域分布</template>
          <div ref="regionRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据列表 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>低库存预警</span>
            <el-tag type="danger" size="small" style="margin-left:8px">{{ stats.lowStockCount ?? 0 }} 项</el-tag>
          </template>
          <el-table :data="stats.lowStockItems || []" size="small" stripe empty-text="暂无低库存物料">
            <el-table-column label="物料名称">
              <template #default="{ row }">{{ row.material_name || row.materialName }}</template>
            </el-table-column>
            <el-table-column label="当前库存" width="90">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column label="安全库存" width="90">
              <template #default="{ row }">{{ row.safety_stock ?? row.safetyStock }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>待处理事项</template>
          <el-table :data="stats.pendingTasks || []" size="small" stripe empty-text="暂无待处理事项">
            <el-table-column prop="type" label="类型" width="90" />
            <el-table-column prop="code" label="单号" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">¥{{ Number(row.amount || 0).toLocaleString() }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统概览 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>系统概览</template>
          <el-descriptions :column="4" border>
            <el-descriptions-item label="项目名称">EIMS 企业综合管理平台</el-descriptions-item>
            <el-descriptions-item label="版本">V1.0</el-descriptions-item>
            <el-descriptions-item label="技术栈">Vue3 + Spring Boot + MySQL</el-descriptions-item>
            <el-descriptions-item label="本月销售额">¥{{ formatMoney(stats.monthlySalesAmount) }}</el-descriptions-item>
            <el-descriptions-item label="本月采购额">¥{{ formatMoney(stats.monthlyPurchaseAmount) }}</el-descriptions-item>
            <el-descriptions-item label="待审批采购">{{ stats.purchasePending ?? 0 }} 单</el-descriptions-item>
            <el-descriptions-item label="销售订单">{{ stats.salesOrderCount ?? 0 }} 单</el-descriptions-item>
            <el-descriptions-item label="待发货">{{ stats.deliveryPending ?? 0 }} 单</el-descriptions-item>
            <el-descriptions-item label="活跃商机">{{ stats.opportunityCount ?? 0 }} 个</el-descriptions-item>
            <el-descriptions-item label="待跟进线索">{{ stats.leadCount ?? 0 }} 条</el-descriptions-item>
            <el-descriptions-item label="进行中盘点">{{ stats.stocktakeActive ?? 0 }} 单</el-descriptions-item>
            <el-descriptions-item label="低库存预警">{{ stats.lowStockCount ?? 0 }} 项</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近操作 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>最近操作日志</template>
          <el-table :data="stats.recentLogs || []" size="small" stripe empty-text="暂无操作记录">
            <el-table-column label="模块" width="120">
              <template #default="{ row }">{{ row.module_name || row.moduleName }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">{{ row.operation_type || row.operationType }}</template>
            </el-table-column>
            <el-table-column label="操作人" width="120">
              <template #default="{ row }">{{ row.operator || row.nickName }}</template>
            </el-table-column>
            <el-table-column label="时间">
              <template #default="{ row }">{{ row.operation_time || row.operationTime }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import request from '@/utils/request'

const stats = ref({})
const trendRef = ref()
const pieRef = ref()
const amountRef = ref()
const warehouseRef = ref()
const funnelRef = ref()
const regionRef = ref()

let charts = []
let echarts

const statCards = [
  { key: 'supplierCount', label: '供应商', color: '#2563eb' },
  { key: 'customerCount', label: '客户', color: '#7c3aed' },
  { key: 'materialCount', label: '物料', color: '#0891b2' },
  { key: 'inventoryTotal', label: '库存总量', color: '#059669' },
  { key: 'purchasePending', label: '待审批采购', color: '#dc2626' },
  { key: 'salesOrderCount', label: '销售订单', color: '#ea580c' },
  { key: 'lowStockCount', label: '低库存预警', color: '#d97706' },
  { key: 'deliveryPending', label: '待发货', color: '#9333ea' },
  { key: 'monthlySalesAmount', label: '本月销售额', color: '#16a34a', money: true },
  { key: 'monthlyPurchaseAmount', label: '本月采购额', color: '#0284c7', money: true },
  { key: 'opportunityCount', label: '活跃商机', color: '#c026d3' },
  { key: 'leadCount', label: '待跟进线索', color: '#64748b' }
]

const COLORS = ['#2563eb', '#7c3aed', '#0891b2', '#059669', '#dc2626', '#ea580c', '#d97706', '#9333ea']

function pick(obj, ...keys) {
  for (const k of keys) if (obj != null && obj[k] != null) return obj[k]
  return undefined
}

function fmtDateLabel(d) {
  if (!d) return ''
  if (typeof d === 'string') return d.length >= 10 ? d.slice(5, 10) : d
  if (Array.isArray(d) && d.length >= 3) {
    return `${String(d[1]).padStart(2, '0')}-${String(d[2]).padStart(2, '0')}`
  }
  return String(d)
}

function formatMoney(v) {
  return Number(v || 0).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

function formatValue(item, v) {
  if (v == null) return '-'
  return item.money ? '¥' + formatMoney(v) : v
}

onMounted(async () => {
  const mod = await import('echarts')
  echarts = mod.default ?? mod
  const res = await request.get('/dashboard/stats')
  stats.value = res.data
  await nextTick()
  initCharts(res.data)
  window.addEventListener('resize', resize)
})

onUnmounted(() => {
  window.removeEventListener('resize', resize)
  charts.forEach(c => c?.dispose())
})

function resize() { charts.forEach(c => c?.resize()) }

function initChart(el, option) {
  if (!el) return null
  const chart = echarts.init(el)
  chart.setOption(option)
  charts.push(chart)
  return chart
}

function initCharts(data) {
  charts = []

  const stockTrend = data.stockTrend || []
  initChart(trendRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: stockTrend.map(i => fmtDateLabel(pick(i, 'date'))) },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar', name: '变动量',
      data: stockTrend.map(i => Number(pick(i, 'change_qty', 'changeQty') || 0)),
      itemStyle: { color: '#2563eb', borderRadius: [4, 4, 0, 0] }
    }]
  })

  const categoryStock = data.categoryStock || []
  initChart(pieRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie', radius: ['40%', '65%'],
      data: categoryStock.map((i, idx) => ({
        name: pick(i, 'category') || '未分类',
        value: Number(pick(i, 'total') || 0),
        itemStyle: { color: COLORS[idx % COLORS.length] }
      }))
    }]
  })

  const amountData = data.amountTrend || []
  initChart(amountRef.value, {
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '采购额'], top: 0 },
    grid: { left: 60, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: amountData.map(i => fmtDateLabel(pick(i, 'date'))) },
    yAxis: { type: 'value', axisLabel: { formatter: v => (v >= 10000 ? (v / 10000) + '万' : v) } },
    series: [
      { type: 'line', name: '销售额', smooth: true, data: amountData.map(i => Number(pick(i, 'sales_amount', 'salesAmount') || 0)), itemStyle: { color: '#16a34a' } },
      { type: 'line', name: '采购额', smooth: true, data: amountData.map(i => Number(pick(i, 'purchase_amount', 'purchaseAmount') || 0)), itemStyle: { color: '#2563eb' } }
    ]
  })

  const warehouseStock = data.warehouseStock || []
  initChart(warehouseRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: '65%',
      data: warehouseStock.map((i, idx) => ({
        name: pick(i, 'name', 'warehouseName') || '未知仓库',
        value: Number(pick(i, 'total') || 0),
        itemStyle: { color: COLORS[idx % COLORS.length] }
      }))
    }]
  })

  const salesFunnel = data.salesFunnel || []
  initChart(funnelRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    series: [{
      type: 'funnel', left: '10%', width: '80%', sort: 'descending',
      label: { show: true, position: 'inside' },
      data: salesFunnel.map(i => ({
        name: pick(i, 'stage'),
        value: Number(pick(i, 'count') || 0)
      }))
    }]
  })

  const customerRegion = data.customerRegion || []
  initChart(regionRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: customerRegion.map(i => pick(i, 'region')) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: customerRegion.map(i => Number(pick(i, 'total') || 0)),
      itemStyle: { color: '#7c3aed', borderRadius: [4, 4, 0, 0] }
    }]
  })
}
</script>

<style scoped>
.stat-row { margin-bottom: 4px; }
.stat-row .el-col { margin-bottom: 16px; }
.chart-row { margin-top: 0; }
.chart-row .el-col { margin-bottom: 16px; }
.stat-card { background: #fff; border-radius: 8px; padding: 18px 20px; box-shadow: 0 1px 3px rgba(0,0,0,.06); }
.stat-value { font-size: 24px; font-weight: 700; color: #0f172a; }
.stat-label { color: #64748b; margin-top: 6px; font-size: 13px; }
.chart-box { height: 300px; }
</style>
