<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKey = computed(() => [route.path.split('/')[2] || 'home'])
const greeting = computed(() => `${userStore.realName || userStore.username}，今天也要好好呼吸 🌿`)
</script>

<template>
  <a-layout style="min-height:100vh;">
    <a-layout-sider theme="light" :width="200" breakpoint="lg" collapsible>
      <div class="logo">
        <div class="logo-circle">P</div>
        <div class="logo-text">
          <div class="title">BreathChain</div>
          <div class="subtitle">患者端</div>
        </div>
      </div>
      <a-menu mode="inline" :selected-keys="selectedKey">
        <a-menu-item key="home"    @click="router.push('/patient/home')">🏠 首页</a-menu-item>
        <a-menu-item key="tasks"   @click="router.push('/patient/tasks')">🌬️ 我的训练</a-menu-item>
        <a-menu-item key="history" @click="router.push('/patient/history')">📜 训练历史</a-menu-item>
        <a-menu-item key="rewards" @click="router.push('/patient/rewards')">🪙 奖励记录</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <AppHeader :greeting="greeting" />
      <a-layout-content>
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.logo {
  display: flex; align-items: center; gap: 10px;
  padding: 18px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.logo-circle {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #10b7e0, #0066ff);
  color: #fff; font-weight: 700; font-size: 16px;
  display: inline-flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 102, 255, 0.4);
}
.logo-text { line-height: 1.2; }
.title { color: var(--brand-primary); font-weight: 600; font-size: 15px; }
.subtitle { color: #888; font-size: 11px; margin-top: 2px; }
</style>
