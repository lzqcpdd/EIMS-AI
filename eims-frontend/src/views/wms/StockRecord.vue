<template>
  <CrudPage
    title="库存流水"
    api="/wms/stock-records"
    :columns="columns"
    search-placeholder="搜索流水号/物料"
  />
</template>
<script setup>
import CrudPage from '@/components/CrudPage.vue'

const columns = [
  { prop: 'recordCode', label: '流水号', width: 150 },
  { prop: 'warehouseName', label: '仓库', width: 110 },
  { prop: 'materialCode', label: '物料编码', width: 130 },
  { prop: 'materialName', label: '物料名称', minWidth: 120 },
  { prop: 'businessTypeLabel', label: '类型', width: 100 },
  { prop: 'quantity', label: '数量', width: 80, align: 'right' },
  { prop: 'beforeQuantity', label: '变动前', width: 80, align: 'right' },
  { prop: 'afterQuantity', label: '变动后', width: 80, align: 'right' },
  { prop: 'createTime', label: '时间', width: 170 }
]
</script>
