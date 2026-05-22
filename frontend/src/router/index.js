import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 角色对应首页（论文 4.2 三类角色）
function homeForRole(role) {
  if (role === 'ADMIN')  return '/admin'
  if (role === 'DOCTOR') return '/doctor'
  if (role === 'USER')   return '/patient'
  return '/login'
}

const routes = [
  { path: '/', redirect: () => {
      const u = useUserStore()
      if (!u.isLogin) return '/login'
      return homeForRole(u.role)
    }
  },
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/Register.vue'), meta: { public: true } },

  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { roles: ['ADMIN'] },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'users',     component: () => import('@/views/admin/Users.vue') },
      { path: 'doctors',   component: () => import('@/views/admin/Doctors.vue') },
      { path: 'requests',  component: () => import('@/views/admin/Requests.vue') },
      { path: 'records',   component: () => import('@/views/admin/Records.vue') },
      { path: 'chain',     component: () => import('@/views/admin/Chain.vue') }
    ]
  },

  {
    path: '/doctor',
    component: () => import('@/layouts/DoctorLayout.vue'),
    meta: { roles: ['DOCTOR'] },
    children: [
      { path: '', redirect: '/doctor/dashboard' },
      { path: 'dashboard', component: () => import('@/views/doctor/Dashboard.vue') },
      { path: 'tasks',     component: () => import('@/views/doctor/TaskManage.vue') },
      { path: 'patients',  component: () => import('@/views/doctor/Patients.vue') },
      { path: 'requests',  component: () => import('@/views/doctor/Requests.vue') }
    ]
  },

  {
    path: '/patient',
    component: () => import('@/layouts/PatientLayout.vue'),
    meta: { roles: ['USER'] },
    children: [
      { path: '', redirect: '/patient/home' },
      { path: 'home',     component: () => import('@/views/patient/Home.vue') },
      { path: 'tasks',    component: () => import('@/views/patient/Tasks.vue') },
      { path: 'training/:taskId', component: () => import('@/views/patient/Training.vue') },
      { path: 'rewards',  component: () => import('@/views/patient/Rewards.vue') },
      { path: 'history',  component: () => import('@/views/patient/History.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.public) return true
  if (!userStore.isLogin) return '/login'
  // 找到目标路由层级里最严格的 roles 约束（嵌套路由从父到子合并）
  const required = to.matched.flatMap(r => r.meta?.roles || []).filter(Boolean)
  if (required.length && !required.includes(userStore.role)) {
    const home = homeForRole(userStore.role)
    return to.path === home ? false : home  // 防止跳到自己导致死循环
  }
  return true
})

export default router
