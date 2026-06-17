import request from '@/utils/request'

export function getCheckPage(params) {
  return request({
    url: '/inventory/check/page',
    method: 'get',
    params
  })
}

export function getCheckDetail(id) {
  return request({
    url: '/inventory/check/' + id,
    method: 'get'
  })
}

export function createCheck(data) {
  return request({
    url: '/inventory/check',
    method: 'post',
    data
  })
}

export function updateCheckItems(checkId, data) {
  return request({
    url: '/inventory/check/' + checkId + '/items',
    method: 'put',
    data
  })
}

export function completeCheck(id) {
  return request({
    url: '/inventory/check/' + id + '/complete',
    method: 'post'
  })
}

export function getStockLogs(params) {
  return request({
    url: '/inventory/stock-logs',
    method: 'get',
    params
  })
}
