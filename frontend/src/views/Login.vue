<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)

const onSubmit = async () => {
  loading.value = true
  try {
    const data = await login(form)
    userStore.setLogin(data)
    message.success('登录成功')
    const target = data.role === 'ADMIN' ? '/admin' : data.role === 'DOCTOR' ? '/doctor' : '/patient'
    router.push(target)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <div class="logo" />
        <h1>BreathChain</h1>
        <p>基于 FISCO BCOS 联盟链的呼吸训练干预系统</p>
      </div>
      <a-form :model="form" layout="vertical" @finish="onSubmit">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" placeholder="请输入用户名" allow-clear @pressEnter="onSubmit" />
        </a-form-item>
        <a-form-item label="密码" name="password">
          <a-input-password v-model:value="form.password" placeholder="请输入密码" @pressEnter="onSubmit" />
        </a-form-item>
        <a-button type="primary" html-type="submit" block :loading="loading" @click="onSubmit">登 录</a-button>
        <div class="footer">
          还没有账户？<router-link to="/register">立即注册</router-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #10b7e0 0%, #0066ff 100%);
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
}
.brand { text-align: center; margin-bottom: 24px; }
.brand h1 { font-size: 28px; margin-top: 16px; color: #0066ff; }
.brand p  { color: #888; margin-top: 6px; font-size: 13px; }
.logo {
  width: 64px;
  height: 64px;
  margin: 0 auto;
  border-radius: 50%;
  background: radial-gradient(circle, #10b7e0 0%, #0066ff 100%);
  box-shadow: 0 6px 20px rgba(16, 183, 224, 0.4);
}
.footer { margin-top: 14px; text-align: center; font-size: 13px; color: #666; }
.footer a { color: #0066ff; }
</style>
