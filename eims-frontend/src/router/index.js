import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'ai/query', component: () => import('@/views/ai/Query.vue') },
      { path: 'base/supplier', component: () => import('@/views/base/Supplier.vue') },
      { path: 'base/customer', component: () => import('@/views/base/Customer.vue') },
      { path: 'base/material', component: () => import('@/views/base/Material.vue') },
      { path: 'base/warehouse', component: () => import('@/views/base/Warehouse.vue') },
      { path: 'base/codeRule', component: () => import('@/views/base/CodeRule.vue') },
      { path: 'wms/inventory', component: () => import('@/views/wms/Inventory.vue') },
      { path: 'wms/scan', component: () => import('@/views/wms/Scan.vue') },
      { path: 'wms/record', component: () => import('@/views/wms/StockRecord.vue') },
      { path: 'wms/stocktake', component: () => import('@/views/wms/Stocktake.vue') },
      { path: 'purchase/request', component: () => import('@/views/purchase/Request.vue') },
      { path: 'purchase/approval', component: () => import('@/views/purchase/Approval.vue') },
      { path: 'purchase/quote', component: () => import('@/views/purchase/Quote.vue') },
      { path: 'purchase/order', component: () => import('@/views/purchase/Order.vue') },
      { path: 'sales/lead', component: () => import('@/views/sales/Lead.vue') },
      { path: 'sales/opportunity', component: () => import('@/views/sales/Opportunity.vue') },
      { path: 'sales/quotation', component: () => import('@/views/sales/Quotation.vue') },
      { path: 'sales/contract', component: () => import('@/views/sales/Contract.vue') },
      { path: 'sales/order', component: () => import('@/views/sales/Order.vue') },
      { path: 'sales/delivery', component: () => import('@/views/sales/Delivery.vue') },
      { path: 'sales/afterService', component: () => import('@/views/sales/AfterService.vue') },
      { path: 'system/user', component: () => import('@/views/system/User.vue') },
      { path: 'system/role', component: () => import('@/views/system/Role.vue') },
      { path: 'system/dept', component: () => import('@/views/system/Dept.vue') },
      { path: 'system/log', component: () => import('@/views/system/Log.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const store = useUserStore()
  if (to.path === '/login' && store.token) return '/dashboard'
  if (!to.meta.public && !store.token) return '/login'
})

export default router
