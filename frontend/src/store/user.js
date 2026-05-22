import { defineStore } from 'pinia'

// 用 sessionStorage 实现 per-tab 隔离 — 不同标签页可以同时登录不同角色
const STORAGE_KEY = 'breathchain.user'

const readCached = () => {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch (_) { return {} }
}

export const useUserStore = defineStore('user', {
  state: () => {
    const cached = readCached()
    return {
      token: cached.token || '',
      userId: cached.userId || null,
      username: cached.username || '',
      realName: cached.realName || '',
      role: cached.role || '',
      walletAddress: cached.walletAddress || '',
      certified: cached.certified // 仅医生有意义；true/false/undefined
    }
  },
  getters: {
    isLogin: (state) => !!state.token,
    isDoctor: (state) => state.role === 'DOCTOR',
    isPatient: (state) => state.role === 'USER',
    isAdmin: (state) => state.role === 'ADMIN',
    /** 医生是否通过认证（非医生角色返回 true） */
    isCertified: (state) => state.role !== 'DOCTOR' || state.certified === true
  },
  actions: {
    setLogin(payload) {
      this.token = payload.token
      this.userId = payload.userId
      this.username = payload.username
      this.realName = payload.realName
      this.role = payload.role
      this.walletAddress = payload.walletAddress
      this.certified = payload.certified
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.role = ''
      this.walletAddress = ''
      this.certified = undefined
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }
})
