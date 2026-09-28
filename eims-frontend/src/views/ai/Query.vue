<template>
  <div class="ai-query">
    <!-- 顶部标题区 -->
    <div class="header-card">
      <h2>AI 智能数据问答</h2>
      <p class="subtitle">用自然语言提问，AI 自动生成 SQL 并查询数据库</p>
    </div>

    <!-- 输入区 -->
    <el-card class="input-card" shadow="never">
      <div class="input-row">
        <el-input
          v-model="question"
          placeholder="例如：上个月销售金额最高的客户是谁？"
          size="large"
          @keyup.enter="handleQuery"
        >
          <template #prefix>
            <el-icon><ChatDotRound /></el-icon>
          </template>
        </el-input>
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleQuery"
        >
          提问
        </el-button>
      </div>
      <!-- 快捷问题 -->
      <div class="quick-questions">
        <span class="quick-label">试试：</span>
        <el-button
          v-for="q in quickQuestions"
          :key="q"
          size="small"
          round
          @click="quickAsk(q)"
        >
          {{ q }}
        </el-button>
      </div>
    </el-card>

    <!-- 结果区 -->
    <div v-if="result" class="result-area">
      <!-- 生成的 SQL -->
      <el-card class="sql-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-tag type="success" size="small">AI 生成的 SQL</el-tag>
            <el-button link size="small" @click="copySql">
              <el-icon><CopyDocument /></el-icon> 复制
            </el-button>
          </div>
        </template>
        <pre class="sql-code">{{ result.sql }}</pre>
      </el-card>

      <!-- 查询结果表格 -->
      <el-card class="table-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-tag type="primary" size="small">查询结果（{{ result.rowCount }} 行）</el-tag>
          </div>
        </template>
        <el-table v-if="result.rows.length" :data="result.rows" stripe border>
          <el-table-column
            v-for="(col, idx) in columns"
            :key="idx"
            :prop="col"
            :label="formatLabel(col)"
            min-width="140"
          />
        </el-table>
        <el-empty v-else description="没有查到数据" />
      </el-card>
    </div>

    <!-- 初始状态提示 -->
    <el-empty v-else description="输入问题开始查询，AI 会自动把中文转成 SQL 查询你的业务数据" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { ChatDotRound, CopyDocument } from '@element-plus/icons-vue'

// 用户输入的问题
const question = ref('')
// 加载状态
const loading = ref(false)
// 查询结果
const result = ref(null)

// 快捷问题示例
const quickQuestions = [
  '上个月销售金额最高的客户是谁',
  '当前库存最低的 5 种物料',
  '今年各月的销售总额趋势',
  '有哪些待处理的采购申请'
]

// 从结果行数据里提取所有列名（第一行的 key 集合）
const columns = computed(() => {
  if (!result.value || !result.value.rows.length) return []
  return Object.keys(result.value.rows[0])
})

// 列名格式化：把 snake_case 转成可读的中文标签（简单处理）
function formatLabel(key) {
  // 常见字段映射
  const labelMap = {
    customer_name: '客户名称',
    supplier_name: '供应商名称',
    material_name: '物料名称',
    total_amount: '金额',
    quantity: '数量',
    order_code: '订单号',
    region: '区域',
    category: '分类',
    create_time: '创建时间'
  }
  return labelMap[key] || key
}

// 提交查询
async function handleQuery() {
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  loading.value = true
  try {
    // 调用后端 AI 查询接口
    const res = await request.post('/ai/query', { question: question.value.trim() })
    result.value = res.data
  } catch (e) {
    // request.js 已经统一弹错误提示了
  } finally {
    loading.value = false
  }
}

// 点击快捷问题
function quickAsk(q) {
  question.value = q
  handleQuery()
}

// 复制 SQL
function copySql() {
  navigator.clipboard.writeText(result.value.sql)
  ElMessage.success('SQL 已复制')
}
</script>

<style scoped>
.ai-query { max-width: 1000px; margin: 0 auto; }
.header-card { margin-bottom: 16px; }
.header-card h2 { margin: 0 0 4px; font-size: 20px; color: #1e293b; }
.subtitle { margin: 0; color: #64748b; font-size: 13px; }

.input-card { margin-bottom: 16px; }
.input-row { display: flex; gap: 12px; }
.input-row .el-input { flex: 1; }

.quick-questions { margin-top: 12px; display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
.quick-label { color: #94a3b8; font-size: 13px; }

.result-area { display: flex; flex-direction: column; gap: 16px; }

.sql-card :deep(.el-card__body) { padding: 0; }
.sql-code {
  margin: 0; padding: 16px;
  background: #1e293b; color: #e2e8f0;
  font-family: 'Consolas', 'Monaco', monospace; font-size: 13px;
  line-height: 1.6; overflow-x: auto;
  border-radius: 4px;
}

.card-header { display: flex; align-items: center; justify-content: space-between; }

.table-card :deep(.el-table) { font-size: 13px; }
</style>
