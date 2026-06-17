import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/system/users',
    method: 'get',
    params
  })
}

export function getUserDetail(id) {
  return request({
    url: '/system/users/' + id,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/system/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: '/system/users/' + id,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: '/system/users/' + id,
    method: 'delete'
  })
}

export function getRoleList() {
  return request({
    url: '/system/roles',
    method: 'get'
  })
}

export function getDepartmentList() {
  return request({
    url: '/system/departments',
    method: 'get'
  })
}
