<script setup>
import { RouterView, useRouter, useRoute } from 'vue-router'
import { computed } from 'vue'
import AppHeader from '@/components/AppHeader.vue'

const router = useRouter()
const route = useRoute()

const selectedKey = computed(() => [route.path.split('/')[2] || 'dashboard'])
</script>

<template>
  <a-layout style="min-height:100vh;">
    <a-layout-sider theme="dark" :width="200" breakpoint="lg" collapsible>
      <div class="logo">
        <div class="logo-circle">A</div>
        <div class="logo-text">
          <div class="title">BreathChain</div>
          <div class="subtitle">管理端</div>
        </div>
      </div>
      <a-menu mode="inline" theme="dark" :selected-keys="selectedKey">
        <a-menu-item key="dashboard" @click="router.push('/admin/dashboard')">📊 数据概览</a-menu-item>
        <a-menu-item key="users"     @click="router.push('/admin/users')">👥 用户管理</a-menu-item>
        <a-menu-item key="doctors"   @click="router.push('/admin/doctors')">🩺 医生审核</a-menu-item>
        <a-menu-item key="requests"  @click="router.push('/admin/requests')">🤝 患者分配</a-menu-item>
        <a-menu-item key="records"   @click="router.push('/admin/records')">📋 记录审计</a-menu-item>
        <a-menu-item key="chain"     @click="router.push('/admin/chain')">⛓ 区块链状态</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <AppHeader greeting="管理后台 · 全局数据视图" />
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
  background: rgba(255,255,255,.05);
}
.logo-circle {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff; font-weight: 700; font-size: 16px;
  display: inline-flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
}
.logo-text { line-height: 1.2; }
.title { color: #fff; font-weight: 600; font-size: 15px; }
.subtitle { color: rgba(255,255,255,.6); font-size: 11px; margin-top: 2px; }
</style>
