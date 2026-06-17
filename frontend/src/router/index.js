import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'
import { Message } from 'element-ui'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'supplies',
        name: 'SupplyList',
        component: () => import('@/views/supply/SupplyList.vue'),
        meta: { title: '物资管理', roles: ['ADMIN', 'ADMIN_STAFF'] }
      },
      {
        path: 'categories',
        name: 'CategoryList',
        component: () => import('@/views/supply/CategoryList.vue'),
        meta: { title: '物资分类', roles: ['ADMIN'] }
      },
      {
        path: 'supplies-catalog',
        name: 'SupplyCatalog',
        component: () => import('@/views/supply/SupplyCatalog.vue'),
        meta: { title: '物资目录', roles: ['ADMIN', 'ADMIN_STAFF', 'EMPLOYEE'] }
      },
      {
        path: 'requisitions',
        name: 'RequisitionList',
        component: () => import('@/views/requisition/RequisitionList.vue'),
        meta: { title: '申领管理', roles: ['ADMIN', 'ADMIN_STAFF', 'EMPLOYEE'] }
      },
      {
        path: 'requisitions/create',
        name: 'RequisitionCreate',
        component: () => import('@/views/requisition/RequisitionCreate.vue'),
        meta: { title: '提交申领', roles: ['ADMIN', 'ADMIN_STAFF', 'EMPLOYEE'] }
      },
      {
        path: 'purchases',
        name: 'PurchaseList',
        component: () => import('@/views/purchase/PurchaseList.vue'),
        meta: { title: '采购管理', roles: ['ADMIN'] }
      },
      {
        path: 'purchases/create',
        name: 'PurchaseCreate',
        component: () => import('@/views/purchase/PurchaseCreate.vue'),
        meta: { title: '创建采购单', roles: ['ADMIN'] }
      },
      {
        path: 'inventory/check',
        name: 'InventoryCheckList',
        component: () => import('@/views/inventory/InventoryCheckList.vue'),
        meta: { title: '库存盘点', roles: ['ADMIN', 'ADMIN_STAFF'] }
      },
      {
        path: 'inventory/check/:id',
        name: 'InventoryCheckDetail',
        component: () => import('@/views/inventory/InventoryCheckDetail.vue'),
        meta: { title: '盘点详情', roles: ['ADMIN', 'ADMIN_STAFF'] }
      },
      {
        path: 'inventory/logs',
        name: 'StockLogList',
        component: () => import('@/views/inventory/StockLogList.vue'),
        meta: { title: '库存流水', roles: ['ADMIN', 'ADMIN_STAFF'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '数据统计', roles: ['ADMIN', 'ADMIN_STAFF'] }
      },
      {
        path: 'system/users',
        name: 'UserList',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'approval/flow',
        name: 'ApprovalFlowList',
        component: () => import('@/views/approval/ApprovalFlowList.vue'),
        meta: { title: '审批流程配置', roles: ['ADMIN'] }
      },
      {
        path: 'approval/tasks',
        name: 'ApprovalTaskCenter',
        component: () => import('@/views/approval/ApprovalTaskCenter.vue'),
        meta: { title: '我的待办审批', roles: ['ADMIN', 'ADMIN_STAFF', 'DEPT_MANAGER', 'EMPLOYEE'] }
      }
    ]
  },
  {
    path: '*',
    redirect: '/dashboard'
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? to.meta.title + ' - 办公用品申领系统' : '办公用品申领系统'
  const token = store.state.token
  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      if (to.meta.roles && to.meta.roles.length > 0) {
        if (!store.state.roleCode) {
          store.dispatch('getUserInfo').then(() => {
            if (to.meta.roles.includes(store.state.roleCode)) {
              next()
            } else {
              Message.error('无权限访问该页面')
              next('/dashboard')
            }
          }).catch(() => {
            next('/login')
          })
        } else if (to.meta.roles.includes(store.state.roleCode)) {
          next()
        } else {
          Message.error('无权限访问该页面')
          next('/dashboard')
        }
      } else {
        next()
      }
    }
  }
})

export default router
