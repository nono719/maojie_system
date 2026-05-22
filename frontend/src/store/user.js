import { defineStore } from 'pinia'

const STORAGE_KEY = 'breathchain.user'

export const useUserStore = defineStore('user', {
  state: () => {
    const raw = localStorage.getItem(STORAGE_KEY)
    const cached = raw ? JSON.parse(raw) : {}
    return {
      token: cached.token || '',
      userId: cached.userId || null,
      username: cached.username || '',
      realName: cached.realName || '',
      role: cached.role || '',
      walletAddress: cached.walletAddress || ''
    }
  },
  getters: {
    isLogin: (state) => !!state.token,
    isDoctor: (state) => state.role === 'DOCTOR',
    isPatient: (state) => state.role === 'USER',
    isAdmin: (state) => state.role === 'ADMIN'
  },
  actions: {
    setLogin(payload) {
      this.token = payload.token
      this.userId = payload.userId
      this.username = payload.username
      this.realName = payload.realName
      this.role = payload.role
      this.walletAddress = payload.walletAddress
      localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.role = ''
      this.walletAddress = ''
      localStorage.removeItem(STORAGE_KEY)
    }
  }
})
