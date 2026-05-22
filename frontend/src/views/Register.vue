<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '', password: '', realName: '', phone: '',
  role: 'USER',
  licenseNo: '', hospital: '', department: '', title: ''
})
const loading = ref(false)
const isDoctor = computed(() => form.role === 'DOCTOR')

const onSubmit = async () => {
  loading.value = true
  try {
    const data = await register(form)
    userStore.setLogin(data)
    message.success('注册成功')
    router.push(form.role === 'DOCTOR' ? '/doctor' : '/patient')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <h2 style="text-align:center;margin-bottom:24px;">注册账户</h2>
      <a-form :model="form" layout="vertical" @finish="onSubmit">
        <a-form-item label="角色">
          <a-radio-group v-model:value="form.role" button-style="solid">
            <a-radio-button value="USER">患者</a-radio-button>
            <a-radio-button value="DOCTOR">医生</a-radio-button>
          </a-radio-group>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="用户名"><a-input v-model:value="form.username" /></a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="密码"><a-input-password v-model:value="form.password" /></a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="真实姓名"><a-input v-model:value="form.realName" /></a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="手机号"><a-input v-model:value="form.phone" /></a-form-item>
          </a-col>
        </a-row>

        <template v-if="isDoctor">
          <a-divider>医生资质</a-divider>
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="执业医师证书号"><a-input v-model:value="form.licenseNo" /></a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="所属医院"><a-input v-model:value="form.hospital" /></a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="科室"><a-input v-model:value="form.department" /></a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="职称"><a-input v-model:value="form.title" /></a-form-item>
            </a-col>
          </a-row>
        </template>

        <a-button type="primary" html-type="submit" block :loading="loading" @click="onSubmit">注 册</a-button>
        <div style="margin-top:14px;text-align:center;">
          已有账户？<router-link to="/login">直接登录</router-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100%;
  padding: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #10b7e0 0%, #0066ff 100%);
}
.register-card {
  width: 640px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 40px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
}
</style>
