<template>
  <div v-if="detail" class="workflow-panel">
    <el-steps :active="workflowActiveStep(detail.status)" finish-status="success" align-center>
      <el-step v-for="step in WORKFLOW_STEPS" :key="step" :title="step" />
    </el-steps>
    <el-descriptions :column="2" border size="small" class="meta">
      <el-descriptions-item label="申请单号">{{ detail.requestCode }}</el-descriptions-item>
      <el-descriptions-item label="当前状态">
        <el-tag :type="statusTagType(detail.status)">{{ detail.statusLabel }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="发起人">{{ detail.applicantName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="申请部门">{{ detail.deptName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="当前环节" :span="2">
        <span class="stage">{{ detail.stageLabel }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="总金额">¥ {{ detail.totalAmount }}</el-descriptions-item>
      <el-descriptions-item label="提交时间">{{ detail.submitTime || '—' }}</el-descriptions-item>
      <el-descriptions-item v-if="detail.remark" label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
    </el-descriptions>

    <div class="section-title">采购物料明细</div>
    <el-table :data="detail.items || []" size="small" border>
      <el-table-column prop="materialCode" label="物料编码" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column prop="unitPrice" label="单价" width="100" />
      <el-table-column label="小计" width="100">
        <template #default="{ row }">¥ {{ lineAmount(row) }}</template>
      </el-table-column>
    </el-table>

    <template v-if="detail.approvals?.length">
      <div class="section-title">审批记录</div>
      <el-timeline>
        <el-timeline-item
          v-for="item in detail.approvals"
          :key="item.id"
          :type="item.result === 1 ? 'success' : 'danger'"
          :timestamp="item.approvalTime"
        >
          <strong>{{ item.approvalLevelLabel }}</strong> — {{ item.approverName }}
          <el-tag size="small" :type="item.result === 1 ? 'success' : 'danger'" style="margin-left:8px">
            {{ item.resultLabel }}
          </el-tag>
          <div v-if="item.comment" class="comment">{{ item.comment }}</div>
        </el-timeline-item>
      </el-timeline>
    </template>
  </div>
</template>

<script setup>
import { WORKFLOW_STEPS, workflowActiveStep, statusTagType, lineAmount } from '@/utils/purchaseWorkflow'

defineProps({
  detail: { type: Object, default: null }
})
</script>

<style scoped>
.workflow-panel { display: flex; flex-direction: column; gap: 16px; }
.meta { margin-top: 8px; }
.stage { color: #d97706; font-weight: 500; }
.section-title { font-weight: 600; margin-top: 8px; }
.comment { color: #64748b; margin-top: 4px; font-size: 13px; }
</style>
