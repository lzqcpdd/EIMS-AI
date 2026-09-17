<template>
  <CrudPage title="物料管理" api="/base/materials" :columns="columns" :fields="fields" search-placeholder="搜索编码/名称/分类" />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const columns = [
  { prop: 'materialCode', label: '编码', width: 140 },
  { prop: 'materialName', label: '名称', minWidth: 140 },
  { prop: 'category', label: '分类', width: 100 },
  { prop: 'spec', label: '规格', minWidth: 120 },
  { prop: 'unit', label: '单位', width: 70 },
  { prop: 'safetyStock', label: '安全库存', width: 100, align: 'right' },
  {
    prop: 'status', label: '状态', width: 80, type: 'tag',
    tagMap: [
      { value: 1, label: '启用', type: 'success' },
      { value: 0, label: '停用', type: 'info' }
    ]
  }
]
const fields = [
  { prop: 'materialCode', label: '编码', placeholder: '留空自动生成' },
  { prop: 'materialName', label: '名称', required: true },
  { prop: 'category', label: '分类', required: true },
  { prop: 'spec', label: '规格' },
  { prop: 'unit', label: '单位', required: true },
  { prop: 'safetyStock', label: '安全库存', type: 'number', min: 0 },
  {
    prop: 'status', label: '状态', type: 'select',
    options: [
      { value: 1, label: '启用' },
      { value: 0, label: '停用' }
    ]
  }
]
</script>
