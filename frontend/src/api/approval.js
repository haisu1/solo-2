import request from '@/utils/request'

export function getApprovalFlowPage(params) {
  return request({
    url: '/api/approval/flow/page',
    method: 'get',
    params
  })
}

export function getApprovalFlowDetail(id) {
  return request({
    url: `/api/approval/flow/${id}`,
    method: 'get'
  })
}

export function createApprovalFlow(data) {
  return request({
    url: '/api/approval/flow',
    method: 'post',
    data
  })
}

export function updateApprovalFlow(id, data) {
  return request({
    url: `/api/approval/flow/${id}`,
    method: 'put',
    data
  })
}

export function deleteApprovalFlow(id) {
  return request({
    url: `/api/approval/flow/${id}`,
    method: 'delete'
  })
}

export function getMyApprovalTasks(params) {
  return request({
    url: '/api/approval/task/mytasks',
    method: 'get',
    params
  })
}

export function getApprovalHistory(params) {
  return request({
    url: '/api/approval/task/history',
    method: 'get',
    params
  })
}

export function approveTask(data) {
  return request({
    url: '/api/approval/task/approve',
    method: 'post',
    params: data
  })
}

export function withdrawApproval(data) {
  return request({
    url: '/api/approval/task/withdraw',
    method: 'post',
    params: data
  })
}

export function batchApproveTasks(data) {
  return request({
    url: '/api/approval/task/batch',
    method: 'post',
    data
  })
}
