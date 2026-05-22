<script setup>
import { onMounted, ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/index'
import dayjs from 'dayjs'

const list = ref([])
const visible = ref(false)
const form = reactive({ username: '', reason: '' })

const load = async () => { list.value = await http.get('/doctor/assignment-requests') }

const submit = async () => {
  if (!form.username) { message.warning('请输入患者用户名'); return }
  try {
    await http.post('/doctor/assignment-requests', { username: form.username, reason: form.reason })
    message.success('申请已提交，等待管理员审批')
    visible.value = false
    form.username = ''; form.reason = ''
    await load()
  } catch (_) {}
}

const statusTag = (s) => ({
  PENDING:  { color: 'orange', text: '待审批' },
  APPROVED: { color: 'green',  text: '已通过' },
  REJECTED: { color: 'red',    text: '已驳回' }
}[s] || { color: 'default', text: s })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '目标患者', key: 'patient' },
  { title: '申请理由', dataIndex: 'reason' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '提交时间', dataIndex: 'createTime', width: 160,
    customRender: ({ text }) => dayjs(text).format('YYYY-MM-DD HH:mm') },
  { title: '处理时间', dataIndex: 'processedTime', width: 160,
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm') : '—' }
]

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-title">
      患者分配申请
      <a-button type="primary" style="margin-left:auto;" @click="visible = true">➕ 新申请</a-button>
    </div>

    <a-alert show-icon style="margin-bottom: 16px;"
      message="医生通过此功能申请绑定新患者，由管理员审批通过后患者会自动归到您名下。" />

    <div class="section-card">
      <a-table :columns="columns" :data-source="list" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'patient'">
            <div>
              <div style="font-weight:600;">{{ record.patientName }}</div>
              <div style="color:#999;font-size:12px;">@{{ record.patientUsername }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusTag(record.status).color">{{ statusTag(record.status).text }}</a-tag>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal v-model:open="visible" title="申请绑定患者" ok-text="提交申请" cancel-text="取消" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="患者用户名" required>
          <a-input v-model:value="form.username" placeholder="例如：patient02" />
        </a-form-item>
        <a-form-item label="申请理由（可选）">
          <a-textarea v-model:value="form.reason" :rows="3" placeholder="说明为什么需要绑定该患者" />
        </a-form-item>
        <a-alert type="info" show-icon style="margin-top: 8px;"
          message="提交后由管理员审批，通过后患者的主治医生将更新为您。" />
      </a-form>
    </a-modal>
  </div>
</template>
