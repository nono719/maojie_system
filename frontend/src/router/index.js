import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// ADMIN 复用医生端布局（论文 4.2：管理员负责用户/系统管理，与医生端管理后台同一布局）
function homeForRole(role) {
  if (role === 'DOCTOR' || role === 'ADMIN') return '/doctor'
  if (role === 'USER') return '/patient'
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
    path: '/doctor',
    component: () => import('@/layouts/DoctorLayout.vue'),
    meta: { roles: ['DOCTOR', 'ADMIN'] },
    children: [
      { path: '', redirect: '/doctor/dashboard' },
      { path: 'dashboard', component: () => import('@/views/doctor/Dashboard.vue') },
      { path: 'tasks',     component: () => import('@/views/doctor/TaskManage.vue') },
      { path: 'patients',  component: () => import('@/views/doctor/Patients.vue') }
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
