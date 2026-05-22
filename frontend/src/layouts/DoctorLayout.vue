<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKey = computed(() => [route.path.split('/')[2] || 'dashboard'])
const pendingReview = computed(() => userStore.role === 'DOCTOR' && userStore.certified === false)
</script>

<template>
  <a-layout style="min-height:100vh;">
    <a-layout-sider theme="dark" :width="200" breakpoint="lg" collapsible>
      <div class="logo">
        <div class="logo-circle">D</div>
        <div class="logo-text">
          <div class="title">BreathChain</div>
          <div class="subtitle">医生端</div>
        </div>
      </div>
      <a-menu mode="inline" theme="dark" :selected-keys="selectedKey">
        <a-menu-item key="dashboard" @click="router.push('/doctor/dashboard')">📊 数据概览</a-menu-item>
        <a-menu-item key="tasks"     @click="router.push('/doctor/tasks')" :disabled="pendingReview">📝 任务管理</a-menu-item>
        <a-menu-item key="patients"  @click="router.push('/doctor/patients')" :disabled="pendingReview">👥 患者管理</a-menu-item>
        <a-menu-item key="requests"  @click="router.push('/doctor/requests')" :disabled="pendingReview">🤝 患者分配申请</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <AppHeader greeting="临床康复管理 · 数据可信存证" />
      <a-layout-content>
        <a-alert v-if="pendingReview"
          type="warning" show-icon
          message="您的医生资质正在等待管理员审核"
          description="审核通过前，您可以浏览界面但无法创建任务、分配患者等操作。"
          banner
          style="margin: 16px 24px 0;"
        />
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.logo {
  display: flex; align-items: center; gap: 10px;
  padding: 18px 16px;
  background: rgba(255,255,255,.05);
}
.logo-circle {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #10b7e0, #0066ff);
  color: #fff; font-weight: 700; font-size: 16px;
  display: inline-flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.4);
}
.logo-text { line-height: 1.2; }
.title { color: #fff; font-weight: 600; font-size: 15px; }
.subtitle { color: rgba(255,255,255,.6); font-size: 11px; margin-top: 2px; }
</style>
