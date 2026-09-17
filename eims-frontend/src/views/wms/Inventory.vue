<template>
  <CrudPage
    title="库存查询"
    api="/wms/inventory"
    :columns="columns"
    search-placeholder="搜索物料编码/名称"
  />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const columns = [
  { prop: 'warehouseName', label: '仓库', width: 120 },
  { prop: 'materialCode', label: '物料编码', width: 140 },
  { prop: 'materialName', label: '物料名称', minWidth: 140 },
  { prop: 'quantity', label: '库存', width: 90, align: 'right' },
  { prop: 'lockedQuantity', label: '锁定', width: 80, align: 'right' },
  { prop: 'safetyStock', label: '安全库存', width: 100, align: 'right' },
  {
    prop: 'lowStock', label: '预警', width: 90, type: 'tag',
    tagFormatter: (v) => v
      ? { label: '低库存', type: 'danger' }
      : { label: '正常', type: 'success' }
  }
]
</script>
