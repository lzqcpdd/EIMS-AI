<template>
  <div class="crud-page">
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="toolbar">
          <span class="title">{{ title }}</span>
          <div class="toolbar-right">
            <el-input
              v-if="searchable"
              v-model="keyword"
              class="search-input"
              clearable
              :placeholder="searchPlaceholder"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <el-button v-if="fields?.length" type="primary" @click="openDialog()">
              <el-icon><Plus /></el-icon>新增
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe class="data-table">
        <el-table-column
          v-for="col in columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :align="col.align"
          :show-overflow-tooltip="col.showOverflowTooltip !== false && col.type !== 'tag'"
        >
          <template #default="{ row }">
            <template v-if="col.type === 'tag'">
              <el-tag :type="resolveTag(row, col).type" size="small">
                {{ resolveTag(row, col).label }}
              </el-tag>
            </template>
            <template v-else>
              {{ col.formatter ? col.formatter(row[col.prop], row) : row[col.prop] }}
            </template>
          </template>
        </el-table-column>

        <el-table-column v-if="fields?.length" label="操作" :width="actionWidth" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
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

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑' : '新增'"
      width="520px"
      destroy-on-close
      class="form-dialog"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item
          v-for="field in visibleFields"
          :key="field.prop"
          :label="field.label"
          :prop="field.prop"
        >
          <el-input
            v-if="!field.type || field.type === 'text'"
            v-model="form[field.prop]"
            :placeholder="field.placeholder || `请输入${field.label}`"
            :disabled="field.disabled"
          />
          <el-input
            v-else-if="field.type === 'password'"
            v-model="form[field.prop]"
            type="password"
            show-password
            :placeholder="field.placeholder || `请输入${field.label}`"
          />
          <el-input-number
            v-else-if="field.type === 'number'"
            v-model="form[field.prop]"
            style="width:100%"
            :min="field.min ?? 0"
          />
          <el-select
            v-else-if="field.type === 'select'"
            v-model="form[field.prop]"
            style="width:100%"
            :placeholder="field.placeholder || `请选择${field.label}`"
            :filterable="field.filterable"
            :clearable="field.clearable !== false"
          >
            <el-option
              v-for="opt in getFieldOptions(field)"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const props = defineProps({
  title: String,
  api: String,
  columns: Array,
  fields: Array,
  defaultForm: { type: Object, default: () => ({}) },
  searchable: { type: Boolean, default: true },
  searchPlaceholder: { type: String, default: '输入关键字搜索' },
  actionWidth: { type: [Number, String], default: 160 }
})

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const keyword = ref('')
const formRef = ref()
const form = reactive({})
const page = reactive({ current: 1, size: 10, total: 0 })
const remoteOptions = reactive({})

const visibleFields = computed(() =>
  props.fields?.filter(field => !field.createOnly || !form.id) || []
)

const formRules = computed(() => {
  const rules = {}
  props.fields?.forEach(field => {
    if (!field.required) return
    if (field.createOnly && form.id) return
    if (field.type === 'number') {
      rules[field.prop] = [{
        validator: (_, value, callback) => {
          if (value === null || value === undefined || value === '') {
            callback(new Error(`${field.label}不能为空`))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }]
    } else {
      rules[field.prop] = [{
        required: true,
        message: `${field.label}不能为空`,
        trigger: 'blur'
      }]
    }
  })
  return rules
})

onMounted(async () => {
  await loadRemoteOptions()
  loadData()
})

async function loadRemoteOptions() {
  const tasks = props.fields
    ?.filter(field => field.type === 'select' && field.optionsUrl)
    .map(async field => {
      const params = field.listParams || { current: 1, size: 200 }
      const res = await request.get(field.optionsUrl, { params })
      const records = res.data.records || res.data || []
      const labelKey = field.optionLabel || 'label'
      const valueKey = field.optionValue || 'id'
      remoteOptions[field.prop] = records.map(item => ({
        label: typeof labelKey === 'function' ? labelKey(item) : item[labelKey],
        value: item[valueKey]
      }))
    }) || []
  await Promise.all(tasks)
}

function getFieldOptions(field) {
  if (field.optionsUrl) return remoteOptions[field.prop] || []
  return field.options || []
}

function resolveTag(row, col) {
  const value = row[col.prop]
  if (col.tagFormatter) return col.tagFormatter(value, row)
  const meta = (col.tagMap || []).find(item => item.value === value)
  return meta || { label: value ?? '-', type: 'info' }
}

async function loadData() {
  loading.value = true
  try {
    const params = { current: page.current, size: page.size }
    if (props.searchable && keyword.value) params.keyword = keyword.value
    const res = await request.get(props.api, { params })
    list.value = res.data.records
    page.total = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.current = 1
  loadData()
}

function resetForm() {
  formRef.value?.resetFields()
}

function openDialog(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, props.defaultForm, row ? { ...row } : {})
  visible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善必填项后再保存')
    return
  }
  saving.value = true
  try {
    if (form.id) await request.put(`${props.api}/${form.id}`, form)
    else await request.post(props.api, form)
    ElMessage.success('保存成功')
    visible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  await request.delete(`${props.api}/${id}`)
  ElMessage.success('删除成功')
  loadData()
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title { font-size: 16px; font-weight: 600; }
.search-input { width: 260px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.page-card {
  animation: card-enter 0.35s ease both;
}
@keyframes card-enter {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
