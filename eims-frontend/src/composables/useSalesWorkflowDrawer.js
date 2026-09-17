import { ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

/** 打开销售商机流程详情抽屉 */
export function useSalesWorkflowDrawer() {
  const detail = ref(null)
  const visible = ref(false)
  const loading = ref(false)

  async function openByOpportunityId(opportunityId) {
    if (!opportunityId) {
      ElMessage.warning('未关联商机，无法查看流程')
      return
    }
    loading.value = true
    try {
      const res = await request.get(`/sales/workflow/opportunity/${opportunityId}`)
      detail.value = res.data
      visible.value = true
    } finally {
      loading.value = false
    }
  }

  async function openByOrderId(orderId) {
    loading.value = true
    try {
      const res = await request.get(`/sales/workflow/opportunity-by-order/${orderId}`)
      detail.value = res.data
      visible.value = true
    } catch (e) {
      ElMessage.warning(e?.message || '未找到关联商机')
    } finally {
      loading.value = false
    }
  }

  return { detail, visible, loading, openByOpportunityId, openByOrderId }
}
