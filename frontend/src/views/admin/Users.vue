<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/index'
import dayjs from 'dayjs'

const list = ref([])
const doctors = ref([])
const editing = ref(false)
const editForm = reactive({
  id: null, username: '', realName: '', phone: '', email: '',
  role: 'USER', doctorId: null, walletAddress: '', status: 1
})

const load = async () => {
  list.value = await http.get('/admin/users')
  doctors.value = await http.get('/public/doctors')
}

const toggleStatus = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await http.put(`/admin/users/${row.id}/status?status=${next}`)
  message.success(next === 1 ? '已启用' : '已停用')
  await load()
}

const openDetail = (row) => {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    realName: row.realName || '',
    phone: row.phone || '',
    email: row.email || '',
    role: row.role,
    doctorId: row.doctorId,
    walletAddress: row.walletAddress || '',
    status: row.status
  })
  editing.value = true
}

const submitEdit = async () => {
  try {
    await http.put(`/admin/users/${editForm.id}`, {
      realName: editForm.realName,
      phone: editForm.phone,
      email: editForm.email,
      role: editForm.role,
      doctorId: editForm.role === 'USER' ? editForm.doctorId : null,
      walletAddress: editForm.walletAddress,
      status: editForm.status
    })
    message.success('修改成功')
    editing.value = false
    await load()
  } catch (_) {}
}

const roleColor = { ADMIN: 'red', DOCTOR: 'blue', USER: 'green' }
const roleLabel = { ADMIN: '管理员', DOCTOR: '医生', USER: '患者' }

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '真实姓名', dataIndex: 'realName' },
  { title: '角色', dataIndex: 'role', key: 'role', width: 90 },
  { title: '手机号', dataIndex: 'phone' },
  { title: '绑定医生ID', dataIndex: 'doctorId' },
  { title: '创建时间', dataIndex: 'createTime',
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm') : '-' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' }
]

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-title">用户管理</div>

    <div class="section-card">
      <a-table :columns="columns" :data-source="list" row-key="id" :pagination="{ pageSize: 20 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'role'">
            <a-tag :color="roleColor[record.role]">{{ roleLabel[record.role] }}</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openDetail(record)">详情/编辑</a-button>
              <a-button v-if="record.role !== 'ADMIN'" type="link" size="small"
                        :danger="record.status === 1" @click="toggleStatus(record)">
                {{ record.status === 1 ? '停用' : '启用' }}
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal v-model:open="editing" title="用户详情 / 编辑" :width="600" @ok="submitEdit" ok-text="保存" cancel-text="取消">
      <a-descriptions :column="2" size="small" bordered style="margin-bottom: 16px;">
        <a-descriptions-item label="用户ID">{{ editForm.id }}</a-descriptions-item>
        <a-descriptions-item label="用户名">{{ editForm.username }}</a-descriptions-item>
      </a-descriptions>

      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="真实姓名">
              <a-input v-model:value="editForm.realName" placeholder="请输入真实姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="手机号">
              <a-input v-model:value="editForm.phone" placeholder="13800138000" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="邮箱">
              <a-input v-model:value="editForm.email" placeholder="user@example.com" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="角色">
              <a-select v-model:value="editForm.role" :disabled="editForm.role === 'ADMIN'">
                <a-select-option value="USER">患者 (USER)</a-select-option>
                <a-select-option value="DOCTOR">医生 (DOCTOR)</a-select-option>
                <a-select-option v-if="editForm.role === 'ADMIN'" value="ADMIN">管理员 (ADMIN)</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12" v-if="editForm.role === 'USER'">
            <a-form-item label="绑定医生">
              <a-select v-model:value="editForm.doctorId" placeholder="选择主治医生" allow-clear show-search
                        :filter-option="(input, opt) => opt.label?.includes(input)">
                <a-select-option v-for="d in doctors" :key="d.userId" :value="d.userId"
                  :label="(d.realName||d.username) + ' · ' + (d.hospital||'')">
                  {{ d.realName || d.username }}
                  <span v-if="d.hospital" style="color:#999;font-size:12px;">· {{ d.hospital }} {{ d.department }}</span>
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-radio-group v-model:value="editForm.status" :disabled="editForm.role === 'ADMIN'">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">停用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="区块链钱包地址">
              <a-input v-model:value="editForm.walletAddress" placeholder="0x..." />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>
