<template>
  <CrudPage title="供应商管理" api="/base/suppliers" :columns="columns" :fields="fields" search-placeholder="搜索编码/名称/电话" />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const columns = [
  { prop: 'supplierCode', label: '编码', width: 130 },
  { prop: 'supplierName', label: '名称', minWidth: 140 },
  { prop: 'contactPerson', label: '联系人', width: 100 },
  { prop: 'contactPhone', label: '电话', width: 130 },
  { prop: 'score', label: '评分', width: 80, align: 'right' }
]
const fields = [
  { prop: 'supplierCode', label: '编码', placeholder: '留空自动生成' },
  { prop: 'supplierName', label: '名称', required: true },
  { prop: 'contactPerson', label: '联系人', required: true },
  { prop: 'contactPhone', label: '电话', required: true },
  { prop: 'bankAccount', label: '银行账号' }
]
</script>
