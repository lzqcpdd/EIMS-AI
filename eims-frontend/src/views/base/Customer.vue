<template>
  <CrudPage title="客户管理" api="/base/customers" :columns="columns" :fields="fields" search-placeholder="搜索编码/名称/电话" />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const columns = [
  { prop: 'customerCode', label: '编码', width: 130 },
  { prop: 'customerName', label: '名称', minWidth: 140 },
  { prop: 'region', label: '区域', width: 100 },
  {
    prop: 'level', label: '等级', width: 90, type: 'tag',
    tagMap: [
      { value: 1, label: '普通', type: 'info' },
      { value: 2, label: '重点', type: 'warning' },
      { value: 3, label: '战略', type: 'success' }
    ]
  },
  { prop: 'contactPhone', label: '电话', width: 130 }
]
const fields = [
  { prop: 'customerCode', label: '编码', placeholder: '留空自动生成' },
  { prop: 'customerName', label: '名称', required: true },
  { prop: 'region', label: '区域', required: true },
  { prop: 'contactPerson', label: '联系人', required: true },
  { prop: 'contactPhone', label: '电话', required: true },
  {
    prop: 'level', label: '等级', type: 'select',
    options: [
      { value: 1, label: '普通' },
      { value: 2, label: '重点' },
      { value: 3, label: '战略' }
    ]
  }
]
</script>
