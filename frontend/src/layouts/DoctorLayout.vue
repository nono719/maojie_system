<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKey = computed(() => [route.path.split('/')[2] || 'dashboard'])

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <a-layout style="min-height:100vh;">
    <a-layout-sider theme="dark" width="220">
      <div class="logo">BreathChain · {{ userStore.isAdmin ? '管理端' : '医生端' }}</div>
      <a-menu mode="inline" theme="dark" :selected-keys="selectedKey">
        <a-menu-item key="dashboard" @click="router.push('/doctor/dashboard')">数据概览</a-menu-item>
        <a-menu-item key="tasks"     @click="router.push('/doctor/tasks')">任务管理</a-menu-item>
        <a-menu-item key="patients"  @click="router.push('/doctor/patients')">患者管理</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <span>欢迎，{{ userStore.realName || userStore.username }} {{ userStore.isAdmin ? '管理员' : '医生' }}</span>
        <a-button type="link" @click="logout">退出登录</a-button>
      </a-layout-header>
      <a-layout-content class="page-container">
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.logo {
  color:#fff;
  text-align:center;
  padding:18px 0;
  font-weight:600;
  letter-spacing:1px;
  background:rgba(255,255,255,.05);
}
.header {
  background:#fff;
  padding:0 24px;
  display:flex;
  justify-content:space-between;
  align-items:center;
}
</style>
