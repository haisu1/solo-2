import request from '@/utils/request'

export function getCategoryList() {
  return request({
    url: '/supplies/categories',
    method: 'get'
  })
}

export function getCategoryPage(params) {
  return request({
    url: '/supplies/categories/page',
    method: 'get',
    params
  })
}

export function createCategory(data) {
  return request({
    url: '/supplies/categories',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: '/supplies/categories/' + id,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: '/supplies/categories/' + id,
    method: 'delete'
  })
}

export function getSupplyPage(params) {
  return request({
    url: '/supplies/page',
    method: 'get',
    params
  })
}

export function getSupplyList(params) {
  return request({
    url: '/supplies/list',
    method: 'get',
    params
  })
}

export function getSupplyDetail(id) {
  return request({
    url: '/supplies/' + id,
    method: 'get'
  })
}

export function getLowStockSupplies() {
  return request({
    url: '/supplies/low-stock',
    method: 'get'
  })
}

export function createSupply(data) {
  return request({
    url: '/supplies',
    method: 'post',
    data
  })
}

export function updateSupply(id, data) {
  return request({
    url: '/supplies/' + id,
    method: 'put',
    data
  })
}

export function deleteSupply(id) {
  return request({
    url: '/supplies/' + id,
    method: 'delete'
  })
}

export function addStock(id, data) {
  return request({
    url: '/supplies/' + id + '/stock-add',
    method: 'post',
    data
  })
}
