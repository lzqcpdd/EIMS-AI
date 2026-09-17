export const ORDER_STATUS = [
  { value: 0, label: '待入库', type: 'info' },
  { value: 1, label: '部分入库', type: 'warning' },
  { value: 2, label: '全部入库', type: 'success' },
  { value: 3, label: '已完成', type: '' },
  { value: 4, label: '已取消', type: 'danger' }
]

export const PURCHASE_STATUS = [
  { value: 0, label: '草稿', type: 'info' },
  { value: 1, label: '审批中', type: 'warning' },
  { value: 2, label: '审批通过', type: 'success' },
  { value: 3, label: '已驳回', type: 'danger' },
  { value: 4, label: '已生成订单', type: '' }
]

export const WORKFLOW_STEPS = ['填写申请', '提交审批', '分级审批', '询价比价', '采购订单']

export function orderStatusLabel(status) {
  return ORDER_STATUS.find(s => s.value === Number(status))?.label || '未知'
}

export function orderStatusType(status) {
  return ORDER_STATUS.find(s => s.value === Number(status))?.type || 'info'
}

export function statusTagType(status) {
  return PURCHASE_STATUS.find(s => s.value === Number(status))?.type || 'info'
}

export function statusLabel(status) {
  return PURCHASE_STATUS.find(s => s.value === Number(status))?.label || '未知'
}

export function workflowActiveStep(status) {
  const s = Number(status)
  if (s === 0) return 0
  if (s === 1) return 2
  if (s === 2) return 3
  if (s === 3) return 2
  if (s === 4) return 4
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
