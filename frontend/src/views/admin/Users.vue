<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/index'
import dayjs from 'dayjs'

const list = ref([])
const doctors = ref([])
const editing = ref(false)
const keyword = ref('')
const roleFilter = ref('ALL')
const statusFilter = ref('ALL')
const editForm = reactive({
  id: null, username: '', realName: '', phone: '', email: '',
  role: 'USER', doctorId: null, walletAddress: '', status: 1, createTime: ''
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
    status: row.status,
    createTime: row.createTime || ''
  })
  editing.value = true
}

const submitEdit = async () => {
  if (editForm.walletAddress && !walletPattern.test(editForm.walletAddress)) {
    message.error('钱包地址格式不正确，请检查后再保存')
    return
  }
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
const walletPattern = /^0x[a-fA-F0-9]{40}$/

const getDoctorLabel = (doctorId) => {
  const doctor = doctors.value.find(d => d.userId === doctorId)
  if (!doctor) return doctorId ? `医生 #${doctorId}` : '未绑定'
  return doctor.realName || doctor.username
}

const copyText = async (text, label = '内容') => {
  if (!text) return
  try {
    await navigator.clipboard?.writeText(text)
    message.success(`${label}已复制`)
  } catch (_) {
    message.error('复制失败，请手动复制')
  }
}

const doctorDisplay = computed(() => {
  if (editForm.role !== 'USER') return '不适用'
  return getDoctorLabel(editForm.doctorId)
})

const walletState = computed(() => {
  if (!editForm.walletAddress) {
    return { status: '', help: '未配置钱包地址时，训练记录仍可上链，但不会实际发放 BREATH 奖励。', label: '未配置' }
  }
  if (walletPattern.test(editForm.walletAddress)) {
    return { status: 'success', help: '钱包地址格式正确，可用于联盟链奖励发放。', label: '格式正确' }
  }
  return { status: 'error', help: '钱包地址格式应为 42 位，以 0x 开头，后跟 40 位十六进制字符。', label: '格式异常' }
})

const profileCompleteness = computed(() => {
  const fields = [editForm.realName, editForm.phone, editForm.email]
  if (editForm.role === 'USER') fields.push(editForm.doctorId)
  fields.push(editForm.walletAddress)
  const filled = fields.filter(Boolean).length
  const percent = Math.round((filled / fields.length) * 100)
  return percent
})

const filteredSummary = computed(() => `当前显示 ${filteredList.value.length} / ${list.value.length} 位用户`)

const filteredList = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  return list.value.filter((item) => {
    const matchKeyword = !search || [
      item.username,
      item.realName,
      item.phone,
      item.email
    ].some(v => String(v || '').toLowerCase().includes(search))

    const matchRole = roleFilter.value === 'ALL' || item.role === roleFilter.value
    const matchStatus = statusFilter.value === 'ALL' || String(item.status) === statusFilter.value

    return matchKeyword && matchRole && matchStatus
  })
})

const stats = computed(() => ({
  total: list.value.length,
  admin: list.value.filter(i => i.role === 'ADMIN').length,
  doctor: list.value.filter(i => i.role === 'DOCTOR').length,
  user: list.value.filter(i => i.role === 'USER').length,
  enabled: list.value.filter(i => i.status === 1).length
}))

const resetFilters = () => {
  keyword.value = ''
  roleFilter.value = 'ALL'
  statusFilter.value = 'ALL'
}

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户信息', key: 'user', width: 220 },
  { title: '角色', dataIndex: 'role', key: 'role', width: 90 },
  { title: '手机号', dataIndex: 'phone' },
  { title: '邮箱', dataIndex: 'email' },
  { title: '钱包地址', dataIndex: 'walletAddress', key: 'wallet', width: 220 },
  { title: '绑定医生ID', dataIndex: 'doctorId', width: 110 },
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

    <div class="stats-row">
      <div class="stats-card">
        <div class="stats-label">用户总数</div>
        <div class="stats-value">{{ stats.total }}</div>
      </div>
      <div class="stats-card">
        <div class="stats-label">管理员</div>
        <div class="stats-value">{{ stats.admin }}</div>
      </div>
      <div class="stats-card">
        <div class="stats-label">医生</div>
        <div class="stats-value">{{ stats.doctor }}</div>
      </div>
      <div class="stats-card">
        <div class="stats-label">患者</div>
        <div class="stats-value">{{ stats.user }}</div>
      </div>
      <div class="stats-card">
        <div class="stats-label">启用中</div>
        <div class="stats-value">{{ stats.enabled }}</div>
      </div>
    </div>

    <div class="section-card">
      <div class="table-toolbar">
        <a-input-search
          v-model:value="keyword"
          allow-clear
          placeholder="搜索用户名、姓名、手机号、邮箱"
          class="toolbar-search"
        />
        <a-select v-model:value="roleFilter" class="toolbar-select">
          <a-select-option value="ALL">全部角色</a-select-option>
          <a-select-option value="ADMIN">管理员</a-select-option>
          <a-select-option value="DOCTOR">医生</a-select-option>
          <a-select-option value="USER">患者</a-select-option>
        </a-select>
        <a-select v-model:value="statusFilter" class="toolbar-select">
          <a-select-option value="ALL">全部状态</a-select-option>
          <a-select-option value="1">启用</a-select-option>
          <a-select-option value="0">停用</a-select-option>
        </a-select>
        <a-button @click="resetFilters">重置筛选</a-button>
        <div class="toolbar-summary">{{ filteredSummary }}</div>
      </div>

      <a-table :columns="columns" :data-source="filteredList" row-key="id" :pagination="{ pageSize: 20 }" :scroll="{ x: 1320 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'">
            <div class="table-user">
              <div class="table-user-avatar">{{ (record.realName || record.username || '?').slice(0, 1).toUpperCase() }}</div>
              <div class="table-user-meta">
                <div class="table-user-name">{{ record.realName || record.username }}</div>
                <div class="table-user-sub">@{{ record.username }}</div>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'role'">
            <a-tag :color="roleColor[record.role]">{{ roleLabel[record.role] }}</a-tag>
          </template>
          <template v-else-if="column.key === 'wallet'">
            <div v-if="record.walletAddress" class="wallet-cell">
              <code class="wallet-code">{{ record.walletAddress }}</code>
              <a-button type="link" size="small" @click="copyText(record.walletAddress, '钱包地址')">复制</a-button>
            </div>
            <span v-else class="cell-empty">未配置</span>
          </template>
          <template v-else-if="column.dataIndex === 'doctorId'">
            <span>{{ getDoctorLabel(record.doctorId) }}</span>
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

    <a-modal v-model:open="editing" title="用户详情 / 编辑" :width="720" @ok="submitEdit" ok-text="保存修改" cancel-text="取消">
      <div class="user-edit-modal">
        <div class="user-summary">
          <div class="user-avatar">{{ (editForm.realName || editForm.username || '?').slice(0, 1).toUpperCase() }}</div>
          <div class="user-meta">
            <div class="user-name">{{ editForm.realName || editForm.username }}</div>
            <div class="user-sub">@{{ editForm.username }}</div>
            <div class="user-tags">
              <a-tag :color="roleColor[editForm.role]">{{ roleLabel[editForm.role] }}</a-tag>
              <a-tag :color="editForm.status === 1 ? 'green' : 'red'">{{ editForm.status === 1 ? '启用中' : '已停用' }}</a-tag>
              <a-tag color="default">ID #{{ editForm.id }}</a-tag>
            </div>
          </div>
        </div>

        <div class="summary-grid">
          <div class="summary-card">
            <div class="summary-card-label">绑定医生</div>
            <div class="summary-card-value">{{ doctorDisplay }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-card-label">钱包状态</div>
            <div class="summary-card-value">{{ walletState.label }}</div>
          </div>
          <div class="summary-card">
            <div class="summary-card-label">创建时间</div>
            <div class="summary-card-value summary-card-value-sm">
              {{ editForm.createTime ? dayjs(editForm.createTime).format('YYYY-MM-DD HH:mm') : '-' }}
            </div>
          </div>
          <div class="summary-card">
            <div class="summary-card-label">资料完整度</div>
            <div class="summary-card-value">{{ profileCompleteness }}%</div>
          </div>
        </div>

        <div class="edit-section">
          <div class="edit-section-title">基础信息</div>
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
            </a-row>
          </a-form>
        </div>

        <div class="edit-section">
          <div class="edit-section-title">联盟链钱包配置</div>
          <a-alert
            type="info"
            show-icon
            style="margin-bottom: 16px;"
            message="奖励发放依赖患者钱包地址"
            description="患者训练完成后，BREATH 奖励将发放到这里填写的钱包地址；未配置地址时训练记录仍会正常上链，但不会实际发币。"
          />
          <a-form layout="vertical">
            <a-form-item label="区块链钱包地址">
              <div class="wallet-tips">
                <a-tag :color="walletState.status === 'success' ? 'green' : walletState.status === 'error' ? 'red' : 'default'">
                  {{ walletState.label }}
                </a-tag>
              </div>
              <a-input-group compact>
                <a-input v-model:value="editForm.walletAddress" placeholder="0x..." style="width: calc(100% - 76px);" />
                <a-button style="width: 76px;" :disabled="!editForm.walletAddress" @click="copyText(editForm.walletAddress, '钱包地址')">复制</a-button>
              </a-input-group>
              <div class="wallet-help" :class="{ error: walletState.status === 'error', success: walletState.status === 'success' }">
                {{ walletState.help }}
              </div>
            </a-form-item>
          </a-form>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.user-edit-modal {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.stats-card {
  background: linear-gradient(135deg, #f7fbff 0%, #ffffff 100%);
  border: 1px solid #eef3ff;
  border-radius: 14px;
  padding: 16px 18px;
}

.stats-label {
  color: var(--color-text-light);
  font-size: 13px;
}

.stats-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #0066ff;
  line-height: 1;
}

.table-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.toolbar-search {
  flex: 1;
  min-width: 260px;
}

.toolbar-select {
  width: 140px;
}

.toolbar-summary {
  margin-left: auto;
  color: var(--color-text-light);
  font-size: 13px;
  display: flex;
  align-items: center;
}

.table-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #10b7e0, #0066ff);
}

.table-user-meta {
  min-width: 0;
}

.table-user-name {
  font-weight: 600;
  color: var(--color-text);
}

.table-user-sub {
  color: var(--color-text-light);
  font-size: 12px;
}

.wallet-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.wallet-code {
  font-size: 12px;
  word-break: break-all;
  white-space: normal;
  color: #0066ff;
}

.cell-empty {
  color: #999;
}

.user-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
  border: 1px solid rgba(0, 102, 255, 0.08);
}

.user-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #10b7e0, #0066ff);
  box-shadow: 0 8px 20px rgba(0, 102, 255, 0.22);
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
}

.user-sub {
  color: var(--color-text-light);
  font-size: 13px;
}

.user-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  background: #fff;
  border: 1px solid #edf2ff;
  border-radius: 14px;
  padding: 16px;
}

.summary-card-label {
  color: var(--color-text-light);
  font-size: 12px;
  margin-bottom: 8px;
}

.summary-card-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.4;
  word-break: break-word;
}

.summary-card-value-sm {
  font-size: 15px;
}

.wallet-tips {
  margin-bottom: 8px;
}

.wallet-help {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-light);
  line-height: 1.6;
}

.wallet-help.error {
  color: #cf1322;
}

.wallet-help.success {
  color: #389e0d;
}

.edit-section {
  background: #fafbfc;
  border-radius: 14px;
  padding: 18px 20px 4px;
  border: 1px solid #f0f0f0;
}

.edit-section-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 14px;
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .toolbar-summary {
    width: 100%;
    margin-left: 0;
  }
}
</style>
