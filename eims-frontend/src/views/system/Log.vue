<template>
  <CrudPage
    title="操作日志"
    api="/system/log"
    :columns="columns"
    search-placeholder="搜索模块/操作/URI"
  />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const columns = [
  { prop: 'moduleName', label: '模块', width: 120 },
  { prop: 'operationType', label: '操作', width: 100 },
  { prop: 'requestUri', label: 'URI', minWidth: 180 },
  { prop: 'ipAddress', label: 'IP', width: 130 },
  {
    prop: 'operationResult', label: '结果', width: 80, type: 'tag',
    tagFormatter: (v) => v === 1
      ? { label: '成功', type: 'success' }
      : { label: '失败', type: 'danger' }
  },
  { prop: 'operationTime', label: '时间', width: 170 }
]
</script>
