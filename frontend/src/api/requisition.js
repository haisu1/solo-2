import request from '@/utils/request'

export function getRequisitionPage(params) {
  return request({
    url: '/requisitions/page',
    method: 'get',
    params
  })
}

export function getRequisitionDetail(id) {
  return request({
    url: '/requisitions/' + id,
    method: 'get'
  })
}

export function createRequisition(data) {
  return request({
    url: '/requisitions',
    method: 'post',
    data
  })
}

export function approveRequisition(id, data) {
  return request({
    url: '/requisitions/' + id + '/approve',
    method: 'post',
    data
  })
}

export function cancelRequisition(id) {
  return request({
    url: '/requisitions/' + id + '/cancel',
    method: 'post'
  })
}
