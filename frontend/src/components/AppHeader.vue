<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const props = defineProps({
  greeting: { type: String, default: '' }
})

const router = useRouter()
const userStore = useUserStore()

const initial = computed(() => (userStore.realName || userStore.username || '?').slice(0, 1).toUpperCase())
const roleLabel = computed(() => ({
  ADMIN: '管理员', DOCTOR: '医生', USER: '患者'
}[userStore.role] || ''))
const roleColor = computed(() => ({
  ADMIN: '#ff4d4f', DOCTOR: '#0066ff', USER: '#52c41a'
}[userStore.role] || '#888'))

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <a-layout-header class="app-header">
    <div class="left">
      <slot name="left">
        <span class="greeting">{{ greeting }}</span>
      </slot>
    </div>
    <div class="right">
      <div class="user-meta">
        <div class="avatar-circle">{{ initial }}</div>
        <div class="user-text">
          <div class="user-name">{{ userStore.realName || userStore.username }}</div>
          <a-tag :color="roleColor" style="margin: 0; font-size: 11px; line-height: 1.4;">{{ roleLabel }}</a-tag>
        </div>
      </div>
      <a-button type="text" danger @click="logout">退出登录</a-button>
    </div>
  </a-layout-header>
</template>

<style scoped>
.app-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  border-bottom: 1px solid rgba(0, 102, 255, 0.06);
  box-shadow: 0 2px 6px rgba(0,0,0,0.03);
}
.left { display: flex; align-items: center; gap: 12px; }
.greeting { color: var(--color-text-light); font-size: 14px; }
.right { display: flex; align-items: center; gap: 16px; }
.user-meta { display: flex; align-items: center; gap: 10px; }
.user-text { display: flex; flex-direction: column; gap: 2px; line-height: 1; }
.user-name { font-size: 13px; font-weight: 600; color: var(--color-text); }
</style>
