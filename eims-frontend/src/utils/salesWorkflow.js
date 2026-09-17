export const WORKFLOW_STEPS = ['线索跟进', '商机立项', '销售报价', '合同签章', '销售订单', '发货出库', '售后服务']

export const LEAD_STATUS = [
  { value: 0, label: '待跟进', type: 'info' },
  { value: 1, label: '跟进中', type: 'warning' },
  { value: 2, label: '已转商机', type: 'success' },
  { value: 3, label: '无效线索', type: 'danger' }
]

export const OPP_STAGE = [
  { value: 1, label: '初步接触', type: 'info' },
  { value: 2, label: '报价阶段', type: 'warning' },
  { value: 3, label: '合同阶段', type: '' },
  { value: 4, label: '已成交', type: 'success' },
  { value: 5, label: '已失败', type: 'danger' }
]

export const QUOTE_STATUS = [
  { value: 0, label: '草稿', type: 'info' },
  { value: 1, label: '已提交', type: 'warning' },
  { value: 2, label: '已确认', type: 'success' },
  { value: 3, label: '已失效', type: 'danger' }
]

export const CONTRACT_SIGN = [
  { value: 0, label: '待签章', type: 'warning' },
  { value: 1, label: '已签章', type: 'success' },
  { value: 2, label: '已作废', type: 'danger' }
]

export const ORDER_STATUS = [
  { value: 0, label: '待确认', type: 'info' },
  { value: 1, label: '待发货', type: 'warning' },
  { value: 2, label: '部分发货', type: '' },
  { value: 3, label: '已完成', type: 'success' },
  { value: 4, label: '已取消', type: 'danger' }
]

export const PAYMENT_STATUS = [
  { value: 0, label: '未回款', type: 'danger' },
  { value: 1, label: '部分回款', type: 'warning' },
  { value: 2, label: '已回款', type: 'success' }
]

export const DELIVERY_STATUS = [
  { value: 0, label: '待出库', type: 'info' },
  { value: 1, label: '已出库', type: 'warning' },
  { value: 2, label: '已签收', type: 'success' }
]

export const SERVICE_TYPE = [
  { value: 1, label: '客户回访', type: 'info' },
  { value: 2, label: '退货处理', type: 'warning' },
  { value: 3, label: '投诉处理', type: 'danger' }
]

export const SERVICE_STATUS = [
  { value: 0, label: '处理中', type: 'warning' },
  { value: 1, label: '已完成', type: 'success' }
]

export function findMeta(list, value) {
  return list.find(s => s.value === Number(value)) || { label: '未知', type: 'info' }
}

/** 根据商机详情计算当前流程步骤（0-6） */
export function workflowActiveStep(detail) {
  if (!detail) return 0
  if (detail.currentStep != null) return Number(detail.currentStep)
  const s = Number(detail.stage)
  if (s <= 1) return 1
  if (s === 2) return 2
  if (s === 3) return 3
  if (s >= 4) return 4
  return 0
}

export function lineAmount(row) {
  const qty = Number(row.quantity) || 0
  const price = Number(row.unitPrice) || 0
  return (qty * price).toFixed(2)
}

export function calcTotalAmount(items) {
  return items.reduce((sum, row) => sum + Number(lineAmount(row)), 0).toFixed(2)
}

export function pendingDeliver(row) {
  const ordered = Number(row.quantity) || 0
  const delivered = Number(row.deliveredQuantity) || 0
  return Math.max(ordered - delivered, 0)
}
