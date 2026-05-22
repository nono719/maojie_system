import axios from 'axios'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/store/user'
import router from '@/router'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && body.code !== undefined && body.code !== 0) {
      message.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body?.data
  },
  (err) => {
    const status = err?.response?.status
    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      message.warning('登录已过期，请重新登录')
    } else if (status === 403) {
      message.error('无权访问')
    } else {
      message.error(err?.response?.data?.message || err.message || '网络异常')
    }
    return Promise.reject(err)
  }
)

export default http
