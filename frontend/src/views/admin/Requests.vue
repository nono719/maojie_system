<script setup>
import { onMounted, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import http from '@/api/index'
import dayjs from 'dayjs'

const list = ref([])
const filter = ref('PENDING')

const load = async () => {
  list.value = await http.get('/admin/assignment-requests' + (filter.value ? `?status=${filter.value}` : ''))
}

const approve = (row) => {
  Modal.confirm({
    title: '通过该申请？',
    content: `批准后，患者「${row.patientName}」的主治医生将更新为「${row.doctorName}」`,
    okText: '批准', cancelText: '取消',
    onOk: async () => {
      await http.put(`/admin/assignment-requests/${row.id}/approve`)
      message.success('已通过')
      await load()
    }
  })
}

const reject = (row) => {
  Modal.confirm({
    title: '驳回申请？',
    content: `驳回医生「${row.doctorName}」对患者「${row.patientName}」的绑定申请？`,
    okText: '驳回', okType: 'danger', cancelText: '取消',
    onOk: async () => {
      await http.put(`/admin/assignment-requests/${row.id}/reject`)
      message.success('已驳回')
      await load()
    }
  })
}

const statusTag = (s) => ({
  PENDING:  { color: 'orange', text: '待审批' },
  APPROVED: { color: 'green',  text: '已通过' },
  REJECTED: { color: 'red',    text: '已驳回' }
}[s] || { color: 'default', text: s })

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '医生', key: 'doctor' },
  { title: '患者', key: 'patient' },
  { title: '申请理由', dataIndex: 'reason' },
  { title: '当前主治', key: 'currentDoctor', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '提交时间', dataIndex: 'createTime', width: 150,
    customRender: ({ text }) => dayjs(text).format('YYYY-MM-DD HH:mm') },
  { title: '操作', key: 'action', width: 160, fixed: 'right' }
]

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="page-title">
      患者分配申请
      <a-radio-group v-model:value="filter" button-style="solid" style="margin-left:auto;" @change="load">
        <a-radio-button value="PENDING">待审批</a-radio-button>
        <a-radio-button value="APPROVED">已通过</a-radio-button>
        <a-radio-button value="REJECTED">已驳回</a-radio-button>
        <a-radio-button value="">全部</a-radio-button>
      </a-radio-group>
    </div>

    <div class="section-card">
      <a-table :columns="columns" :data-source="list" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'doctor'">
            <div>
              <div style="font-weight:600;">{{ record.doctorName }}</div>
              <div style="color:#999;font-size:12px;">@{{ record.doctorUsername }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'patient'">
            <div>
              <div style="font-weight:600;">{{ record.patientName }}</div>
              <div style="color:#999;font-size:12px;">@{{ record.patientUsername }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'currentDoctor'">
            <span v-if="record.patientCurrentDoctorId">#{{ record.patientCurrentDoctorId }}</span>
            <span v-else style="color:#999;">未绑定</span>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusTag(record.status).color">{{ statusTag(record.status).text }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="record.status === 'PENDING'">
              <a-button type="link" size="small" @click="approve(record)">通过</a-button>
              <a-button type="link" size="small" danger @click="reject(record)">驳回</a-button>
            </a-space>
            <span v-else style="color:#999;">已处理</span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>
