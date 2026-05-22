<script setup>
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import http from '@/api/index'
import dayjs from 'dayjs'

const list = ref([])
const load = async () => { list.value = await http.get('/admin/users') }

const toggle = async (row) => {
  const next = row.status === 1 ? 0 : 1
  await http.put(`/admin/users/${row.id}/status?status=${next}`)
  message.success(next === 1 ? '已启用' : '已停用')
  await load()
}

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '用户名', dataIndex: 'username' },
  { title: '真实姓名', dataIndex: 'realName' },
  { title: '角色', dataIndex: 'role' },
  { title: '手机号', dataIndex: 'phone' },
  { title: '创建时间', dataIndex: 'createTime',
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm') : '-' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '操作', key: 'action' }
]

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom:16px;">用户管理</h2>
    <a-table :columns="columns" :data-source="list" row-key="id" :pagination="{ pageSize: 20 }">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button v-if="record.role !== 'ADMIN'" type="link" @click="toggle(record)">
            {{ record.status === 1 ? '停用' : '启用' }}
          </a-button>
          <span v-else style="color:#999;">—</span>
        </template>
      </template>
    </a-table>
  </div>
</template>
