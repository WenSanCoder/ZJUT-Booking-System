// src/router/index.ts
import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板' }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: () => import('@/views/approval/index.vue'),
        meta: { title: '审批中心' }
      },
      {
        path: 'venue',
        name: 'Venue',
        component: () => import('@/views/venue/index.vue'),
        meta: { title: '场地管理' }
      },
      {
        path: 'sys/user',
        name: 'UserMgmt',
        component: () => import('@/views/sys/user/index.vue'),
        meta: { title: '账号管理' }
      },
      {
        path: 'sys/structure',
        name: 'StructureMgmt',
        component: () => import('@/views/sys/structure/index.vue'),
        meta: { title: '架构管理' }
      },
      {
        path: 'sys/announcement',
        name: 'Announcement',
        component: () => import('@/views/sys/announcement/index.vue'),
        meta: { title: '发布公告' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
