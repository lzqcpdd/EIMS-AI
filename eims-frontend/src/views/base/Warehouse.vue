<template>
  <CrudPage title="仓库管理" api="/base/warehouses" :columns="columns" :fields="fields" search-placeholder="搜索编码/名称/位置" />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const columns = [
  { prop: 'warehouseCode', label: '编码', width: 130 },
  { prop: 'warehouseName', label: '名称', minWidth: 140 },
  { prop: 'location', label: '位置', minWidth: 160 }
]
const fields = [
  { prop: 'warehouseCode', label: '编码', placeholder: '留空自动生成' },
  { prop: 'warehouseName', label: '名称', required: true },
  { prop: 'location', label: '位置', required: true }
]
</script>
