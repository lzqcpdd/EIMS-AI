export function formatMoney(value, fractionDigits = 0) {
  return Number(value || 0).toLocaleString(undefined, {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits
  })
}
