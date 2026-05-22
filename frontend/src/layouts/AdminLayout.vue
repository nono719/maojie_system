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
      <div class="logo">
        <span class="badge">ADMIN</span>
        <div class="title">BreathChain · 管理端</div>
      </div>
      <a-menu mode="inline" theme="dark" :selected-keys="selectedKey">
        <a-menu-item key="dashboard" @click="router.push('/admin/dashboard')">数据概览</a-menu-item>
        <a-menu-item key="users"     @click="router.push('/admin/users')">用户管理</a-menu-item>
        <a-menu-item key="doctors"   @click="router.push('/admin/doctors')">医生审核</a-menu-item>
        <a-menu-item key="records"   @click="router.push('/admin/records')">训练记录审计</a-menu-item>
        <a-menu-item key="chain"     @click="router.push('/admin/chain')">区块链状态</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <span>欢迎，{{ userStore.realName || userStore.username }} 管理员</span>
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
  background:rgba(255,255,255,.05);
}
.logo .badge {
  display:inline-block;
  font-size:11px;
  background:#ff4d4f;
  color:#fff;
  padding:2px 8px;
  border-radius:10px;
  letter-spacing:1px;
  margin-bottom:6px;
}
.logo .title {
  color:#fff;
  font-weight:600;
  letter-spacing:1px;
  font-size:14px;
}
.header {
  background:#fff;
  padding:0 24px;
  display:flex;
  justify-content:space-between;
  align-items:center;
}
</style>
