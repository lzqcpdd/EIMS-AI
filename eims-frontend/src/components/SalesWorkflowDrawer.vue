<template>
  <el-drawer v-model="visible" title="销售流程详情" size="760px" destroy-on-close>
    <SalesWorkflowPanel v-loading="loading" :detail="detail" />
  </el-drawer>
</template>

<script setup>
import SalesWorkflowPanel from '@/components/SalesWorkflowPanel.vue'
import { useSalesWorkflowDrawer } from '@/composables/useSalesWorkflowDrawer'

const { detail, visible, loading, openByOpportunityId, openByOrderId } = useSalesWorkflowDrawer()

defineExpose({ openByOpportunityId, openByOrderId })
</script>
