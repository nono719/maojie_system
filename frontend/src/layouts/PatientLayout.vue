<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKey = computed(() => [route.path.split('/')[2] || 'home'])

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <a-layout style="min-height:100vh;">
    <a-layout-sider theme="light" width="220">
      <div class="logo">BreathChain · 患者端</div>
      <a-menu mode="inline" :selected-keys="selectedKey">
        <a-menu-item key="home"    @click="router.push('/patient/home')">首页</a-menu-item>
        <a-menu-item key="tasks"   @click="router.push('/patient/tasks')">我的训练</a-menu-item>
        <a-menu-item key="history" @click="router.push('/patient/history')">训练历史</a-menu-item>
        <a-menu-item key="rewards" @click="router.push('/patient/rewards')">奖励记录</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <span>{{ userStore.realName || userStore.username }}，今天也要好好呼吸 🌿</span>
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
  text-align:center;
  padding:18px 0;
  font-weight:600;
  color:#0066ff;
  letter-spacing:1px;
}
.header {
  background:#fff;
  padding:0 24px;
  display:flex;
  justify-content:space-between;
  align-items:center;
}
</style>
