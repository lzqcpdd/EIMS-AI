<template>
  <CrudPage
    title="编码规则"
    api="/base/code-rules"
    :columns="columns"
    :fields="fields"
    :default-form="defaultForm"
    search-placeholder="搜索规则码/名称"
  />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const defaultForm = {
  datePattern: 'yyyyMMdd',
  seqLength: 4,
  currentSeq: 0,
  status: 1
}

const columns = [
  { prop: 'ruleCode', label: '规则码', width: 120 },
  { prop: 'ruleName', label: '名称', minWidth: 120 },
  { prop: 'prefix', label: '前缀', width: 80 },
  { prop: 'datePattern', label: '日期格式', width: 110 },
  { prop: 'seqLength', label: '序号长度', width: 90, align: 'right' },
  { prop: 'currentSeq', label: '当前序号', width: 90, align: 'right' },
  {
    prop: 'status', label: '状态', width: 80, type: 'tag',
    tagMap: [
      { value: 1, label: '启用', type: 'success' },
      { value: 0, label: '停用', type: 'info' }
    ]
  }
]

const fields = [
  { prop: 'ruleCode', label: '规则码', required: true },
  { prop: 'ruleName', label: '名称', required: true },
  { prop: 'prefix', label: '前缀', required: true },
  { prop: 'datePattern', label: '日期格式' },
  { prop: 'seqLength', label: '序号长度', type: 'number' },
  { prop: 'currentSeq', label: '当前序号', type: 'number' }
]
</script>
