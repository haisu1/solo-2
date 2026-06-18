import request from '@/utils/request'

export function getSupplyWarnings(params) {
  return request({
    url: '/warnings/supplies',
    method: 'get',
    params
  })
}

export function getWarningSupplies() {
  return request({
    url: '/warnings/supplies/warning-only',
    method: 'get'
  })
}

export function getSupplyWarning(supplyId) {
  return request({
    url: '/warnings/supplies/' + supplyId,
    method: 'get'
  })
}

export function getWarningStatistics() {
  return request({
    url: '/warnings/statistics',
    method: 'get'
  })
}

export function getConsumptionTrend(supplyId, period) {
  return request({
    url: '/warnings/supplies/' + supplyId + '/trend',
    method: 'get',
    params: { period }
  })
}

export function recalculateWarnings() {
  return request({
    url: '/warnings/recalculate',
    method: 'post'
  })
}

export function getWarningMessages(params) {
  return request({
    url: '/warnings/messages',
    method: 'get',
    params
  })
}

export function getUnreadMessages() {
  return request({
    url: '/warnings/messages/unread',
    method: 'get'
  })
}

export function getUnreadStatistics() {
  return request({
    url: '/warnings/messages/unread-statistics',
    method: 'get'
  })
}

export function markMessageAsRead(id) {
  return request({
    url: '/warnings/messages/' + id + '/read',
    method: 'post'
  })
}

export function markAllMessagesAsRead() {
  return request({
    url: '/warnings/messages/read-all',
    method: 'post'
  })
}

export function getPurchaseSuggestions() {
  return request({
    url: '/warnings/purchase-suggestions',
    method: 'get'
  })
}

export function getPurchaseSuggestionStatistics() {
  return request({
    url: '/warnings/purchase-suggestions/statistics',
    method: 'get'
  })
}

export function getSupplyPurchaseSuggestion(supplyId) {
  return request({
    url: '/warnings/purchase-suggestions/' + supplyId,
    method: 'get'
  })
}

export function generatePurchaseFromSuggestions(data, remark) {
  return request({
    url: '/warnings/purchase-suggestions/generate-purchase',
    method: 'post',
    data,
    params: { remark }
  })
}
