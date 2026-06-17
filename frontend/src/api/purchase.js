import request from '@/utils/request'

export function getPurchasePage(params) {
  return request({
    url: '/purchases/page',
    method: 'get',
    params
  })
}

export function getPurchaseDetail(id) {
  return request({
    url: '/purchases/' + id,
    method: 'get'
  })
}

export function createPurchase(data) {
  return request({
    url: '/purchases',
    method: 'post',
    data
  })
}

export function approvePurchase(id, data) {
  return request({
    url: '/purchases/' + id + '/approve',
    method: 'post',
    data
  })
}

export function stockIn(id) {
  return request({
    url: '/purchases/' + id + '/stock-in',
    method: 'post'
  })
}
