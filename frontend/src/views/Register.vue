<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { register } from '@/api/auth'
import { useUserStore } from '@/store/user'
import http from '@/api/index'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '', password: '', realName: '', phone: '',
  role: 'USER',
  licenseNo: '', hospital: '', department: '', title: '',
  doctorId: null
})
const loading = ref(false)
const isDoctor = computed(() => form.role === 'DOCTOR')

const doctors = ref([])
onMounted(async () => {
  try { doctors.value = await http.get('/public/doctors') } catch (_) {}
})

// 一键填充测试数据
const fillTest = () => {
  const ts = Date.now().toString().slice(-6)
  if (isDoctor.value) {
    Object.assign(form, {
      username: 'doctor' + ts,
      password: 'admin123',
      realName: '测试医生' + ts.slice(-3),
      phone: '138' + ts.padStart(8, '0').slice(-8),
      licenseNo: 'TEST-' + ts,
      hospital: '成都信息工程大学附属医院',
      department: '呼吸科',
      title: '主治医师'
    })
  } else {
    Object.assign(form, {
      username: 'user' + ts,
      password: 'admin123',
      realName: '测试患者' + ts.slice(-3),
      phone: '139' + ts.padStart(8, '0').slice(-8)
    })
  }
  message.success('已填充测试数据，可直接提交')
}

const onSubmit = async () => {
  loading.value = true
  try {
    const data = await register(form)
    if (form.role === 'DOCTOR') {
      // 医生注册成功 → 等待审核
      Modal.success({
        title: '注册成功，等待审核',
        content: `已提交医生认证申请（用户名：${data.username}）。
请联系系统管理员审核通过后，方可使用本账户登录。`,
        okText: '返回登录',
        onOk: () => router.push('/login')
      })
    } else {
      userStore.setLogin(data)
      message.success('注册成功')
      router.push('/patient')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="header-row">
        <h2 style="margin: 0;">注册账户</h2>
        <a-button type="dashed" size="small" @click="fillTest">⚡ 填充测试数据</a-button>
      </div>

      <a-form :model="form" layout="vertical" @finish="onSubmit">
        <a-form-item label="角色">
          <a-radio-group v-model:value="form.role" button-style="solid">
            <a-radio-button value="USER">👤 患者</a-radio-button>
            <a-radio-button value="DOCTOR">🩺 医生</a-radio-button>
          </a-radio-group>
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="用户名" name="username">
              <a-input v-model:value="form.username" placeholder="3-50 字符" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="密码" name="password">
              <a-input-password v-model:value="form.password" placeholder="6-50 字符" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="真实姓名"><a-input v-model:value="form.realName" /></a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="手机号"><a-input v-model:value="form.phone" /></a-form-item>
          </a-col>
        </a-row>

        <template v-if="isDoctor">
          <a-divider>医生资质（填写后等待管理员审核）</a-divider>
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
          <a-alert type="info" show-icon style="margin-bottom: 12px;"
            message="医生账户须通过管理员审核后方可登录使用，请耐心等待。" />
        </template>

        <template v-else>
          <a-form-item label="主治医生（可选）">
            <a-select v-model:value="form.doctorId" placeholder="选择您的主治医生" allow-clear show-search
                      :filter-option="(input, opt) => opt.label?.includes(input)">
              <a-select-option v-for="d in doctors" :key="d.userId" :value="d.userId"
                :label="(d.realName||d.username)+(d.hospital ? ' · '+d.hospital : '')">
                <span>{{ d.realName || d.username }}</span>
                <span v-if="d.title" style="margin-left:8px;color:#999;font-size:12px;">{{ d.title }}</span>
                <span v-if="d.hospital" style="margin-left:8px;color:#999;font-size:12px;">{{ d.hospital }} · {{ d.department }}</span>
              </a-select-option>
            </a-select>
          </a-form-item>
        </template>

        <a-button type="primary" html-type="submit" block :loading="loading" @click="onSubmit">
          {{ isDoctor ? '提交审核申请' : '注 册' }}
        </a-button>
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
  width: 680px;
  background: #fff;
  border-radius: 16px;
  padding: 36px 40px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.18);
}
.header-row {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
}
</style>
