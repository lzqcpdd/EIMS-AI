<template>
  <div v-if="detail" class="workflow-panel">
    <el-steps :active="workflowActiveStep(detail)" finish-status="success" align-center>
      <el-step v-for="(step, idx) in WORKFLOW_STEPS" :key="step" :title="step"
        :status="stepStatus(idx)" />
    </el-steps>

    <el-alert
      :title="`当前环节：${detail.currentStepName || WORKFLOW_STEPS[workflowActiveStep(detail)]}`"
      :description="detail.stageLabel"
      type="warning"
      :closable="false"
      show-icon
      class="current-alert"
    />

    <el-descriptions :column="2" border size="small" class="meta">
      <el-descriptions-item label="商机编号">{{ detail.opportunityCode }}</el-descriptions-item>
      <el-descriptions-item label="商机阶段">
        <el-tag :type="findMeta(OPP_STAGE, detail.stage).type">
          {{ findMeta(OPP_STAGE, detail.stage).label }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="客户">{{ detail.customerName || '—' }}</el-descriptions-item>
      <el-descriptions-item label="预计金额">¥ {{ detail.expectedAmount }}</el-descriptions-item>
      <el-descriptions-item label="来源线索">{{ detail.leadCode || '—' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ detail.createTime || '—' }}</el-descriptions-item>
    </el-descriptions>

    <template v-if="detail.quotations?.length">
      <div class="section-title">销售报价</div>
      <el-table :data="detail.quotations" size="small" border>
        <el-table-column prop="quotationCode" label="报价单号" />
        <el-table-column prop="totalAmount" label="金额" width="110">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="findMeta(QUOTE_STATUS, row.status).type">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="validUntil" label="有效期" width="170" />
      </el-table>
    </template>

    <template v-if="detail.contracts?.length">
      <div class="section-title">销售合同</div>
      <el-table :data="detail.contracts" size="small" border>
        <el-table-column prop="contractCode" label="合同编号" />
        <el-table-column prop="totalAmount" label="金额" width="110">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="signStatus" label="签章状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="findMeta(CONTRACT_SIGN, row.signStatus).type">{{ row.signStatusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="signTime" label="签章时间" width="170" />
      </el-table>
    </template>

    <template v-if="detail.orders?.length">
      <div class="section-title">销售订单</div>
      <el-table :data="detail.orders" size="small" border>
        <el-table-column prop="orderCode" label="订单号" />
        <el-table-column prop="totalAmount" label="金额" width="110">
          <template #default="{ row }">¥ {{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="findMeta(ORDER_STATUS, row.status).type">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="回款" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="findMeta(PAYMENT_STATUS, row.paymentStatus).type">{{ row.paymentStatusLabel }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template v-if="detail.deliveries?.length">
      <div class="section-title">发货记录</div>
      <el-table :data="detail.deliveries" size="small" border>
        <el-table-column prop="deliveryCode" label="发货单号" />
        <el-table-column prop="orderCode" label="订单号" width="140" />
        <el-table-column prop="statusLabel" label="状态" width="100" />
        <el-table-column prop="logisticsNo" label="物流单号" min-width="140" />
      </el-table>
    </template>

    <template v-if="detail.afterServices?.length">
      <div class="section-title">售后服务</div>
      <el-table :data="detail.afterServices" size="small" border>
        <el-table-column prop="serviceCode" label="售后单号" />
        <el-table-column prop="serviceTypeLabel" label="类型" width="100" />
        <el-table-column prop="statusLabel" label="状态" width="100" />
        <el-table-column prop="content" label="内容" min-width="160" show-overflow-tooltip />
      </el-table>
    </template>

    <template v-if="detail.timeline?.length">
      <div class="section-title">流程记录</div>
      <el-timeline>
        <el-timeline-item
          v-for="(item, idx) in detail.timeline"
          :key="idx"
          :type="item.type"
          :timestamp="item.eventTime"
        >
          <el-tag size="small" effect="plain" style="margin-right:6px">{{ item.stepName }}</el-tag>
          <strong>{{ item.title }}</strong>
          <div v-if="item.description" class="comment">{{ item.description }}</div>
        </el-timeline-item>
      </el-timeline>
    </template>
  </div>
</template>

<script setup>
import {
  WORKFLOW_STEPS, OPP_STAGE, QUOTE_STATUS, CONTRACT_SIGN, ORDER_STATUS, PAYMENT_STATUS,
  workflowActiveStep, findMeta
} from '@/utils/salesWorkflow'

const props = defineProps({
  detail: { type: Object, default: null }
})

function stepStatus(idx) {
  const active = workflowActiveStep(props.detail)
  if (idx < active) return 'success'
  if (idx === active) return 'process'
  return 'wait'
}
</script>

<style scoped>
.workflow-panel { display: flex; flex-direction: column; gap: 16px; }
.current-alert { margin-top: 4px; }
.meta { margin-top: 0; }
.section-title { font-weight: 600; margin-top: 8px; }
.comment { color: #64748b; margin-top: 4px; font-size: 13px; }
</style>
