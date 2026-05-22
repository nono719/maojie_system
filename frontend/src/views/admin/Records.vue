<script setup>
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/index'
import { verifyOnChain } from '@/api/training'
import dayjs from 'dayjs'

const list = ref([])
const load = async () => { list.value = await http.get('/admin/training-records') }

const verify = async (row) => {
  const r = await verifyOnChain(row.id)
  message[r.verified ? 'success' : 'error'](r.verified ? '链上哈希一致' : '哈希校验失败 — 数据已被篡改')
}

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户ID', dataIndex: 'userId' },
  { title: '任务ID', dataIndex: 'taskId' },
  { title: '时长(秒)', dataIndex: 'duration' },
  { title: '完成率', dataIndex: 'completionRate',
    customRender: ({ text }) => text != null ? `${text}%` : '-' },
  { title: '上链状态', dataIndex: 'chainStatus' },
  { title: '数据哈希', dataIndex: 'dataHash',
    customRender: ({ text }) => text ? text.slice(0, 12) + '...' : '-' },
  { title: '时间', dataIndex: 'createTime',
    customRender: ({ text }) => dayjs(text).format('YYYY-MM-DD HH:mm') },
  { title: '操作', key: 'action' }
]

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">训练记录审计</h2>
    <a-table :columns="columns" :data-source="list" row-key="id" :pagination="{ pageSize: 20 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-button type="link" @click="verify(record)" :disabled="!record.dataHash">校验链上一致性</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
