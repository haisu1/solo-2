import request from '@/utils/request'

export function getDashboardData() {
  return request({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

export function getRequisitionStatistics(params) {
  return request({
    url: '/statistics/requisitions',
    method: 'get',
    params
  })
}
