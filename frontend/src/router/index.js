import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/', redirect: () => {
      const u = useUserStore()
      if (!u.isLogin) return '/login'
      return u.isDoctor ? '/doctor' : '/patient'
    }
  },
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/Register.vue'), meta: { public: true } },

  {
    path: '/doctor',
    component: () => import('@/layouts/DoctorLayout.vue'),
    meta: { role: 'DOCTOR' },
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
    meta: { role: 'USER' },
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
  if (to.meta.role && to.meta.role !== userStore.role) {
    return userStore.isDoctor ? '/doctor' : '/patient'
  }
  return true
})

export default router
